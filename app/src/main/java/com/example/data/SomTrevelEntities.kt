package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String) {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Administrator"),
    BOOKING_MANAGER("Booking Manager"),
    ACCOUNTANT("Accountant"),
    TRAVEL_AGENT("Travel Agent"),
    CUSTOMER("Customer");

    val isStaff: Boolean
        get() = this != CUSTOMER

    val canManageAccounting: Boolean
        get() = this == SUPER_ADMIN || this == ADMIN || this == ACCOUNTANT

    val canManageBookings: Boolean
        get() = this == SUPER_ADMIN || this == ADMIN || this == BOOKING_MANAGER || this == TRAVEL_AGENT

    val canManageAdminSettings: Boolean
        get() = this == SUPER_ADMIN || this == ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String = UserRole.CUSTOMER.name,
    val passportNumber: String = "",
    val nationality: String = "Somali",
    val isVerified: Boolean = true,
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val city: String,
    val country: String,
    val region: String,
    val isSomali: Boolean,
    val description: String,
    val attractions: String,
    val bestTimeToVisit: String,
    val travelInfo: String,
    val startingPriceUsd: Double,
    val availablePackagesCount: Int,
    val coverKey: String,
    val customImageUri: String? = null,
    val isDemo: Boolean = false
)

@Entity(tableName = "flight_offers")
data class FlightOfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val airlineName: String,
    val airlineCode: String,
    val flightNumber: String,
    val originCode: String,
    val originCity: String,
    val destinationCode: String,
    val destinationCity: String,
    val isDomestic: Boolean,
    val departureTime: String,
    val arrivalTime: String,
    val durationLabel: String,
    val baggageAllowance: String,
    val economyPriceUsd: Double,
    val premiumEconomyPriceUsd: Double,
    val businessPriceUsd: Double,
    val firstClassPriceUsd: Double,
    val seatsAvailable: Int,
    val isDemo: Boolean = false
)

@Entity(tableName = "flight_bookings")
data class FlightBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pnr: String,
    val userId: Long,
    val customerName: String,
    val customerSurname: String,
    val flightId: Long,
    val airlineName: String,
    val flightNumber: String,
    val originCode: String,
    val originCity: String,
    val destinationCode: String,
    val destinationCity: String,
    val departureDate: String,
    val returnDate: String = "",
    val departureTime: String,
    val arrivalTime: String,
    val tripType: String, // ONE_WAY, ROUND_TRIP, MULTI_CITY
    val cabinClass: String, // Economy, Premium Economy, Business, First Class
    val adultCount: Int,
    val childCount: Int,
    val infantCount: Int,
    val passengerSummary: String,
    val baggageAllowance: String,
    val totalUsd: Double,
    val paymentStatus: String, // PAID, PENDING, REFUNDED
    val ticketStatus: String, // Pending, Confirmed, Issued, Cancelled, Refunded
    val notes: String = "",
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "hotels")
data class HotelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,
    val country: String,
    val stars: Int,
    val description: String,
    val roomCategoriesCsv: String,
    val pricePerNightUsd: Double,
    val roomsAvailable: Int,
    val coverKey: String,
    val customImageUri: String? = null,
    val isDemo: Boolean = false
)

@Entity(tableName = "hotel_bookings")
data class HotelBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceCode: String,
    val userId: Long,
    val customerName: String,
    val hotelId: Long,
    val hotelName: String,
    val cityCountry: String,
    val roomCategory: String,
    val checkInDate: String,
    val checkOutDate: String,
    val nights: Int,
    val guestsCount: Int,
    val totalUsd: Double,
    val paymentStatus: String,
    val status: String, // Pending, Confirmed, Cancelled
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "travel_packages")
data class TravelPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // HOLIDAY, HAJJ, UMRAH
    val tier: String, // Economy, Standard, VIP
    val destinationLabel: String,
    val durationDays: Int,
    val departureDate: String,
    val accommodationDetails: String,
    val transportDetails: String,
    val includedServices: String,
    val priceUsd: Double,
    val seatsAvailable: Int,
    val coverKey: String,
    val customImageUri: String? = null,
    val isDemo: Boolean = false
)

@Entity(tableName = "package_bookings")
data class PackageBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceCode: String,
    val userId: Long,
    val customerName: String,
    val packageId: Long,
    val packageTitle: String,
    val category: String, // HOLIDAY, HAJJ, UMRAH
    val tier: String,
    val groupCode: String,
    val travelerCount: Int,
    val travelerNames: String,
    val passportNumbers: String,
    val totalUsd: Double,
    val paidUsd: Double,
    val status: String, // Pending, Confirmed, Cancelled
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "visa_applications")
data class VisaApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceCode: String,
    val userId: Long,
    val applicantName: String,
    val destinationCountry: String,
    val visaCategory: String,
    val passportNumber: String,
    val nationality: String,
    val requiredDocumentsSummary: String,
    val uploadedDocUri: String? = null,
    val status: String, // Submitted, Under Review, Additional Documents Required, Approved, Rejected
    val feeUsd: Double,
    val adminRemarks: String = "",
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceCode: String,
    val type: String, // INCOME, EXPENSE
    val category: String, // Ticket Sales, Hotel Income, Hajj Revenue, Umrah Revenue, Visa Revenue, Other Income, Salaries, Office Expenses, Supplier Payments, Refunds
    val description: String,
    val amountUsd: Double,
    val currencyCode: String = "USD",
    val linkedBookingRef: String = "",
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey val code: String,
    val name: String,
    val symbol: String,
    val rateFromUsd: Double
)

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val customerName: String,
    val serviceType: String, // INSURANCE, TRANSFER, SUPPORT, MESSAGE, REVIEW
    val subject: String,
    val details: String,
    val status: String, // Open, In Progress, Resolved
    val adminReply: String = "",
    val rating: Int = 5,
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "editorial_content")
data class EditorialContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentType: String, // BLOG, OFFER, FAQ, TESTIMONIAL
    val title: String,
    val categoryOrSubtitle: String,
    val body: String,
    val badgeText: String = "",
    val coverKey: String = "hero_aviation",
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Destination, Hotel, Airplane, Hajj & Umrah, Gallery, Blog, Banner, Logo
    val drawableKey: String,
    val localUri: String? = null,
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val targetSummary: String,
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // 0 = broadcast/all staff
    val title: String,
    val message: String,
    val category: String,
    val isRead: Boolean = false,
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
