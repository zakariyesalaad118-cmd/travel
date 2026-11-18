package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SomTrevelDao {

    // --- Users & Auth ---
    @Query("SELECT * FROM users ORDER BY createdAtMillis DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Destinations ---
    @Query("SELECT * FROM destinations ORDER BY isSomali DESC, city ASC")
    fun observeDestinations(): Flow<List<DestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(items: List<DestinationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(item: DestinationEntity): Long

    @Query("DELETE FROM destinations WHERE id = :id")
    suspend fun deleteDestination(id: Long)

    // --- Flights ---
    @Query("SELECT * FROM flight_offers ORDER BY economyPriceUsd ASC")
    fun observeFlightOffers(): Flow<List<FlightOfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlightOffers(items: List<FlightOfferEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlightOffer(item: FlightOfferEntity): Long

    @Query("DELETE FROM flight_offers WHERE id = :id")
    suspend fun deleteFlightOffer(id: Long)

    // --- Flight Bookings & E-Tickets ---
    @Query("SELECT * FROM flight_bookings ORDER BY createdAtMillis DESC")
    fun observeAllFlightBookings(): Flow<List<FlightBookingEntity>>

    @Query("SELECT * FROM flight_bookings WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeUserFlightBookings(userId: Long): Flow<List<FlightBookingEntity>>

    @Query("SELECT * FROM flight_bookings WHERE UPPER(pnr) = UPPER(:pnr) AND LOWER(customerSurname) = LOWER(:surname) LIMIT 1")
    suspend fun verifyTicketByPnrAndSurname(pnr: String, surname: String): FlightBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlightBooking(booking: FlightBookingEntity): Long

    @Query("UPDATE flight_bookings SET ticketStatus = :ticketStatus, paymentStatus = :paymentStatus WHERE id = :bookingId")
    suspend fun updateFlightBookingStatus(bookingId: Long, ticketStatus: String, paymentStatus: String)

    // --- Hotels & Hotel Bookings ---
    @Query("SELECT * FROM hotels ORDER BY stars DESC, pricePerNightUsd ASC")
    fun observeHotels(): Flow<List<HotelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotels(items: List<HotelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotel(hotel: HotelEntity): Long

    @Query("DELETE FROM hotels WHERE id = :id")
    suspend fun deleteHotel(id: Long)

    @Query("SELECT * FROM hotel_bookings ORDER BY createdAtMillis DESC")
    fun observeAllHotelBookings(): Flow<List<HotelBookingEntity>>

    @Query("SELECT * FROM hotel_bookings WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeUserHotelBookings(userId: Long): Flow<List<HotelBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotelBooking(booking: HotelBookingEntity): Long

    @Query("UPDATE hotel_bookings SET status = :status, paymentStatus = :paymentStatus WHERE id = :bookingId")
    suspend fun updateHotelBookingStatus(bookingId: Long, status: String, paymentStatus: String)

    // --- Packages (Holiday, Hajj, Umrah) ---
    @Query("SELECT * FROM travel_packages ORDER BY category ASC, priceUsd ASC")
    fun observeTravelPackages(): Flow<List<TravelPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelPackages(items: List<TravelPackageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravelPackage(pkg: TravelPackageEntity): Long

    @Query("DELETE FROM travel_packages WHERE id = :id")
    suspend fun deleteTravelPackage(id: Long)

    @Query("SELECT * FROM package_bookings ORDER BY createdAtMillis DESC")
    fun observeAllPackageBookings(): Flow<List<PackageBookingEntity>>

    @Query("SELECT * FROM package_bookings WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeUserPackageBookings(userId: Long): Flow<List<PackageBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackageBooking(booking: PackageBookingEntity): Long

    @Query("UPDATE package_bookings SET status = :status, paidUsd = :paidUsd WHERE id = :bookingId")
    suspend fun updatePackageBooking(bookingId: Long, status: String, paidUsd: Double)

    // --- Visa Applications ---
    @Query("SELECT * FROM visa_applications ORDER BY createdAtMillis DESC")
    fun observeAllVisaApplications(): Flow<List<VisaApplicationEntity>>

    @Query("SELECT * FROM visa_applications WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeUserVisaApplications(userId: Long): Flow<List<VisaApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisaApplication(visa: VisaApplicationEntity): Long

    @Query("UPDATE visa_applications SET status = :status, adminRemarks = :remarks WHERE id = :visaId")
    suspend fun updateVisaStatus(visaId: Long, status: String, remarks: String)

    // --- Accounting & Currencies ---
    @Query("SELECT * FROM financial_transactions ORDER BY createdAtMillis DESC")
    fun observeTransactions(): Flow<List<FinancialTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: FinancialTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<FinancialTransactionEntity>)

    @Query("SELECT * FROM currency_rates ORDER BY code ASC")
    fun observeCurrencyRates(): Flow<List<CurrencyRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencyRates(rates: List<CurrencyRateEntity>)

    @Query("UPDATE currency_rates SET rateFromUsd = :rate WHERE code = :code")
    suspend fun updateCurrencyRate(code: String, rate: Double)

    // --- Service Requests (Insurance, Transfers, Support, Messages, Reviews) ---
    @Query("SELECT * FROM service_requests ORDER BY createdAtMillis DESC")
    fun observeAllServiceRequests(): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeUserServiceRequests(userId: Long): Flow<List<ServiceRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRequest(req: ServiceRequestEntity): Long

    @Query("UPDATE service_requests SET status = :status, adminReply = :reply WHERE id = :requestId")
    suspend fun updateServiceRequest(requestId: Long, status: String, reply: String)

    // --- Editorial Content (Blog, Offers, FAQ, Testimonials) ---
    @Query("SELECT * FROM editorial_content ORDER BY createdAtMillis DESC")
    fun observeEditorialContent(): Flow<List<EditorialContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditorialList(items: List<EditorialContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditorialItem(item: EditorialContentEntity): Long

    @Query("DELETE FROM editorial_content WHERE id = :id")
    suspend fun deleteEditorialItem(id: Long)

    // --- Media Assets ---
    @Query("SELECT * FROM media_assets ORDER BY createdAtMillis DESC")
    fun observeMediaAssets(): Flow<List<MediaAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAssets(items: List<MediaAssetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAsset(item: MediaAssetEntity): Long

    @Query("DELETE FROM media_assets WHERE id = :id")
    suspend fun deleteMediaAsset(id: Long)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY createdAtMillis DESC LIMIT 100")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 0 ORDER BY createdAtMillis DESC")
    fun observeNotificationsForUser(userId: Long): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    // --- Safe Demo Data Cleanup (Preserves all real user-created records!) ---
    @Query("DELETE FROM destinations WHERE isDemo = 1")
    suspend fun clearDemoDestinations()

    @Query("DELETE FROM flight_offers WHERE isDemo = 1")
    suspend fun clearDemoFlights()

    @Query("DELETE FROM flight_bookings WHERE isDemo = 1")
    suspend fun clearDemoFlightBookings()

    @Query("DELETE FROM hotels WHERE isDemo = 1")
    suspend fun clearDemoHotels()

    @Query("DELETE FROM hotel_bookings WHERE isDemo = 1")
    suspend fun clearDemoHotelBookings()

    @Query("DELETE FROM travel_packages WHERE isDemo = 1")
    suspend fun clearDemoPackages()

    @Query("DELETE FROM package_bookings WHERE isDemo = 1")
    suspend fun clearDemoPackageBookings()

    @Query("DELETE FROM visa_applications WHERE isDemo = 1")
    suspend fun clearDemoVisas()

    @Query("DELETE FROM financial_transactions WHERE isDemo = 1")
    suspend fun clearDemoTransactions()

    @Query("DELETE FROM service_requests WHERE isDemo = 1")
    suspend fun clearDemoServiceRequests()

    @Query("DELETE FROM editorial_content WHERE isDemo = 1")
    suspend fun clearDemoEditorial()

    @Query("DELETE FROM media_assets WHERE isDemo = 1")
    suspend fun clearDemoMedia()

    @Query("DELETE FROM audit_logs WHERE isDemo = 1")
    suspend fun clearDemoAuditLogs()

    @Query("DELETE FROM notifications WHERE isDemo = 1")
    suspend fun clearDemoNotifications()

    @Query("DELETE FROM users WHERE isDemo = 1 AND role = 'CUSTOMER'")
    suspend fun clearDemoCustomerUsers()
}
