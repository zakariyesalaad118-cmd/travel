package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        DestinationEntity::class,
        FlightOfferEntity::class,
        FlightBookingEntity::class,
        HotelEntity::class,
        HotelBookingEntity::class,
        TravelPackageEntity::class,
        PackageBookingEntity::class,
        VisaApplicationEntity::class,
        FinancialTransactionEntity::class,
        CurrencyRateEntity::class,
        ServiceRequestEntity::class,
        EditorialContentEntity::class,
        MediaAssetEntity::class,
        AuditLogEntity::class,
        AppNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SomTrevelDatabase : RoomDatabase() {
    abstract fun dao(): SomTrevelDao

    companion object {
        @Volatile
        private var INSTANCE: SomTrevelDatabase? = null

        fun getInstance(context: Context): SomTrevelDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SomTrevelDatabase::class.java,
                    "som_trevel_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
