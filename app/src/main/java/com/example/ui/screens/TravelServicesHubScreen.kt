package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomTrevelViewModel

@Composable
fun TravelServicesHubScreen(
    vm: SomTrevelViewModel,
    initialTab: String = "FLIGHTS",
    initialQuery: String = ""
) {
    val lang by vm.language.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val rates by vm.currencyRates.collectAsState()
    val flights by vm.flights.collectAsState()
    val hotels by vm.hotels.collectAsState()
    val destinations by vm.destinations.collectAsState()
    val packages by vm.packages.collectAsState()
    val visas by vm.myVisaApplications.collectAsState()
    val currentUser by vm.currentUser.collectAsState()

    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    var searchQuery by remember(initialQuery) { mutableStateOf(initialQuery) }

    // Booking Modals
    var selectedFlightForBooking by remember { mutableStateOf<FlightOfferEntity?>(null) }
    var selectedHotelForBooking by remember { mutableStateOf<HotelEntity?>(null) }
    var selectedPackageForBooking by remember { mutableStateOf<TravelPackageEntity?>(null) }
    var selectedDestinationDetail by remember { mutableStateOf<DestinationEntity?>(null) }

    val tabs = listOf(
        "FLIGHTS" to L10n.text(lang, "Flights", "Duulimaad"),
        "HOTELS" to L10n.text(lang, "Hotels", "Huteelo"),
        "DESTINATIONS" to L10n.text(lang, "Destinations", "Magaalooyin"),
        "HAJJ" to "Hajj",
        "UMRAH" to "Umrah",
        "HOLIDAYS" to L10n.text(lang, "Holidays", "Fasaxyada"),
        "VISAS" to L10n.text(lang, "Visa Desk", "Dal-ku-gal")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable Service Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { (key, label) ->
                FilterChip(
                    selected = activeTab == key,
                    onClick = { activeTab = key },
                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("service_tab_$key")
                )
            }
        }

        if (activeTab != "VISAS") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search city, airport code, hotel, or package...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when (activeTab) {
            "FLIGHTS" -> FlightsCatalogSection(
                flights = flights,
                searchQuery = searchQuery,
                currencyCode = prefs.currencyCode,
                rates = rates,
                onBookFlight = { selectedFlightForBooking = it }
            )
            "HOTELS" -> HotelsCatalogSection(
                hotels = hotels,
                searchQuery = searchQuery,
                currencyCode = prefs.currencyCode,
                rates = rates,
                onReserveHotel = { selectedHotelForBooking = it }
            )
            "DESTINATIONS" -> DestinationsCatalogSection(
                destinations = destinations,
                searchQuery = searchQuery,
                currencyCode = prefs.currencyCode,
                rates = rates,
                onOpenDetail = { selectedDestinationDetail = it }
            )
            "HAJJ" -> PilgrimageCatalogSection(
                isHajj = true,
                packages = packages.filter { it.category == "HAJJ" },
                currencyCode = prefs.currencyCode,
                rates = rates,
                onRegister = { selectedPackageForBooking = it }
            )
            "UMRAH" -> PilgrimageCatalogSection(
                isHajj = false,
                packages = packages.filter { it.category == "UMRAH" },
                currencyCode = prefs.currencyCode,
                rates = rates,
                onRegister = { selectedPackageForBooking = it }
            )
            "HOLIDAYS" -> HolidayPackagesSection(
                packages = packages.filter { it.category == "HOLIDAY" },
                currencyCode = prefs.currencyCode,
                rates = rates,
                onBook = { selectedPackageForBooking = it }
            )
            "VISAS" -> VisaServicesSection(
                currentUser = currentUser,
                myVisas = visas,
                currencyCode = prefs.currencyCode,
                rates = rates,
                onSubmitVisa = { country, cat, pp, nat, uri ->
                    vm.submitVisaWithUri(country, cat, pp, nat, uri)
                }
            )
        }
    }

    // Flight Booking Modal
    selectedFlightForBooking?.let { flight ->
        FlightBookingDialog(
            flight = flight,
            currencyCode = prefs.currencyCode,
            rates = rates,
            onDismiss = { selectedFlightForBooking = null },
            onConfirm = { trip, cabin, depDate, retDate, adults, children, infants, notes, payNow ->
                vm.bookFlight(flight, trip, cabin, depDate, retDate, adults, children, infants, notes, payNow)
                selectedFlightForBooking = null
            }
        )
    }

    // Hotel Booking Modal
    selectedHotelForBooking?.let { hotel ->
        HotelBookingDialog(
            hotel = hotel,
            currencyCode = prefs.currencyCode,
            rates = rates,
            onDismiss = { selectedHotelForBooking = null },
            onConfirm = { roomCat, checkIn, checkOut, nights, guests ->
                vm.bookHotel(hotel, roomCat, checkIn, checkOut, nights, guests)
                selectedHotelForBooking = null
            }
        )
    }

    // Package / Hajj / Umrah Registration Modal
    selectedPackageForBooking?.let { pkg ->
        PackageBookingDialog(
            pkg = pkg,
            currencyCode = prefs.currencyCode,
            rates = rates,
            onDismiss = { selectedPackageForBooking = null },
            onConfirm = { count, names, passports, deposit ->
                vm.bookPackage(pkg, count, names, passports, deposit)
                selectedPackageForBooking = null
            }
        )
    }

    // Destination Detail Modal
    selectedDestinationDetail?.let { dest ->
        AlertDialog(
            onDismissRequest = { selectedDestinationDetail = null },
            title = { Text("${dest.city}, ${dest.country}", style = MaterialTheme.typography.headlineMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CoverArtworkImage(dest.coverKey, dest.customImageUri, dest.city, Modifier.fillMaxWidth().height(140.dp))
                    Text(dest.description)
                    Text("Top Attractions: ${dest.attractions}", fontWeight = FontWeight.SemiBold)
                    Text("Best Time to Visit: ${dest.bestTimeToVisit}")
                    Text("Travel Info: ${dest.travelInfo}", color = RoyalBlueLight)
                    Text("Packages Available: ${dest.availablePackagesCount} • Starting ${CurrencyFormatter.format(dest.startingPriceUsd, prefs.currencyCode, rates)}", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val city = dest.city
                    selectedDestinationDetail = null
                    searchQuery = city
                    activeTab = "FLIGHTS"
                }) { Text("Find Flights") }
            },
            dismissButton = {
                TextButton(onClick = { selectedDestinationDetail = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun FlightsCatalogSection(
    flights: List<FlightOfferEntity>,
    searchQuery: String,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onBookFlight: (FlightOfferEntity) -> Unit
) {
    var scopeFilter by remember { mutableStateOf("ALL") } // ALL, INTL, DOMESTIC
    val filtered = remember(flights, searchQuery, scopeFilter) {
        flights.filter { f ->
            val matchesScope = when (scopeFilter) {
                "INTL" -> !f.isDomestic
                "DOMESTIC" -> f.isDomestic
                else -> true
            }
            val q = searchQuery.trim()
            val matchesSearch = q.isBlank() || listOf(
                f.airlineName, f.flightNumber, f.originCode, f.originCity, f.destinationCode, f.destinationCity
            ).any { it.contains(q, ignoreCase = true) }
            matchesScope && matchesSearch
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = AmberContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Agency-Managed Inventory: Fares shown reflect SOM TREVEL's admin-configured demo schedules (not live external airline GDS feeds).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberPending,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = scopeFilter == "ALL", onClick = { scopeFilter = "ALL" }, label = { Text("All Flights") })
                FilterChip(selected = scopeFilter == "INTL", onClick = { scopeFilter = "INTL" }, label = { Text("International") })
                FilterChip(selected = scopeFilter == "DOMESTIC", onClick = { scopeFilter = "DOMESTIC" }, label = { Text("Domestic Somalia") })
            }
        }

        if (filtered.isEmpty()) {
            item { EmptyStateCard("No Matching Flights", "Try clearing your search filter or ask an admin to add this route.") }
        }

        items(filtered) { f ->
            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${f.airlineName} • ${f.flightNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LuxuryBadge(text = if (f.isDomestic) "DOMESTIC" else "INTERNATIONAL")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(f.originCode, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            Text(f.originCity, style = MaterialTheme.typography.bodyMedium)
                            Text(f.departureTime, style = MaterialTheme.typography.labelLarge)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Flight, contentDescription = null, tint = LuxuryGoldDark)
                            Text(f.durationLabel, style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(f.destinationCode, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            Text(f.destinationCity, style = MaterialTheme.typography.bodyMedium)
                            Text(f.arrivalTime, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Baggage: ${f.baggageAllowance} • ${f.seatsAvailable} seats open", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Economy from", style = MaterialTheme.typography.labelSmall)
                            Text(
                                CurrencyFormatter.format(f.economyPriceUsd, currencyCode, rates),
                                style = MaterialTheme.typography.titleLarge,
                                color = RoyalBlueLight
                            )
                        }
                        Button(
                            onClick = { onBookFlight(f) },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                            modifier = Modifier.testTag("book_flight_${f.id}")
                        ) {
                            Text("Select & Book")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelsCatalogSection(
    hotels: List<HotelEntity>,
    searchQuery: String,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onReserveHotel: (HotelEntity) -> Unit
) {
    val filtered = remember(hotels, searchQuery) {
        val q = searchQuery.trim()
        hotels.filter { q.isBlank() || it.name.contains(q, true) || it.city.contains(q, true) || it.country.contains(q, true) }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(filtered) { hotel ->
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(155.dp)) {
                        CoverArtworkImage(hotel.coverKey, hotel.customImageUri, hotel.name, Modifier.fillMaxSize())
                        LuxuryBadge(text = "${hotel.stars}-STAR LUXURY", modifier = Modifier.padding(12.dp))
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(hotel.name, style = MaterialTheme.typography.titleLarge)
                        Text("${hotel.city}, ${hotel.country}", style = MaterialTheme.typography.bodyMedium, color = RoyalBlueLight)
                        Spacer(Modifier.height(6.dp))
                        Text(hotel.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Rooms: ${hotel.roomCategoriesCsv}", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${CurrencyFormatter.format(hotel.pricePerNightUsd, currencyCode, rates)} / night",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { onReserveHotel(hotel) },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite)
                            ) { Text("Reserve Room") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationsCatalogSection(
    destinations: List<DestinationEntity>,
    searchQuery: String,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onOpenDetail: (DestinationEntity) -> Unit
) {
    var selectedRegion by remember { mutableStateOf("All") }
    val regions = listOf("All", "Horn of Africa", "Middle East", "East Africa", "Europe", "Southeast Asia", "North America")
    val filtered = remember(destinations, searchQuery, selectedRegion) {
        destinations.filter { d ->
            val matchesRegion = selectedRegion == "All" || d.region.equals(selectedRegion, true)
            val q = searchQuery.trim()
            val matchesQuery = q.isBlank() || d.city.contains(q, true) || d.country.contains(q, true)
            matchesRegion && matchesQuery
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                regions.forEach { r ->
                    FilterChip(selected = selectedRegion == r, onClick = { selectedRegion = r }, label = { Text(r) })
                }
            }
        }
        items(filtered) { dest ->
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().clickable { onOpenDetail(dest) }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CoverArtworkImage(
                        dest.coverKey, dest.customImageUri, dest.city,
                        Modifier.size(92.dp).background(DeepNavy, RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${dest.city}, ${dest.country}", style = MaterialTheme.typography.titleMedium)
                        Text(dest.region, style = MaterialTheme.typography.labelSmall, color = LuxuryGoldDark)
                        Spacer(Modifier.height(4.dp))
                        Text(dest.description, maxLines = 2, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("From ${CurrencyFormatter.format(dest.startingPriceUsd, currencyCode, rates)} • ${dest.availablePackagesCount} packages", style = MaterialTheme.typography.labelLarge, color = RoyalBlueLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun PilgrimageCatalogSection(
    isHajj: Boolean,
    packages: List<TravelPackageEntity>,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onRegister: (TravelPackageEntity) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = DeepNavy,
                border = BorderStroke(1.5.dp, LuxuryGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    LuxuryBadge(text = if (isHajj) "HAJJ 1448H / 2027" else "UMRAH YEAR-ROUND TIERS")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isHajj) "Sacred Hajj Delegation Management" else "Economy, Standard & VIP Umrah Journeys",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isHajj)
                            "Complete pilgrim group management, Mina/Arafat camps, Makkah & Madinah hotels, and scholar guidance."
                        else
                            "Choose Economy, Standard, or VIP Haram-view suites with Haramain Rail and guided Ziyarah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LuxuryGoldSoft
                    )
                }
            }
        }

        items(packages) { pkg ->
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        CoverArtworkImage(pkg.coverKey, pkg.customImageUri, pkg.title, Modifier.fillMaxSize())
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DeepNavy.copy(0.8f)))))
                        Row(
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LuxuryBadge(text = pkg.tier.uppercase())
                            LuxuryBadge(text = "${pkg.durationDays} DAYS")
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(pkg.title, style = MaterialTheme.typography.titleLarge)
                        Text("Departure: ${pkg.departureDate} • ${pkg.destinationLabel}", style = MaterialTheme.typography.labelLarge, color = RoyalBlueLight)
                        Spacer(Modifier.height(6.dp))
                        Text("Stay: ${pkg.accommodationDetails}", style = MaterialTheme.typography.bodyMedium)
                        Text("Transport: ${pkg.transportDetails}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Included: ${pkg.includedServices}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${CurrencyFormatter.format(pkg.priceUsd, currencyCode, rates)} / pilgrim",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { onRegister(pkg) },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = DeepNavy)
                            ) {
                                Text("Register Pilgrim", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HolidayPackagesSection(
    packages: List<TravelPackageEntity>,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onBook: (TravelPackageEntity) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(packages) { pkg ->
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    CoverArtworkImage(pkg.coverKey, pkg.customImageUri, pkg.title, Modifier.fillMaxWidth().height(150.dp))
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(pkg.title, style = MaterialTheme.typography.titleLarge)
                        Text("${pkg.destinationLabel} • ${pkg.durationDays} Days", style = MaterialTheme.typography.labelLarge, color = RoyalBlueLight)
                        Spacer(Modifier.height(6.dp))
                        Text(pkg.accommodationDetails, style = MaterialTheme.typography.bodyMedium)
                        Text(pkg.includedServices, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(CurrencyFormatter.format(pkg.priceUsd, currencyCode, rates), style = MaterialTheme.typography.titleLarge)
                            Button(onClick = { onBook(pkg) }) { Text("Book Holiday") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisaServicesSection(
    currentUser: UserEntity?,
    myVisas: List<VisaApplicationEntity>,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onSubmitVisa: (String, String, String, String, Uri?) -> Unit
) {
    var selectedCountry by remember { mutableStateOf("Turkey") }
    var visaCategory by remember { mutableStateOf("Tourist & Business e-Visa") }
    var passportNumber by remember(currentUser) { mutableStateOf(currentUser?.passportNumber ?: "") }
    var nationality by remember(currentUser) { mutableStateOf(currentUser?.nationality ?: "Somali") }
    var pickedDocUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) pickedDocUri = uri }
    )

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Visa Application Desk", style = MaterialTheme.typography.headlineMedium)
                    Text("Select destination country, upload passport copy via Android Photo Picker, and track approval status.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Turkey", "United Arab Emirates", "Saudi Arabia", "United Kingdom", "Schengen (France/Italy/Germany)", "United States", "Canada", "Malaysia").forEach { c ->
                            FilterChip(selected = selectedCountry == c, onClick = { selectedCountry = c }, label = { Text(c) })
                        }
                    }

                    OutlinedTextField(value = visaCategory, onValueChange = { visaCategory = it }, label = { Text("Visa Category") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = passportNumber, onValueChange = { passportNumber = it }, label = { Text("Passport Number") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = nationality, onValueChange = { nationality = it }, label = { Text("Nationality") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("visa_upload_doc_btn")
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (pickedDocUri == null) "Attach Passport Photo (Zero-Permission Picker)" else "Document Attached ✓")
                    }

                    Button(
                        onClick = { onSubmitVisa(selectedCountry, visaCategory, passportNumber, nationality, pickedDocUri) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("visa_submit_btn")
                    ) {
                        Text("Submit Visa Application")
                    }
                }
            }
        }

        item { Text("My Visa Tracking", style = MaterialTheme.typography.titleLarge) }
        if (myVisas.isEmpty()) {
            item { EmptyStateCard("No Visa Applications", if (currentUser == null) "Sign in on the Portal tab to submit and track visas." else "Your submitted visa applications will appear here.") }
        } else {
            items(myVisas) { v ->
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${v.destinationCountry} • ${v.referenceCode}", style = MaterialTheme.typography.titleMedium)
                            StatusPill(v.status)
                        }
                        Text("${v.visaCategory} • PP: ${v.passportNumber}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fee: ${CurrencyFormatter.format(v.feeUsd, currencyCode, rates)}", style = MaterialTheme.typography.labelLarge, color = RoyalBlueLight)
                        if (v.adminRemarks.isNotBlank()) {
                            Text("Desk Note: ${v.adminRemarks}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightBookingDialog(
    flight: FlightOfferEntity,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int, Int, Int, String, Boolean) -> Unit
) {
    var tripType by remember { mutableStateOf("ROUND_TRIP") }
    var cabin by remember { mutableStateOf("Economy") }
    var depDate by remember { mutableStateOf("2026-12-15") }
    var retDate by remember { mutableStateOf("2026-12-28") }
    var adults by remember { mutableIntStateOf(1) }
    var children by remember { mutableIntStateOf(0) }
    var infants by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var payNow by remember { mutableStateOf(true) }

    val unitPrice = when (cabin) {
        "Premium Economy" -> flight.premiumEconomyPriceUsd
        "Business" -> flight.businessPriceUsd
        "First Class" -> flight.firstClassPriceUsd
        else -> flight.economyPriceUsd
    }
    val mult = if (tripType == "ROUND_TRIP") 1.85 else if (tripType == "MULTI_CITY") 2.15 else 1.0
    val estimate = (adults * unitPrice + children * unitPrice * 0.75 + infants * unitPrice * 0.15) * mult

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book ${flight.airlineName} ${flight.flightNumber}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${flight.originCity} (${flight.originCode}) → ${flight.destinationCity} (${flight.destinationCode})", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ONE_WAY", "ROUND_TRIP", "MULTI_CITY").forEach { t ->
                        FilterChip(selected = tripType == t, onClick = { tripType = t }, label = { Text(t.replace("_", " ")) })
                    }
                }
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Economy", "Premium Economy", "Business", "First Class").forEach { c ->
                        FilterChip(selected = cabin == c, onClick = { cabin = c }, label = { Text(c) })
                    }
                }
                OutlinedTextField(value = depDate, onValueChange = { depDate = it }, label = { Text("Departure Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                if (tripType != "ONE_WAY") {
                    OutlinedTextField(value = retDate, onValueChange = { retDate = it }, label = { Text("Return / Next Leg Date") }, modifier = Modifier.fillMaxWidth())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Adults: $adults")
                    IconButton(onClick = { if (adults > 1) adults-- }) { Icon(Icons.Filled.Remove, null) }
                    IconButton(onClick = { adults++ }) { Icon(Icons.Filled.Add, null) }
                    Text("Child: $children")
                    IconButton(onClick = { if (children > 0) children-- }) { Icon(Icons.Filled.Remove, null) }
                    IconButton(onClick = { children++ }) { Icon(Icons.Filled.Add, null) }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Meal / Seat / Special Notes") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = payNow, onCheckedChange = { payNow = it })
                    Text("Demo Instant Payment & Issue E-Ticket Now")
                }
                Text("Estimated Total: ${CurrencyFormatter.format(estimate, currencyCode, rates)}", style = MaterialTheme.typography.titleMedium, color = RoyalBlueLight)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tripType, cabin, depDate, retDate, adults, children, infants, notes, payNow) }) {
                Text("Confirm Booking")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun HotelBookingDialog(
    hotel: HotelEntity,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Int) -> Unit
) {
    val rooms = remember(hotel) { hotel.roomCategoriesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
    var selectedRoom by remember { mutableStateOf(rooms.firstOrNull() ?: "Deluxe Suite") }
    var checkIn by remember { mutableStateOf("2026-12-15") }
    var checkOut by remember { mutableStateOf("2026-12-19") }
    var nights by remember { mutableIntStateOf(4) }
    var guests by remember { mutableIntStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reserve ${hotel.name}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rooms.forEach { r -> FilterChip(selected = selectedRoom == r, onClick = { selectedRoom = r }, label = { Text(r) }) }
                }
                OutlinedTextField(value = checkIn, onValueChange = { checkIn = it }, label = { Text("Check-In") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = checkOut, onValueChange = { checkOut = it }, label = { Text("Check-Out") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nights: $nights")
                    IconButton(onClick = { if (nights > 1) nights-- }) { Icon(Icons.Filled.Remove, null) }
                    IconButton(onClick = { nights++ }) { Icon(Icons.Filled.Add, null) }
                    Text("Guests: $guests")
                    IconButton(onClick = { if (guests > 1) guests-- }) { Icon(Icons.Filled.Remove, null) }
                    IconButton(onClick = { guests++ }) { Icon(Icons.Filled.Add, null) }
                }
                Text("Total: ${CurrencyFormatter.format(hotel.pricePerNightUsd * nights, currencyCode, rates)}", style = MaterialTheme.typography.titleMedium, color = RoyalBlueLight)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedRoom, checkIn, checkOut, nights, guests) }) { Text("Confirm Stay") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PackageBookingDialog(
    pkg: TravelPackageEntity,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String, Double) -> Unit
) {
    var count by remember { mutableIntStateOf(1) }
    var names by remember { mutableStateOf("") }
    var passports by remember { mutableStateOf("") }
    val total = pkg.priceUsd * count
    var depositStr by remember(total) { mutableStateOf(total.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register: ${pkg.title}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${pkg.category} • ${pkg.tier} • ${pkg.departureDate}", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Travelers / Pilgrims: $count")
                    IconButton(onClick = { if (count > 1) count-- }) { Icon(Icons.Filled.Remove, null) }
                    IconButton(onClick = { count++ }) { Icon(Icons.Filled.Add, null) }
                }
                OutlinedTextField(value = names, onValueChange = { names = it }, label = { Text("Full Names (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = passports, onValueChange = { passports = it }, label = { Text("Passport Numbers") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = depositStr, onValueChange = { depositStr = it }, label = { Text("Initial Payment (USD)") }, modifier = Modifier.fillMaxWidth())
                Text("Total Package: ${CurrencyFormatter.format(total, currencyCode, rates)}", style = MaterialTheme.typography.titleMedium, color = RoyalBlueLight)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(count, names, passports, depositStr.toDoubleOrNull() ?: total) }) {
                Text("Confirm Registration")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
