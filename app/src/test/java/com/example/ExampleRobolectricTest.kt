package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: SomTrevelDatabase
    private lateinit var repo: SomTrevelRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, SomTrevelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = SomTrevelRepository(context, db.dao(), UserPreferencesRepository(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `app name matches SOM TREVEL`() {
        assertEquals("SOM TREVEL", context.getString(R.string.app_name))
    }

    @Test
    fun `demo seeding, PNR verification, customer isolation, and safe demo purge`() = runTest {
        repo.ensureSeeded()

        // 1. PNR Verification works for seeded ticket ST884K + Warsame
        val verified = repo.verifyTicket("ST884K", "Warsame")
        assertNotNull(verified)
        assertEquals("TK 687", verified?.flightNumber)
        assertNull(repo.verifyTicket("ST884K", "WrongSurname"))

        // 2. Register real customer & verify strict isolation
        val regResult = repo.registerCustomer(
            fullName = "Hassan Ali",
            email = "hassan.real@example.com",
            phone = "+252611111111",
            password = "StrongPassword123",
            passportNumber = "SOM999111",
            nationality = "Somali"
        )
        assertTrue(regResult.isSuccess)
        val realCustomer = regResult.getOrThrow()

        // Real customer has 0 bookings initially (does not see Amina's demo bookings)
        assertEquals(0, repo.observeUserFlightBookings(realCustomer.id).first().size)

        // Book a real flight for Hassan
        val flight = repo.flightOffers.first().first()
        val realBooking = repo.bookFlight(
            user = realCustomer,
            flight = flight,
            tripType = "ONE_WAY",
            cabinClass = "Economy",
            departureDate = "2026-12-20",
            returnDate = "",
            adults = 1,
            children = 0,
            infants = 0,
            passengerNotes = "Window",
            payNow = true
        )
        assertEquals(1, repo.observeUserFlightBookings(realCustomer.id).first().size)

        // 3. Super Admin safely purges demo records -> real customer booking remains intact!
        val admin = db.dao().getUserByEmail("admin@somtrevel.com")!!
        repo.removeDemoDataSafely(admin)

        assertNull(repo.verifyTicket("ST884K", "Warsame"))
        val realBookingsAfterPurge = repo.observeUserFlightBookings(realCustomer.id).first()
        assertEquals(1, realBookingsAfterPurge.size)
        assertEquals(realBooking.pnr, realBookingsAfterPurge.first().pnr)
    }
}
