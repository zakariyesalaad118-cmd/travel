package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SomTrevelRepository(
    private val context: Context,
    private val dao: SomTrevelDao,
    val prefs: UserPreferencesRepository
) {
    suspend fun ensureSeeded() {
        DemoSeeder.seedIfEmpty(dao)
    }

    // --- Public Catalog Flows ---
    val destinations: Flow<List<DestinationEntity>> = dao.observeDestinations()
    val flightOffers: Flow<List<FlightOfferEntity>> = dao.observeFlightOffers()
    val hotels: Flow<List<HotelEntity>> = dao.observeHotels()
    val travelPackages: Flow<List<TravelPackageEntity>> = dao.observeTravelPackages()
    val currencyRates: Flow<List<CurrencyRateEntity>> = dao.observeCurrencyRates()
    val editorialContent: Flow<List<EditorialContentEntity>> = dao.observeEditorialContent()
    val mediaAssets: Flow<List<MediaAssetEntity>> = dao.observeMediaAssets()

    // --- User-Scoped Flows (Strict Customer Isolation) ---
    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)
    fun observeUserFlightBookings(userId: Long): Flow<List<FlightBookingEntity>> = dao.observeUserFlightBookings(userId)
    fun observeUserHotelBookings(userId: Long): Flow<List<HotelBookingEntity>> = dao.observeUserHotelBookings(userId)
    fun observeUserPackageBookings(userId: Long): Flow<List<PackageBookingEntity>> = dao.observeUserPackageBookings(userId)
    fun observeUserVisaApplications(userId: Long): Flow<List<VisaApplicationEntity>> = dao.observeUserVisaApplications(userId)
    fun observeUserServiceRequests(userId: Long): Flow<List<ServiceRequestEntity>> = dao.observeUserServiceRequests(userId)
    fun observeUserNotifications(userId: Long): Flow<List<AppNotificationEntity>> = dao.observeNotificationsForUser(userId)

    // --- Admin / Staff Flows ---
    val allUsers: Flow<List<UserEntity>> = dao.observeAllUsers()
    val allFlightBookings: Flow<List<FlightBookingEntity>> = dao.observeAllFlightBookings()
    val allHotelBookings: Flow<List<HotelBookingEntity>> = dao.observeAllHotelBookings()
    val allPackageBookings: Flow<List<PackageBookingEntity>> = dao.observeAllPackageBookings()
    val allVisaApplications: Flow<List<VisaApplicationEntity>> = dao.observeAllVisaApplications()
    val allTransactions: Flow<List<FinancialTransactionEntity>> = dao.observeTransactions()
    val allServiceRequests: Flow<List<ServiceRequestEntity>> = dao.observeAllServiceRequests()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.observeAuditLogs()

    // --- Authentication ---
    suspend fun login(email: String, rawPassword: String): Result<UserEntity> {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || rawPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required."))
        }
        val user = dao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account found for $cleanEmail."))
        if (user.passwordHash != SecurityUtil.hashPassword(rawPassword)) {
            return Result.failure(IllegalArgumentException("Incorrect password."))
        }
        prefs.setCurrentUserId(user.id)
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = user.fullName,
                actorRole = user.role,
                action = "USER_LOGIN",
                targetSummary = "Signed in (${user.email})"
            )
        )
        return Result.success(user)
    }

    suspend fun quickDemoLogin(userId: Long): Result<UserEntity> {
        val user = dao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("Demo user unavailable."))
        prefs.setCurrentUserId(user.id)
        return Result.success(user)
    }

    suspend fun registerCustomer(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        passportNumber: String,
        nationality: String
    ): Result<UserEntity> {
        val cleanEmail = email.trim()
        if (fullName.isBlank() || !cleanEmail.contains("@") || password.length < 6) {
            return Result.failure(IllegalArgumentException("Enter a valid name, email, and 6+ character password."))
        }
        if (dao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        val user = UserEntity(
            fullName = fullName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            passwordHash = SecurityUtil.hashPassword(password),
            role = UserRole.CUSTOMER.name,
            passportNumber = passportNumber.trim().uppercase(),
            nationality = nationality.trim().ifBlank { "Somali" },
            isDemo = false
        )
        val id = dao.insertUser(user)
        val saved = user.copy(id = id)
        prefs.setCurrentUserId(id)
        dao.insertNotification(
            AppNotificationEntity(
                userId = id,
                title = "Welcome to SOM TREVEL",
                message = "Your personal customer portal is ready. Book flights, Hajj & Umrah, hotels, and track visas.",
                category = "Account"
            )
        )
        return Result.success(saved)
    }

    suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit> {
        if (newPassword.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        val user = dao.getUserByEmail(email.trim())
            ?: return Result.failure(IllegalArgumentException("No account registered with that email."))
        dao.updateUser(user.copy(passwordHash = SecurityUtil.hashPassword(newPassword)))
        return Result.success(Unit)
    }

    suspend fun logout() {
        prefs.setCurrentUserId(null)
    }

    suspend fun updateProfile(userId: Long, fullName: String, phone: String, passport: String, nationality: String) {
        val existing = dao.getUserById(userId) ?: return
        dao.updateUser(
            existing.copy(
                fullName = fullName.trim(),
                phone = phone.trim(),
                passportNumber = passport.trim().uppercase(),
                nationality = nationality.trim()
            )
        )
    }

    // --- PNR Ticket Verification ---
    suspend fun verifyTicket(pnr: String, surname: String): FlightBookingEntity? {
        if (pnr.isBlank() || surname.isBlank()) return null
        return dao.verifyTicketByPnrAndSurname(pnr.trim(), surname.trim())
    }

    // --- Flight Booking + E-Ticket + Accounting ---
    suspend fun bookFlight(
        user: UserEntity,
        flight: FlightOfferEntity,
        tripType: String,
        cabinClass: String,
        departureDate: String,
        returnDate: String,
        adults: Int,
        children: Int,
        infants: Int,
        passengerNotes: String,
        payNow: Boolean
    ): FlightBookingEntity {
        val unitPrice = when (cabinClass) {
            "Premium Economy" -> flight.premiumEconomyPriceUsd
            "Business" -> flight.businessPriceUsd
            "First Class" -> flight.firstClassPriceUsd
            else -> flight.economyPriceUsd
        }
        val tripMultiplier = if (tripType == "ROUND_TRIP") 1.85 else if (tripType == "MULTI_CITY") 2.15 else 1.0
        val total = (adults * unitPrice + children * unitPrice * 0.75 + infants * unitPrice * 0.15) * tripMultiplier
        val pnr = SecurityUtil.generatePnr()
        val surname = user.fullName.trim().split(" ").lastOrNull().orEmpty().ifBlank { user.fullName }
        val booking = FlightBookingEntity(
            pnr = pnr,
            userId = user.id,
            customerName = user.fullName,
            customerSurname = surname,
            flightId = flight.id,
            airlineName = flight.airlineName,
            flightNumber = flight.flightNumber,
            originCode = flight.originCode,
            originCity = flight.originCity,
            destinationCode = flight.destinationCode,
            destinationCity = flight.destinationCity,
            departureDate = departureDate,
            returnDate = returnDate,
            departureTime = flight.departureTime,
            arrivalTime = flight.arrivalTime,
            tripType = tripType,
            cabinClass = cabinClass,
            adultCount = adults,
            childCount = children,
            infantCount = infants,
            passengerSummary = "$adults Adult(s), $children Child(ren), $infants Infant(s) • ${user.fullName}",
            baggageAllowance = flight.baggageAllowance,
            totalUsd = total,
            paymentStatus = if (payNow) "PAID" else "PENDING",
            ticketStatus = if (payNow) "Issued" else "Pending",
            notes = passengerNotes,
            isDemo = false
        )
        val id = dao.insertFlightBooking(booking)
        if (payNow) {
            dao.insertTransaction(
                FinancialTransactionEntity(
                    referenceCode = SecurityUtil.generateReference("TX"),
                    type = "INCOME",
                    category = "Ticket Sales",
                    description = "${flight.airlineName} ${flight.flightNumber} ($pnr)",
                    amountUsd = total,
                    linkedBookingRef = pnr,
                    isDemo = false
                )
            )
        }
        dao.insertNotification(
            AppNotificationEntity(
                userId = user.id,
                title = if (payNow) "E-Ticket Issued • $pnr" else "Flight Reserved • $pnr",
                message = "${flight.originCode} → ${flight.destinationCode} on $departureDate.",
                category = "Ticket"
            )
        )
        return booking.copy(id = id)
    }

    suspend fun updateFlightTicketStatus(actor: UserEntity, booking: FlightBookingEntity, ticketStatus: String, paymentStatus: String) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.updateFlightBookingStatus(booking.id, ticketStatus, paymentStatus)
        if (ticketStatus == "Refunded" && booking.paymentStatus == "PAID") {
            dao.insertTransaction(
                FinancialTransactionEntity(
                    referenceCode = SecurityUtil.generateReference("RF"),
                    type = "EXPENSE",
                    category = "Refunds",
                    description = "Ticket refund for PNR ${booking.pnr}",
                    amountUsd = booking.totalUsd,
                    linkedBookingRef = booking.pnr
                )
            )
        } else if (paymentStatus == "PAID" && booking.paymentStatus != "PAID") {
            dao.insertTransaction(
                FinancialTransactionEntity(
                    referenceCode = SecurityUtil.generateReference("TX"),
                    type = "INCOME",
                    category = "Ticket Sales",
                    description = "Settled ticket payment for PNR ${booking.pnr}",
                    amountUsd = booking.totalUsd,
                    linkedBookingRef = booking.pnr
                )
            )
        }
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = actor.fullName,
                actorRole = actor.role,
                action = "TICKET_STATUS_UPDATE",
                targetSummary = "PNR ${booking.pnr} → $ticketStatus ($paymentStatus)"
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                userId = booking.userId,
                title = "Ticket ${booking.pnr}: $ticketStatus",
                message = "Payment: $paymentStatus • ${booking.originCode} → ${booking.destinationCode}",
                category = "Ticket"
            )
        )
    }

    suspend fun requestTicketAction(user: UserEntity, booking: FlightBookingEntity, actionType: String) {
        if (booking.userId != user.id) return
        dao.insertServiceRequest(
            ServiceRequestEntity(
                userId = user.id,
                customerName = user.fullName,
                serviceType = "SUPPORT",
                subject = "$actionType Request • PNR ${booking.pnr}",
                details = "Customer requested $actionType for ${booking.airlineName} ${booking.flightNumber} (${booking.departureDate}).",
                status = "Open"
            )
        )
    }

    // --- Hotel Booking ---
    suspend fun bookHotel(
        user: UserEntity,
        hotel: HotelEntity,
        roomCategory: String,
        checkIn: String,
        checkOut: String,
        nights: Int,
        guests: Int
    ): HotelBookingEntity {
        val total = hotel.pricePerNightUsd * nights.coerceAtLeast(1)
        val ref = SecurityUtil.generateReference("HTL")
        val booking = HotelBookingEntity(
            referenceCode = ref,
            userId = user.id,
            customerName = user.fullName,
            hotelId = hotel.id,
            hotelName = hotel.name,
            cityCountry = "${hotel.city}, ${hotel.country}",
            roomCategory = roomCategory,
            checkInDate = checkIn,
            checkOutDate = checkOut,
            nights = nights,
            guestsCount = guests,
            totalUsd = total,
            paymentStatus = "PAID",
            status = "Confirmed"
        )
        val id = dao.insertHotelBooking(booking)
        dao.insertTransaction(
            FinancialTransactionEntity(
                referenceCode = SecurityUtil.generateReference("TX"),
                type = "INCOME",
                category = "Hotel Income",
                description = "${hotel.name} ($nights nights) • $ref",
                amountUsd = total,
                linkedBookingRef = ref
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                userId = user.id,
                title = "Hotel Confirmed • $ref",
                message = "${hotel.name} ($roomCategory) for $nights night(s).",
                category = "Hotel"
            )
        )
        return booking.copy(id = id)
    }

    // --- Hajj, Umrah & Holiday Package Booking ---
    suspend fun bookPackage(
        user: UserEntity,
        pkg: TravelPackageEntity,
        travelerCount: Int,
        travelerNames: String,
        passportNumbers: String,
        depositPaidUsd: Double
    ): PackageBookingEntity {
        val prefix = when (pkg.category) {
            "HAJJ" -> "HJJ"
            "UMRAH" -> "UMR"
            else -> "PKG"
        }
        val ref = SecurityUtil.generateReference(prefix)
        val total = pkg.priceUsd * travelerCount.coerceAtLeast(1)
        val paid = depositPaidUsd.coerceIn(0.0, total)
        val booking = PackageBookingEntity(
            referenceCode = ref,
            userId = user.id,
            customerName = user.fullName,
            packageId = pkg.id,
            packageTitle = pkg.title,
            category = pkg.category,
            tier = pkg.tier,
            groupCode = "GRP-$prefix-2027",
            travelerCount = travelerCount,
            travelerNames = travelerNames.ifBlank { user.fullName },
            passportNumbers = passportNumbers.ifBlank { user.passportNumber },
            totalUsd = total,
            paidUsd = paid,
            status = if (paid >= total * 0.5) "Confirmed" else "Pending"
        )
        val id = dao.insertPackageBooking(booking)
        if (paid > 0) {
            val cat = when (pkg.category) {
                "HAJJ" -> "Hajj Revenue"
                "UMRAH" -> "Umrah Revenue"
                else -> "Other Income"
            }
            dao.insertTransaction(
                FinancialTransactionEntity(
                    referenceCode = SecurityUtil.generateReference("TX"),
                    type = "INCOME",
                    category = cat,
                    description = "${pkg.title} ($travelerCount pax) • $ref",
                    amountUsd = paid,
                    linkedBookingRef = ref
                )
            )
        }
        dao.insertNotification(
            AppNotificationEntity(
                userId = user.id,
                title = "${pkg.category} Registration • $ref",
                message = "${pkg.title} reserved for $travelerCount traveler(s).",
                category = pkg.category
            )
        )
        return booking.copy(id = id)
    }

    // --- Visa Services & Zero-Permission Photo Picker File Copy ---
    suspend fun copyPickedUriToPrivateStorage(uri: Uri, folder: String): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, folder).apply { mkdirs() }
            val dest = File(dir, "upload_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    suspend fun submitVisaApplication(
        user: UserEntity,
        country: String,
        visaCategory: String,
        passportNumber: String,
        nationality: String,
        uploadedDocPath: String?
    ): VisaApplicationEntity {
        val ref = SecurityUtil.generateReference("VSA")
        val fee = when {
            country.contains("UK") || country.contains("United States") || country.contains("Canada") -> 240.0
            country.contains("Saudi") -> 185.0
            else -> 145.0
        }
        val visa = VisaApplicationEntity(
            referenceCode = ref,
            userId = user.id,
            applicantName = user.fullName,
            destinationCountry = country,
            visaCategory = visaCategory,
            passportNumber = passportNumber.trim().uppercase(),
            nationality = nationality.trim(),
            requiredDocumentsSummary = "Passport Bio Page, Photo, Travel Itinerary",
            uploadedDocUri = uploadedDocPath,
            status = "Submitted",
            feeUsd = fee,
            adminRemarks = "Application received by SOM TREVEL Visa Desk"
        )
        val id = dao.insertVisaApplication(visa)
        dao.insertTransaction(
            FinancialTransactionEntity(
                referenceCode = SecurityUtil.generateReference("TX"),
                type = "INCOME",
                category = "Visa Revenue",
                description = "$country $visaCategory ($ref)",
                amountUsd = fee,
                linkedBookingRef = ref
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                userId = user.id,
                title = "Visa Submitted • $ref",
                message = "$country ($visaCategory) is now under review.",
                category = "Visa"
            )
        )
        return visa.copy(id = id)
    }

    suspend fun reviewVisa(actor: UserEntity, visa: VisaApplicationEntity, newStatus: String, remarks: String) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.updateVisaStatus(visa.id, newStatus, remarks)
        dao.insertNotification(
            AppNotificationEntity(
                userId = visa.userId,
                title = "Visa ${visa.referenceCode}: $newStatus",
                message = remarks.ifBlank { "Status updated by SOM TREVEL Visa Desk." },
                category = "Visa"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = actor.fullName,
                actorRole = actor.role,
                action = "VISA_REVIEW",
                targetSummary = "${visa.referenceCode} → $newStatus"
            )
        )
    }

    // --- Services, Support & Messages ---
    suspend fun submitServiceRequest(
        user: UserEntity?,
        customerName: String,
        serviceType: String,
        subject: String,
        details: String,
        rating: Int = 5
    ) {
        dao.insertServiceRequest(
            ServiceRequestEntity(
                userId = user?.id ?: 0L,
                customerName = customerName.ifBlank { user?.fullName ?: "Guest Traveler" },
                serviceType = serviceType,
                subject = subject,
                details = details,
                status = "Open",
                rating = rating
            )
        )
    }

    suspend fun replyServiceRequest(actor: UserEntity, req: ServiceRequestEntity, status: String, reply: String) {
        if (!UserRole.valueOf(actor.role).isStaff) return
        dao.updateServiceRequest(req.id, status, reply)
        if (req.userId > 0L) {
            dao.insertNotification(
                AppNotificationEntity(
                    userId = req.userId,
                    title = "Update: ${req.subject}",
                    message = reply,
                    category = "Support"
                )
            )
        }
    }

    // --- Admin Catalog & Accounting & Media ---
    suspend fun addFlightOffer(actor: UserEntity, offer: FlightOfferEntity) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.insertFlightOffer(offer)
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "ADD_FLIGHT", targetSummary = "${offer.airlineCode} ${offer.flightNumber} ${offer.originCode}-${offer.destinationCode}"))
    }

    suspend fun deleteFlightOffer(actor: UserEntity, id: Long) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.deleteFlightOffer(id)
    }

    suspend fun addHotel(actor: UserEntity, hotel: HotelEntity) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.insertHotel(hotel)
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "ADD_HOTEL", targetSummary = hotel.name))
    }

    suspend fun deleteHotel(actor: UserEntity, id: Long) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.deleteHotel(id)
    }

    suspend fun addTravelPackage(actor: UserEntity, pkg: TravelPackageEntity) {
        if (!UserRole.valueOf(actor.role).canManageBookings) return
        dao.insertTravelPackage(pkg)
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "ADD_PACKAGE", targetSummary = "${pkg.category}: ${pkg.title}"))
    }

    suspend fun recordFinancialTransaction(
        actor: UserEntity,
        type: String,
        category: String,
        description: String,
        amountUsd: Double
    ) {
        if (!UserRole.valueOf(actor.role).canManageAccounting) return
        val ref = SecurityUtil.generateReference(if (type == "INCOME") "INC" else "EXP")
        dao.insertTransaction(
            FinancialTransactionEntity(
                referenceCode = ref,
                type = type,
                category = category,
                description = description,
                amountUsd = amountUsd,
                isDemo = false
            )
        )
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "ACCOUNTING_$type", targetSummary = "$ref: $category $$amountUsd"))
    }

    suspend fun updateCurrencyRate(actor: UserEntity, code: String, newRate: Double) {
        if (!UserRole.valueOf(actor.role).canManageAccounting || newRate <= 0.0) return
        dao.updateCurrencyRate(code, newRate)
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "UPDATE_FX_RATE", targetSummary = "$code = $newRate"))
    }

    suspend fun addMediaAsset(actor: UserEntity, title: String, category: String, localPath: String) {
        if (!UserRole.valueOf(actor.role).isStaff) return
        dao.insertMediaAsset(MediaAssetEntity(title = title, category = category, drawableKey = "custom", localUri = localPath))
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "MEDIA_UPLOAD", targetSummary = "$category: $title"))
    }

    suspend fun deleteMediaAsset(actor: UserEntity, id: Long) {
        if (!UserRole.valueOf(actor.role).isStaff) return
        dao.deleteMediaAsset(id)
    }

    suspend fun addEditorialPost(actor: UserEntity, contentType: String, title: String, subtitle: String, body: String) {
        if (!UserRole.valueOf(actor.role).isStaff) return
        dao.insertEditorialItem(EditorialContentEntity(contentType = contentType, title = title, categoryOrSubtitle = subtitle, body = body))
    }

    suspend fun updateUserRole(actor: UserEntity, targetUser: UserEntity, newRole: UserRole) {
        if (!UserRole.valueOf(actor.role).canManageAdminSettings) return
        dao.updateUser(targetUser.copy(role = newRole.name))
        dao.insertAuditLog(AuditLogEntity(actorName = actor.fullName, actorRole = actor.role, action = "ROLE_CHANGE", targetSummary = "${targetUser.email} -> ${newRole.name}"))
    }

    suspend fun markNotificationRead(id: Long) = dao.markNotificationRead(id)

    suspend fun removeDemoDataSafely(actor: UserEntity) {
        if (!UserRole.valueOf(actor.role).canManageAdminSettings) return
        dao.clearDemoDestinations()
        dao.clearDemoFlights()
        dao.clearDemoFlightBookings()
        dao.clearDemoHotels()
        dao.clearDemoHotelBookings()
        dao.clearDemoPackages()
        dao.clearDemoPackageBookings()
        dao.clearDemoVisas()
        dao.clearDemoTransactions()
        dao.clearDemoServiceRequests()
        dao.clearDemoEditorial()
        dao.clearDemoMedia()
        dao.clearDemoAuditLogs()
        dao.clearDemoNotifications()
        dao.clearDemoCustomerUsers()
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = actor.fullName,
                actorRole = actor.role,
                action = "DEMO_DATA_PURGED",
                targetSummary = "Safely cleared demo records while preserving real customer data"
            )
        )
    }
}
