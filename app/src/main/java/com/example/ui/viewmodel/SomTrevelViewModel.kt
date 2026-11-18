package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.theme.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountingSummary(
    val totalRevenueUsd: Double = 0.0,
    val totalExpensesUsd: Double = 0.0,
    val netProfitUsd: Double = 0.0,
    val ticketSalesUsd: Double = 0.0,
    val hotelIncomeUsd: Double = 0.0,
    val hajjRevenueUsd: Double = 0.0,
    val umrahRevenueUsd: Double = 0.0,
    val visaRevenueUsd: Double = 0.0
)

sealed interface TicketVerifyState {
    data object Idle : TicketVerifyState
    data class Found(val booking: FlightBookingEntity) : TicketVerifyState
    data class NotFound(val queryInfo: String) : TicketVerifyState
}

@OptIn(ExperimentalCoroutinesApi::class)
class SomTrevelViewModel(private val repository: SomTrevelRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val preferences: StateFlow<AppPreferences> = repository.prefs.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val language: StateFlow<AppLanguage> = preferences
        .map { if (it.languageCode == "so") AppLanguage.SO else AppLanguage.EN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.EN)

    val currentUser: StateFlow<UserEntity?> = preferences
        .flatMapLatest { prefs ->
            if (prefs.currentUserId == null) flowOf(null)
            else repository.observeUser(prefs.currentUserId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Public Catalog
    val destinations: StateFlow<List<DestinationEntity>> = repository.destinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flights: StateFlow<List<FlightOfferEntity>> = repository.flightOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hotels: StateFlow<List<HotelEntity>> = repository.hotels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packages: StateFlow<List<TravelPackageEntity>> = repository.travelPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencyRates: StateFlow<List<CurrencyRateEntity>> = repository.currencyRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val editorialContent: StateFlow<List<EditorialContentEntity>> = repository.editorialContent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mediaAssets: StateFlow<List<MediaAssetEntity>> = repository.mediaAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Strictly Isolated Customer Records
    val myFlightBookings: StateFlow<List<FlightBookingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserFlightBookings(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myHotelBookings: StateFlow<List<HotelBookingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserHotelBookings(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPackageBookings: StateFlow<List<PackageBookingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserPackageBookings(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myVisaApplications: StateFlow<List<VisaApplicationEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserVisaApplications(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myServiceRequests: StateFlow<List<ServiceRequestEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserServiceRequests(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myNotifications: StateFlow<List<AppNotificationEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserNotifications(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Staff / Admin Flows (Gated by Role)
    val allUsers: StateFlow<List<UserEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.allUsers else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlightBookings: StateFlow<List<FlightBookingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.allFlightBookings else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHotelBookings: StateFlow<List<HotelBookingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.allHotelBookings else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackageBookings: StateFlow<List<PackageBookingEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.allPackageBookings else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisaApplications: StateFlow<List<VisaApplicationEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.allVisaApplications else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<FinancialTransactionEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).canManageAccounting) repository.allTransactions else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceRequests: StateFlow<List<ServiceRequestEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.allServiceRequests else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && UserRole.valueOf(user.role).isStaff) repository.auditLogs else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountingSummary: StateFlow<AccountingSummary> = allTransactions
        .map { txs ->
            val incomes = txs.filter { it.type == "INCOME" }
            val expenses = txs.filter { it.type == "EXPENSE" }
            val rev = incomes.sumOf { it.amountUsd }
            val exp = expenses.sumOf { it.amountUsd }
            AccountingSummary(
                totalRevenueUsd = rev,
                totalExpensesUsd = exp,
                netProfitUsd = rev - exp,
                ticketSalesUsd = incomes.filter { it.category == "Ticket Sales" }.sumOf { it.amountUsd },
                hotelIncomeUsd = incomes.filter { it.category == "Hotel Income" }.sumOf { it.amountUsd },
                hajjRevenueUsd = incomes.filter { it.category == "Hajj Revenue" }.sumOf { it.amountUsd },
                umrahRevenueUsd = incomes.filter { it.category == "Umrah Revenue" }.sumOf { it.amountUsd },
                visaRevenueUsd = incomes.filter { it.category == "Visa Revenue" }.sumOf { it.amountUsd }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountingSummary())

    private val _ticketVerifyState = MutableStateFlow<TicketVerifyState>(TicketVerifyState.Idle)
    val ticketVerifyState: StateFlow<TicketVerifyState> = _ticketVerifyState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // Preferences
    fun toggleDarkMode() = viewModelScope.launch {
        repository.prefs.setDarkMode(!preferences.value.darkMode)
    }

    fun toggleLanguage() = viewModelScope.launch {
        val next = if (preferences.value.languageCode == "en") "so" else "en"
        repository.prefs.setLanguage(next)
    }

    fun setCurrency(code: String) = viewModelScope.launch {
        repository.prefs.setCurrency(code)
    }

    fun subscribeNewsletter(email: String) = viewModelScope.launch {
        if (!email.contains("@")) {
            _snackbarMessage.value = "Please enter a valid email address."
            return@launch
        }
        repository.prefs.setNewsletterEmail(email.trim())
        _snackbarMessage.value = "Subscribed $email to SOM TREVEL VIP alerts."
    }

    // Auth
    fun login(email: String, password: String) = viewModelScope.launch {
        repository.login(email, password)
            .onSuccess { _snackbarMessage.value = "Welcome back, ${it.fullName}" }
            .onFailure { _snackbarMessage.value = it.message ?: "Login failed" }
    }

    fun quickSwitchDemoAccount(userId: Long) = viewModelScope.launch {
        repository.quickDemoLogin(userId)
            .onSuccess { _snackbarMessage.value = "Signed in as ${it.fullName} (${it.role})" }
            .onFailure { _snackbarMessage.value = it.message ?: "Switch failed" }
    }

    fun registerCustomer(
        fullName: String, email: String, phone: String, password: String, passport: String, nationality: String
    ) = viewModelScope.launch {
        repository.registerCustomer(fullName, email, phone, password, passport, nationality)
            .onSuccess { _snackbarMessage.value = "Account created for ${it.fullName}" }
            .onFailure { _snackbarMessage.value = it.message ?: "Registration failed" }
    }

    fun resetPassword(email: String, newPassword: String) = viewModelScope.launch {
        repository.resetPasswordByEmail(email, newPassword)
            .onSuccess { _snackbarMessage.value = "Password updated. You may now sign in." }
            .onFailure { _snackbarMessage.value = it.message ?: "Reset failed" }
    }

    fun logout() = viewModelScope.launch {
        repository.logout()
        _snackbarMessage.value = "Signed out securely."
    }

    fun updateProfile(fullName: String, phone: String, passport: String, nationality: String) = viewModelScope.launch {
        val uid = currentUser.value?.id ?: return@launch
        repository.updateProfile(uid, fullName, phone, passport, nationality)
        _snackbarMessage.value = "Profile updated."
    }

    // Verification
    fun verifyTicket(pnr: String, surname: String) = viewModelScope.launch {
        val found = repository.verifyTicket(pnr, surname)
        _ticketVerifyState.value = if (found != null) {
            TicketVerifyState.Found(found)
        } else {
            TicketVerifyState.NotFound("No SOM TREVEL record found for PNR '$pnr' and surname '$surname'.")
        }
    }

    fun resetVerification() {
        _ticketVerifyState.value = TicketVerifyState.Idle
    }

    // Bookings
    fun bookFlight(
        flight: FlightOfferEntity,
        tripType: String,
        cabinClass: String,
        departureDate: String,
        returnDate: String,
        adults: Int,
        children: Int,
        infants: Int,
        notes: String,
        payNow: Boolean
    ) = viewModelScope.launch {
        val user = currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Please sign in on the Portal tab to confirm your flight booking."
            return@launch
        }
        val booking = repository.bookFlight(
            user, flight, tripType, cabinClass, departureDate, returnDate, adults, children, infants, notes, payNow
        )
        _snackbarMessage.value = "Flight booked! PNR ${booking.pnr} (${booking.ticketStatus})"
    }

    fun requestTicketAction(booking: FlightBookingEntity, actionType: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        repository.requestTicketAction(user, booking, actionType)
        _snackbarMessage.value = "$actionType request submitted for PNR ${booking.pnr}."
    }

    fun bookHotel(
        hotel: HotelEntity,
        roomCategory: String,
        checkIn: String,
        checkOut: String,
        nights: Int,
        guests: Int
    ) = viewModelScope.launch {
        val user = currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Please sign in on the Portal tab to reserve a hotel."
            return@launch
        }
        val booking = repository.bookHotel(user, hotel, roomCategory, checkIn, checkOut, nights, guests)
        _snackbarMessage.value = "Hotel reserved! Reference ${booking.referenceCode}"
    }

    fun bookPackage(
        pkg: TravelPackageEntity,
        travelerCount: Int,
        travelerNames: String,
        passportNumbers: String,
        depositUsd: Double
    ) = viewModelScope.launch {
        val user = currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Please sign in on the Portal tab to register for ${pkg.category}."
            return@launch
        }
        val booking = repository.bookPackage(user, pkg, travelerCount, travelerNames, passportNumbers, depositUsd)
        _snackbarMessage.value = "${pkg.category} confirmed! Ref ${booking.referenceCode}"
    }

    fun submitVisaWithUri(
        country: String,
        visaCategory: String,
        passportNumber: String,
        nationality: String,
        pickedUri: Uri?
    ) = viewModelScope.launch {
        val user = currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Please sign in on the Portal tab to submit a visa application."
            return@launch
        }
        val copiedPath = if (pickedUri != null) {
            repository.copyPickedUriToPrivateStorage(pickedUri, "visa_docs")
        } else null
        val visa = repository.submitVisaApplication(user, country, visaCategory, passportNumber, nationality, copiedPath)
        _snackbarMessage.value = "Visa submitted! Tracking Ref ${visa.referenceCode}"
    }

    fun submitServiceRequest(serviceType: String, subject: String, details: String, rating: Int = 5) = viewModelScope.launch {
        repository.submitServiceRequest(currentUser.value, currentUser.value?.fullName ?: "Guest", serviceType, subject, details, rating)
        _snackbarMessage.value = "Request submitted to SOM TREVEL desk."
    }

    fun markNotificationRead(id: Long) = viewModelScope.launch {
        repository.markNotificationRead(id)
    }

    // Admin Actions
    fun adminUpdateTicket(booking: FlightBookingEntity, ticketStatus: String, paymentStatus: String) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.updateFlightTicketStatus(actor, booking, ticketStatus, paymentStatus)
        _snackbarMessage.value = "PNR ${booking.pnr} updated to $ticketStatus."
    }

    fun adminReviewVisa(visa: VisaApplicationEntity, newStatus: String, remarks: String) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.reviewVisa(actor, visa, newStatus, remarks)
        _snackbarMessage.value = "Visa ${visa.referenceCode} updated to $newStatus."
    }

    fun adminReplyRequest(req: ServiceRequestEntity, status: String, reply: String) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.replyServiceRequest(actor, req, status, reply)
        _snackbarMessage.value = "Reply sent to ${req.customerName}."
    }

    fun adminAddFlight(offer: FlightOfferEntity) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.addFlightOffer(actor, offer)
        _snackbarMessage.value = "Added flight ${offer.flightNumber}."
    }

    fun adminDeleteFlight(id: Long) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.deleteFlightOffer(actor, id)
    }

    fun adminAddHotel(hotel: HotelEntity) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.addHotel(actor, hotel)
        _snackbarMessage.value = "Added hotel ${hotel.name}."
    }

    fun adminDeleteHotel(id: Long) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.deleteHotel(actor, id)
    }

    fun adminAddPackage(pkg: TravelPackageEntity) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.addTravelPackage(actor, pkg)
        _snackbarMessage.value = "Published ${pkg.category} package."
    }

    fun adminRecordTransaction(type: String, category: String, description: String, amountUsd: Double) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.recordFinancialTransaction(actor, type, category, description, amountUsd)
        _snackbarMessage.value = "Recorded $type entry ($$amountUsd)."
    }

    fun adminUpdateCurrencyRate(code: String, rate: Double) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.updateCurrencyRate(actor, code, rate)
        _snackbarMessage.value = "Updated $code rate to $rate."
    }

    fun adminUploadMedia(title: String, category: String, uri: Uri) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        val savedPath = repository.copyPickedUriToPrivateStorage(uri, "media_library")
        if (savedPath != null) {
            repository.addMediaAsset(actor, title, category, savedPath)
            _snackbarMessage.value = "Media uploaded to $category library."
        } else {
            _snackbarMessage.value = "Could not import selected photo."
        }
    }

    fun adminDeleteMedia(id: Long) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.deleteMediaAsset(actor, id)
    }

    fun adminAddBlogPost(title: String, subtitle: String, body: String) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.addEditorialPost(actor, "BLOG", title, subtitle, body)
        _snackbarMessage.value = "Published travel news article."
    }

    fun adminChangeUserRole(target: UserEntity, role: UserRole) = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.updateUserRole(actor, target, role)
        _snackbarMessage.value = "${target.fullName} role set to ${role.displayName}."
    }

    fun adminPurgeDemoData() = viewModelScope.launch {
        val actor = currentUser.value ?: return@launch
        repository.removeDemoDataSafely(actor)
        _snackbarMessage.value = "Demo records removed; real customer records preserved."
    }

    class Factory(private val repository: SomTrevelRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SomTrevelViewModel(repository) as T
        }
    }
}
