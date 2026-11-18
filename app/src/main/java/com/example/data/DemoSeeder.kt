package com.example.data

import kotlin.math.max

object DemoSeeder {
    suspend fun seedIfEmpty(dao: SomTrevelDao) {
        if (dao.countUsers() > 0) return

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        // 1. Currencies
        dao.insertCurrencyRates(
            listOf(
                CurrencyRateEntity("USD", "US Dollar", "$", 1.0),
                CurrencyRateEntity("EUR", "Euro", "€", 0.92),
                CurrencyRateEntity("SAR", "Saudi Riyal", "SAR ", 3.75),
                CurrencyRateEntity("SOS", "Somali Shilling", "SOS ", 570.0)
            )
        )

        // 2. Users (Demo Customer & Staff Profiles with SHA-256 salted hashes)
        val customerId = dao.insertUser(
            UserEntity(
                fullName = "Amina Hassan Warsame",
                email = "amina.warsame@demo.somtrevel.com",
                phone = "+252 61 548 9210",
                passwordHash = SecurityUtil.hashPassword("SomTrevelDemo2026!"),
                role = UserRole.CUSTOMER.name,
                passportNumber = "SOM8842190",
                nationality = "Somali",
                isDemo = true,
                createdAtMillis = now - 14 * dayMs
            )
        )
        val customer2Id = dao.insertUser(
            UserEntity(
                fullName = "Abdirahman Farah Duale",
                email = "abdirahman.duale@demo.somtrevel.com",
                phone = "+252 63 441 7822",
                passwordHash = SecurityUtil.hashPassword("SomTrevelDemo2026!"),
                role = UserRole.CUSTOMER.name,
                passportNumber = "SOM5539104",
                nationality = "Somali",
                isDemo = true,
                createdAtMillis = now - 9 * dayMs
            )
        )
        dao.insertUser(
            UserEntity(
                fullName = "Zakariye Salaad (Super Admin)",
                email = "admin@somtrevel.com",
                phone = "+252 61 900 1000",
                passwordHash = SecurityUtil.hashPassword("AdminSetup#2026"),
                role = UserRole.SUPER_ADMIN.name,
                passportNumber = "STAFF-001",
                nationality = "Somali",
                isDemo = true,
                createdAtMillis = now - 30 * dayMs
            )
        )
        dao.insertUser(
            UserEntity(
                fullName = "Hodan Yusuf Ali (Accountant)",
                email = "accounts@somtrevel.com",
                phone = "+252 61 900 2000",
                passwordHash = SecurityUtil.hashPassword("Accounts#2026"),
                role = UserRole.ACCOUNTANT.name,
                passportNumber = "STAFF-002",
                nationality = "Somali",
                isDemo = true,
                createdAtMillis = now - 25 * dayMs
            )
        )
        dao.insertUser(
            UserEntity(
                fullName = "Khalid Omar Jama (Booking Mgr)",
                email = "bookings@somtrevel.com",
                phone = "+252 61 900 3000",
                passwordHash = SecurityUtil.hashPassword("Bookings#2026"),
                role = UserRole.BOOKING_MANAGER.name,
                passportNumber = "STAFF-003",
                nationality = "Somali",
                isDemo = true,
                createdAtMillis = now - 22 * dayMs
            )
        )

        // 3. Worldwide + Somali Destinations (19 Destinations)
        dao.insertDestinations(
            listOf(
                DestinationEntity(
                    city = "Mogadishu", country = "Somalia", region = "Horn of Africa", isSomali = true,
                    description = "Pearl of the Indian Ocean featuring historic whitewashed coral-stone quarters, golden Lido Beach sunsets, and vibrant culinary waterfronts.",
                    attractions = "Lido Beach, Shangani Old Town, Peace Garden, Gezira Coastline",
                    bestTimeToVisit = "November – March",
                    travelInfo = "Aden Adde International Airport (MGQ) • Airport VIP lounge & armored transfers available",
                    startingPriceUsd = 290.0, availablePackagesCount = 4, coverKey = "mogadishu", isDemo = true
                ),
                DestinationEntity(
                    city = "Hargeisa", country = "Somalia", region = "Horn of Africa", isSomali = true,
                    description = "Highland cultural capital famed for ancient Laas Geel Neolithic rock art, crisp mountain evenings, and dynamic hospitality.",
                    attractions = "Laas Geel Caves, Naasa Hablood Twin Hills, Central Cultural Souq",
                    bestTimeToVisit = "October – February",
                    travelInfo = "Egal International Airport (HGA) • Guided archaeological excursions",
                    startingPriceUsd = 210.0, availablePackagesCount = 3, coverKey = "mogadishu", isDemo = true
                ),
                DestinationEntity(
                    city = "Berbera", country = "Somalia", region = "Horn of Africa", isSomali = true,
                    description = "Red Sea maritime jewel with Ottoman-era waterfront heritage, untouched coral reefs, and serene turquoise bays.",
                    attractions = "Baathela Beach, Historic Port Quarter, Red Sea Diving Reefs",
                    bestTimeToVisit = "November – March",
                    travelInfo = "Berbera International Airport (BBO) • Coastal resort transfers",
                    startingPriceUsd = 240.0, availablePackagesCount = 2, coverKey = "mogadishu", isDemo = true
                ),
                DestinationEntity(
                    city = "Makkah", country = "Saudi Arabia", region = "Middle East", isSomali = false,
                    description = "Sacred heart of Islam, curated with Haram-facing 5-star suites, private Mutawwif scholars, and seamless Haramain rail access.",
                    attractions = "Masjid al-Haram, Jabal al-Nour, Abraj Al Bait Clock Museum",
                    bestTimeToVisit = "Year-round Umrah & Dhul Hijjah Hajj",
                    travelInfo = "Via Jeddah (JED) • VIP GMC & Haramain High-Speed Rail",
                    startingPriceUsd = 1450.0, availablePackagesCount = 6, coverKey = "makkah", isDemo = true
                ),
                DestinationEntity(
                    city = "Madinah", country = "Saudi Arabia", region = "Middle East", isSomali = false,
                    description = "The illuminated city of tranquility with footsteps-away hotels overlooking Al-Masjid an-Nabawi and guided Ziyarah tours.",
                    attractions = "Al-Masjid an-Nabawi, Quba Mosque, Mount Uhud, Dates Garden",
                    bestTimeToVisit = "October – April",
                    travelInfo = "Prince Mohammad Bin Abdulaziz Airport (MED)",
                    startingPriceUsd = 1350.0, availablePackagesCount = 5, coverKey = "makkah", isDemo = true
                ),
                DestinationEntity(
                    city = "Dubai", country = "United Arab Emirates", region = "Middle East", isSomali = false,
                    description = "Ultra-modern luxury skyline, desert yacht experiences, Palm Jumeirah resorts, and premier shopping.",
                    attractions = "Burj Khalifa, Palm Jumeirah, Museum of the Future, Desert Safari",
                    bestTimeToVisit = "November – April",
                    travelInfo = "DXB Terminal 3 • Chauffeur-driven transfers & 30/60-day UAE visas",
                    startingPriceUsd = 680.0, availablePackagesCount = 5, coverKey = "hotel", isDemo = true
                ),
                DestinationEntity(
                    city = "Istanbul", country = "Turkey", region = "Eurasia", isSomali = false,
                    description = "Imperial crossroads of Europe and Asia along the Bosphorus with Ottoman palaces, private cruises, and Michelin dining.",
                    attractions = "Hagia Sophia, Topkapi Palace, Bosphorus Private Yacht, Cappadocia",
                    bestTimeToVisit = "April – June, September – November",
                    travelInfo = "Istanbul Airport (IST) • Turkish e-Visa & medical/business travel support",
                    startingPriceUsd = 790.0, availablePackagesCount = 4, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Doha", country = "Qatar", region = "Middle East", isSomali = false,
                    description = "Architectural elegance on the Arabian Gulf featuring Souq Waqif, Katara Cultural Village, and Qatar Airways Qsuite hubs.",
                    attractions = "Museum of Islamic Art, Souq Waqif, The Pearl-Qatar",
                    bestTimeToVisit = "November – March",
                    travelInfo = "Hamad International Airport (DOH) • Transit luxury stopovers",
                    startingPriceUsd = 720.0, availablePackagesCount = 3, coverKey = "hotel", isDemo = true
                ),
                DestinationEntity(
                    city = "Nairobi", country = "Kenya", region = "East Africa", isSomali = false,
                    description = "East Africa's diplomatic and safari gateway combining Karen luxury villas with Nairobi National Park wildlife.",
                    attractions = "Nairobi National Park, Giraffe Manor, Karen Blixen, Maasai Mara link",
                    bestTimeToVisit = "July – October, January – February",
                    travelInfo = "Jomo Kenyatta International (NBO) • Direct MGQ/HGA connections",
                    startingPriceUsd = 340.0, availablePackagesCount = 3, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Addis Ababa", country = "Ethiopia", region = "East Africa", isSomali = false,
                    description = "Diplomatic capital of Africa perched in lush highlands with coffee heritage and Bole hub connectivity.",
                    attractions = "Unity Park, Entoto Hills, National Museum, Tomoca Coffee",
                    bestTimeToVisit = "October – May",
                    travelInfo = "Bole International Airport (ADD) • Regional & intercontinental hub",
                    startingPriceUsd = 310.0, availablePackagesCount = 2, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Djibouti City", country = "Djibouti", region = "Horn of Africa", isSomali = false,
                    description = "Dramatic volcanic lakes, whale shark diving in the Gulf of Tadjoura, and Red Sea luxury coastal retreats.",
                    attractions = "Lake Assal, Moucha Island, Day Forest National Park",
                    bestTimeToVisit = "November – March",
                    travelInfo = "Ambouli International (JIB) • Direct regional flights",
                    startingPriceUsd = 295.0, availablePackagesCount = 2, coverKey = "mogadishu", isDemo = true
                ),
                DestinationEntity(
                    city = "Cairo", country = "Egypt", region = "North Africa", isSomali = false,
                    description = "Timeless pharaonic wonders, Nile sunset felucca cruises, and Grand Egyptian Museum private tours.",
                    attractions = "Giza Pyramids, Grand Egyptian Museum, Khan El-Khalili, Nile Cruise",
                    bestTimeToVisit = "October – April",
                    travelInfo = "Cairo International (CAI) • Visa-on-arrival / embassy coordination",
                    startingPriceUsd = 590.0, availablePackagesCount = 3, coverKey = "hotel", isDemo = true
                ),
                DestinationEntity(
                    city = "Kuala Lumpur", country = "Malaysia", region = "Southeast Asia", isSomali = false,
                    description = "Halal-friendly tropical metropolis pairing Petronas Towers luxury with Langkawi island escapes.",
                    attractions = "Petronas Towers, Batu Caves, Islamic Arts Museum, Langkawi",
                    bestTimeToVisit = "May – July, December – February",
                    travelInfo = "Kuala Lumpur International (KUL) • Education & family packages",
                    startingPriceUsd = 980.0, availablePackagesCount = 3, coverKey = "hotel", isDemo = true
                ),
                DestinationEntity(
                    city = "Bali", country = "Indonesia", region = "Southeast Asia", isSomali = false,
                    description = "Private cliffside pool villas, emerald rice terraces, and serene Indian Ocean retreats.",
                    attractions = "Uluwatu Cliffs, Ubud Sanctuary, Nusa Penida",
                    bestTimeToVisit = "April – October",
                    travelInfo = "Ngurah Rai International (DPS)",
                    startingPriceUsd = 1150.0, availablePackagesCount = 2, coverKey = "hotel", isDemo = true
                ),
                DestinationEntity(
                    city = "Malé Atolls", country = "Maldives", region = "Indian Ocean", isSomali = false,
                    description = "Overwater sanctuary villas surrounded by crystal lagoons, seaplane transfers, and bespoke honeymoon hospitality.",
                    attractions = "Private Overwater Villas, Coral Reef Snorkeling, Sandbank Dining",
                    bestTimeToVisit = "November – April",
                    travelInfo = "Velana International (MLE) • Seaplane & speedboat transfers",
                    startingPriceUsd = 1890.0, availablePackagesCount = 3, coverKey = "hotel", isDemo = true
                ),
                DestinationEntity(
                    city = "London", country = "United Kingdom", region = "Europe", isSomali = false,
                    description = "Mayfair heritage suites, West End culture, and direct diaspora connectivity.",
                    attractions = "Buckingham Palace, British Museum, Knightsbridge, Hyde Park",
                    bestTimeToVisit = "May – September",
                    travelInfo = "Heathrow (LHR) • UK Visa application dossier assistance",
                    startingPriceUsd = 1090.0, availablePackagesCount = 3, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Paris", country = "France", region = "Europe", isSomali = false,
                    description = "Haute couture avenues, Seine river private cruises, and iconic Parisian palatial hotels.",
                    attractions = "Eiffel Tower, Louvre Museum, Champs-Élysées",
                    bestTimeToVisit = "April – June, September – October",
                    travelInfo = "Charles de Gaulle (CDG) • Schengen visa file support",
                    startingPriceUsd = 1120.0, availablePackagesCount = 2, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Rome & Milan", country = "Italy", region = "Europe", isSomali = false,
                    description = "Renaissance architecture, Mediterranean Amalfi vistas, and Milanese luxury fashion.",
                    attractions = "Colosseum, Amalfi Coast, Duomo di Milano",
                    bestTimeToVisit = "April – June, September – October",
                    travelInfo = "Fiumicino (FCO) • Schengen multi-city itineraries",
                    startingPriceUsd = 1060.0, availablePackagesCount = 2, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Frankfurt & Munich", country = "Germany", region = "Europe", isSomali = false,
                    description = "Bavarian alpine castles, executive business corridors, and medical concierge travel.",
                    attractions = "Neuschwanstein Castle, Marienplatz, Rhine Valley",
                    bestTimeToVisit = "May – September",
                    travelInfo = "Frankfurt (FRA) • Business & medical visa logistics",
                    startingPriceUsd = 990.0, availablePackagesCount = 2, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Washington D.C. & Minneapolis", country = "United States", region = "North America", isSomali = false,
                    description = "Transatlantic family reunions, diplomatic travel, and executive Atlantic crossings.",
                    attractions = "National Mall, Georgetown, Twin Cities Cultural Centers",
                    bestTimeToVisit = "April – October",
                    travelInfo = "Dulles (IAD) & MSP • Full US B1/B2 interview prep",
                    startingPriceUsd = 1380.0, availablePackagesCount = 2, coverKey = "aviation", isDemo = true
                ),
                DestinationEntity(
                    city = "Toronto", country = "Canada", region = "North America", isSomali = false,
                    description = "Cosmopolitan lakefront towers, Niagara excursions, and family settlement corridors.",
                    attractions = "CN Tower, Niagara Falls, Yorkville",
                    bestTimeToVisit = "May – October",
                    travelInfo = "Toronto Pearson (YYZ) • Canada visitor visa guidance",
                    startingPriceUsd = 1340.0, availablePackagesCount = 2, coverKey = "aviation", isDemo = true
                )
            )
        )

        // 4. Flight Offers (Domestic & International)
        dao.insertFlightOffers(
            listOf(
                FlightOfferEntity(
                    airlineName = "Turkish Airlines", airlineCode = "TK", flightNumber = "TK 687",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "IST", destinationCity = "Istanbul",
                    isDomestic = false, departureTime = "10:15", arrivalTime = "17:45", durationLabel = "7h 30m",
                    baggageAllowance = "2 x 23kg Checked + 8kg Cabin",
                    economyPriceUsd = 790.0, premiumEconomyPriceUsd = 1120.0, businessPriceUsd = 2250.0, firstClassPriceUsd = 3400.0,
                    seatsAvailable = 24, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Qatar Airways", airlineCode = "QR", flightNumber = "QR 1462",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "DOH", destinationCity = "Doha",
                    isDomestic = false, departureTime = "14:20", arrivalTime = "19:50", durationLabel = "5h 30m",
                    baggageAllowance = "35kg Checked + 7kg Cabin",
                    economyPriceUsd = 740.0, premiumEconomyPriceUsd = 1090.0, businessPriceUsd = 2680.0, firstClassPriceUsd = 4100.0,
                    seatsAvailable = 18, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Emirates", airlineCode = "EK", flightNumber = "EK 724",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "DXB", destinationCity = "Dubai",
                    isDomestic = false, departureTime = "08:40", arrivalTime = "14:05", durationLabel = "5h 25m",
                    baggageAllowance = "30kg Checked + 7kg Cabin",
                    economyPriceUsd = 680.0, premiumEconomyPriceUsd = 990.0, businessPriceUsd = 2490.0, firstClassPriceUsd = 4450.0,
                    seatsAvailable = 16, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Saudia", airlineCode = "SV", flightNumber = "SV 453",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "JED", destinationCity = "Jeddah (Makkah)",
                    isDomestic = false, departureTime = "06:30", arrivalTime = "10:45", durationLabel = "4h 15m",
                    baggageAllowance = "2 x 23kg + Zamzam 5L",
                    economyPriceUsd = 560.0, premiumEconomyPriceUsd = 840.0, businessPriceUsd = 1790.0, firstClassPriceUsd = 2850.0,
                    seatsAvailable = 32, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Ethiopian Airlines", airlineCode = "ET", flightNumber = "ET 377",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "LHR", destinationCity = "London Heathrow",
                    isDomestic = false, departureTime = "11:10", arrivalTime = "21:35", durationLabel = "10h 25m (1 stop ADD)",
                    baggageAllowance = "2 x 23kg Checked + 7kg Cabin",
                    economyPriceUsd = 1090.0, premiumEconomyPriceUsd = 1480.0, businessPriceUsd = 2990.0, firstClassPriceUsd = 4200.0,
                    seatsAvailable = 14, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Daallo Airlines", airlineCode = "D3", flightNumber = "D3 128",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "HGA", destinationCity = "Hargeisa",
                    isDomestic = true, departureTime = "07:30", arrivalTime = "09:10", durationLabel = "1h 40m",
                    baggageAllowance = "25kg Checked + 7kg Cabin",
                    economyPriceUsd = 165.0, premiumEconomyPriceUsd = 225.0, businessPriceUsd = 340.0, firstClassPriceUsd = 450.0,
                    seatsAvailable = 38, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Freedom Airline Express", airlineCode = "FP", flightNumber = "FP 402",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "KMX", destinationCity = "Kismayo",
                    isDomestic = true, departureTime = "09:45", arrivalTime = "10:55", durationLabel = "1h 10m",
                    baggageAllowance = "20kg Checked + 7kg Cabin",
                    economyPriceUsd = 145.0, premiumEconomyPriceUsd = 195.0, businessPriceUsd = 290.0, firstClassPriceUsd = 380.0,
                    seatsAvailable = 28, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Daallo Airlines", airlineCode = "D3", flightNumber = "D3 214",
                    originCode = "HGA", originCity = "Hargeisa", destinationCode = "JIB", destinationCity = "Djibouti",
                    isDomestic = false, departureTime = "13:00", arrivalTime = "13:55", durationLabel = "55m",
                    baggageAllowance = "25kg Checked + 7kg Cabin",
                    economyPriceUsd = 190.0, premiumEconomyPriceUsd = 250.0, businessPriceUsd = 380.0, firstClassPriceUsd = 490.0,
                    seatsAvailable = 22, isDemo = true
                ),
                FlightOfferEntity(
                    airlineName = "Jubba Airways", airlineCode = "3J", flightNumber = "3J 305",
                    originCode = "MGQ", originCity = "Mogadishu", destinationCode = "NBO", destinationCity = "Nairobi",
                    isDomestic = false, departureTime = "15:15", arrivalTime = "17:20", durationLabel = "2h 05m",
                    baggageAllowance = "30kg Checked + 7kg Cabin",
                    economyPriceUsd = 285.0, premiumEconomyPriceUsd = 390.0, businessPriceUsd = 640.0, firstClassPriceUsd = 890.0,
                    seatsAvailable = 19, isDemo = true
                )
            )
        )

        // 5. Hotels
        dao.insertHotels(
            listOf(
                HotelEntity(
                    name = "Jazeera Palace Hotel", city = "Mogadishu", country = "Somalia", stars = 5,
                    description = "Diplomatic beachfront landmark beside Aden Adde Airport with executive suites, conference halls, and Indian Ocean vistas.",
                    roomCategoriesCsv = "Deluxe Ocean View,Executive Diplomatic Suite,Presidential Villa",
                    pricePerNightUsd = 240.0, roomsAvailable = 18, coverKey = "mogadishu", isDemo = true
                ),
                HotelEntity(
                    name = "Fairmont Makkah Clock Royal Tower", city = "Makkah", country = "Saudi Arabia", stars = 5,
                    description = "Iconic sanctuary steps from Masjid al-Haram offering direct Kaaba view suites and dedicated 24/7 pilgrim concierge.",
                    roomCategoriesCsv = "Fairmont Gold Haram View,Kaaba View Suite,Royal Pilgrim Residence",
                    pricePerNightUsd = 480.0, roomsAvailable = 12, coverKey = "makkah", isDemo = true
                ),
                HotelEntity(
                    name = "Pullman Zamzam Madinah", city = "Madinah", country = "Saudi Arabia", stars = 5,
                    description = "Tranquil 5-star sanctuary a 3-minute walk from Bab Al Salam Gate of Al-Masjid an-Nabawi.",
                    roomCategoriesCsv = "Superior Twin,Haram View Executive,Family Deluxe Suite",
                    pricePerNightUsd = 310.0, roomsAvailable = 21, coverKey = "makkah", isDemo = true
                ),
                HotelEntity(
                    name = "Atlantis The Royal Resort", city = "Dubai", country = "United Arab Emirates", stars = 5,
                    description = "Ultra-luxury Palm Jumeirah destination featuring infinity sky pools, private beach cabanas, and Michelin dining.",
                    roomCategoriesCsv = "Seascape King,Sky Pool Villa,Royal Club Suite",
                    pricePerNightUsd = 690.0, roomsAvailable = 9, coverKey = "hotel", isDemo = true
                ),
                HotelEntity(
                    name = "Damal Luxury Hotel", city = "Hargeisa", country = "Somalia", stars = 4,
                    description = "Refined highland business and leisure hotel in central Hargeisa with gourmet Somali-fusion dining.",
                    roomCategoriesCsv = "Standard Executive,Junior Highland Suite,VIP Corner Suite",
                    pricePerNightUsd = 165.0, roomsAvailable = 26, coverKey = "hotel", isDemo = true
                ),
                HotelEntity(
                    name = "Çırağan Palace Kempinski", city = "Istanbul", country = "Turkey", stars = 5,
                    description = "Restored 19th-century Ottoman palace directly on the Bosphorus strait with heated infinity pool.",
                    roomCategoriesCsv = "Bosphorus Deluxe,Palace Garden Suite,Sultan Suite",
                    pricePerNightUsd = 620.0, roomsAvailable = 7, coverKey = "hotel", isDemo = true
                )
            )
        )

        // 6. Holiday, Hajj & Umrah Packages
        dao.insertTravelPackages(
            listOf(
                TravelPackageEntity(
                    title = "Hajj Al-Noor Royal Sanctuary", category = "HAJJ", tier = "VIP",
                    destinationLabel = "Makkah, Mina VIP & Madinah", durationDays = 18,
                    departureDate = "2027-05-10",
                    accommodationDetails = "Fairmont Clock Tower (Makkah) • Oberoi Madinah • Mina Al-Muaisim VIP Glass Tents",
                    transportDetails = "Direct Business/Charter Flight + Haramain High-Speed Rail + Private GMC Fleet",
                    includedServices = "Hajj Visa Processing, Senior Somali/English Scholars, Full Buffet Dining, 24/7 Medical Team, Ihram Kit",
                    priceUsd = 9850.0, seatsAvailable = 14, coverKey = "makkah", isDemo = true
                ),
                TravelPackageEntity(
                    title = "Hajj Al-Safaa Executive", category = "HAJJ", tier = "Standard",
                    destinationLabel = "Makkah & Madinah", durationDays = 21,
                    departureDate = "2027-05-08",
                    accommodationDetails = "Swissotel Al Maqam (Makkah) • Anwar Al Madinah Movenpick • Upgraded Mina Camp",
                    transportDetails = "Return International Flights + Luxury Air-Conditioned Coaches",
                    includedServices = "Official Nusuk Hajj Visa, Guided Manasik Seminars, Daily Meals, Ziyarah Historical Tours",
                    priceUsd = 6950.0, seatsAvailable = 28, coverKey = "makkah", isDemo = true
                ),
                TravelPackageEntity(
                    title = "Umrah VIP Haram-View Retreat", category = "UMRAH", tier = "VIP",
                    destinationLabel = "Makkah (5N) & Madinah (5N)", durationDays = 10,
                    departureDate = "2026-12-18",
                    accommodationDetails = "Raffles Makkah Palace (Kaaba View) • Hilton Madinah",
                    transportDetails = "Saudia Airlines + Haramain High-Speed Rail First Class + Private SUV Transfers",
                    includedServices = "Umrah Visa, Private Guided Tawaf & Sa'i, Rawdah Permit Coordination, Breakfast & Dinner",
                    priceUsd = 3250.0, seatsAvailable = 16, coverKey = "makkah", isDemo = true
                ),
                TravelPackageEntity(
                    title = "Umrah Standard Family Journey", category = "UMRAH", tier = "Standard",
                    destinationLabel = "Makkah (5N) & Madinah (4N)", durationDays = 9,
                    departureDate = "2026-12-22",
                    accommodationDetails = "Jabal Omar Hyatt Regency • Pullman Zamzam Madinah",
                    transportDetails = "Return Flights + Luxury Group Coaches + Haramain Rail",
                    includedServices = "Umrah Visa, Group Scholar Guidance, Historical Ziyarah, Zamzam 5L Allocation",
                    priceUsd = 1980.0, seatsAvailable = 34, coverKey = "makkah", isDemo = true
                ),
                TravelPackageEntity(
                    title = "Umrah Economy Group Spiritual", category = "UMRAH", tier = "Economy",
                    destinationLabel = "Makkah (7N) & Madinah (5N)", durationDays = 12,
                    departureDate = "2027-01-05",
                    accommodationDetails = "Voco Makkah (Shuttle 24/7) • Emaar Royal Madinah",
                    transportDetails = "Return Group Flights + Dedicated Coach Transfers",
                    includedServices = "Umrah Visa, Group Leader Assistance, Orientation Seminar",
                    priceUsd = 1390.0, seatsAvailable = 45, coverKey = "makkah", isDemo = true
                ),
                TravelPackageEntity(
                    title = "Somali Riviera & Berbera Reefs", category = "HOLIDAY", tier = "VIP",
                    destinationLabel = "Mogadishu, Hargeisa & Berbera", durationDays = 7,
                    departureDate = "2026-12-12",
                    accommodationDetails = "Jazeera Palace Mogadishu • Damal Hargeisa • Red Sea Beach Villa",
                    transportDetails = "Domestic Flights + Private SUV Chauffeur",
                    includedServices = "Laas Geel Heritage Pass, Sunset Dhow Cruise, Full-Board Coastal Dining",
                    priceUsd = 1640.0, seatsAvailable = 12, coverKey = "mogadishu", isDemo = true
                ),
                TravelPackageEntity(
                    title = "Dubai & Maldives Dual Luxury", category = "HOLIDAY", tier = "VIP",
                    destinationLabel = "Dubai (3N) + Maldives Overwater (4N)", durationDays = 7,
                    departureDate = "2027-01-15",
                    accommodationDetails = "Atlantis The Royal Dubai • Soneva / Waldorf Overwater Villa",
                    transportDetails = "Emirates Flights + Seaplane Island Transfer",
                    includedServices = "UAE Visa, Desert Yacht Dinner, Private Sandbank Breakfast",
                    priceUsd = 3890.0, seatsAvailable = 8, coverKey = "hotel", isDemo = true
                )
            )
        )

        // 7. Sample Flight Bookings & Issued E-Tickets
        dao.insertFlightBooking(
            FlightBookingEntity(
                pnr = "ST884K", userId = customerId,
                customerName = "Amina Hassan Warsame", customerSurname = "Warsame",
                flightId = 1, airlineName = "Turkish Airlines", flightNumber = "TK 687",
                originCode = "MGQ", originCity = "Mogadishu", destinationCode = "IST", destinationCity = "Istanbul",
                departureDate = "2026-12-04", returnDate = "2026-12-19",
                departureTime = "10:15", arrivalTime = "17:45",
                tripType = "ROUND_TRIP", cabinClass = "Business",
                adultCount = 1, childCount = 0, infantCount = 0,
                passengerSummary = "1 Adult (Amina Hassan Warsame, PP: SOM8842190)",
                baggageAllowance = "2 x 32kg Checked + Lounge",
                totalUsd = 2250.0, paymentStatus = "PAID", ticketStatus = "Issued",
                notes = "DEMO Booking • Halal Meal & Window Seat 4A", isDemo = true, createdAtMillis = now - 6 * dayMs
            )
        )
        dao.insertFlightBooking(
            FlightBookingEntity(
                pnr = "ST419M", userId = customerId,
                customerName = "Amina Hassan Warsame", customerSurname = "Warsame",
                flightId = 6, airlineName = "Daallo Airlines", flightNumber = "D3 128",
                originCode = "MGQ", originCity = "Mogadishu", destinationCode = "HGA", destinationCity = "Hargeisa",
                departureDate = "2026-11-28", returnDate = "",
                departureTime = "07:30", arrivalTime = "09:10",
                tripType = "ONE_WAY", cabinClass = "Economy",
                adultCount = 2, childCount = 0, infantCount = 0,
                passengerSummary = "2 Adults (Warsame Family)",
                baggageAllowance = "25kg Checked per pax",
                totalUsd = 330.0, paymentStatus = "PAID", ticketStatus = "Confirmed",
                notes = "DEMO Domestic Flight", isDemo = true, createdAtMillis = now - 3 * dayMs
            )
        )
        dao.insertFlightBooking(
            FlightBookingEntity(
                pnr = "ST772D", userId = customer2Id,
                customerName = "Abdirahman Farah Duale", customerSurname = "Duale",
                flightId = 3, airlineName = "Emirates", flightNumber = "EK 724",
                originCode = "MGQ", originCity = "Mogadishu", destinationCode = "DXB", destinationCity = "Dubai",
                departureDate = "2026-12-10", returnDate = "2026-12-18",
                departureTime = "08:40", arrivalTime = "14:05",
                tripType = "ROUND_TRIP", cabinClass = "Economy",
                adultCount = 1, childCount = 1, infantCount = 0,
                passengerSummary = "1 Adult, 1 Child (Duale Family)",
                baggageAllowance = "30kg Checked",
                    totalUsd = 1190.0, paymentStatus = "PENDING", ticketStatus = "Pending",
                notes = "DEMO Awaiting Bank Transfer Confirmation", isDemo = true, createdAtMillis = now - 1 * dayMs
            )
        )

        // 8. Sample Hotel Bookings
        dao.insertHotelBooking(
            HotelBookingEntity(
                referenceCode = "HTL-50412", userId = customerId, customerName = "Amina Hassan Warsame",
                hotelId = 6, hotelName = "Çırağan Palace Kempinski", cityCountry = "Istanbul, Turkey",
                roomCategory = "Bosphorus Deluxe", checkInDate = "2026-12-04", checkOutDate = "2026-12-08",
                nights = 4, guestsCount = 2, totalUsd = 2480.0,
                paymentStatus = "PAID", status = "Confirmed", isDemo = true, createdAtMillis = now - 5 * dayMs
            )
        )

        // 9. Sample Hajj / Umrah Pilgrim Registrations
        dao.insertPackageBooking(
            PackageBookingEntity(
                referenceCode = "UMR-30198", userId = customerId, customerName = "Amina Hassan Warsame",
                packageId = 3, packageTitle = "Umrah VIP Haram-View Retreat", category = "UMRAH",
                tier = "VIP", groupCode = "GRP-UMR-DEC01", travelerCount = 2,
                travelerNames = "Amina Hassan Warsame, Halima Hassan Warsame",
                passportNumbers = "SOM8842190, SOM8842195",
                totalUsd = 6500.0, paidUsd = 4500.0, status = "Confirmed",
                isDemo = true, createdAtMillis = now - 4 * dayMs
            )
        )
        dao.insertPackageBooking(
            PackageBookingEntity(
                referenceCode = "HJJ-90211", userId = customer2Id, customerName = "Abdirahman Farah Duale",
                packageId = 1, packageTitle = "Hajj Al-Noor Royal Sanctuary", category = "HAJJ",
                tier = "VIP", groupCode = "GRP-HJJ-2027A", travelerCount = 1,
                travelerNames = "Abdirahman Farah Duale",
                passportNumbers = "SOM5539104",
                totalUsd = 9850.0, paidUsd = 5000.0, status = "Pending",
                isDemo = true, createdAtMillis = now - 2 * dayMs
            )
        )

        // 10. Sample Visa Applications
        dao.insertVisaApplication(
            VisaApplicationEntity(
                referenceCode = "VSA-71820", userId = customerId, applicantName = "Amina Hassan Warsame",
                destinationCountry = "Turkey", visaCategory = "Tourist & Business e-Visa",
                passportNumber = "SOM8842190", nationality = "Somali",
                requiredDocumentsSummary = "Passport bio page, 2 biometric photos, hotel voucher, return flight",
                status = "Approved", feeUsd = 140.0,
                adminRemarks = "DEMO: Approved & issued in PDF document center",
                isDemo = true, createdAtMillis = now - 7 * dayMs
            )
        )
        dao.insertVisaApplication(
            VisaApplicationEntity(
                referenceCode = "VSA-71894", userId = customer2Id, applicantName = "Abdirahman Farah Duale",
                destinationCountry = "United Arab Emirates", visaCategory = "30-Day Tourist Visa",
                passportNumber = "SOM5539104", nationality = "Somali",
                requiredDocumentsSummary = "Passport copy (6+ mo validity), white background photo",
                status = "Under Review", feeUsd = 165.0,
                adminRemarks = "DEMO: Submitted to immigration desk",
                isDemo = true, createdAtMillis = now - 1 * dayMs
            )
        )

        // 11. Accounting & Financial Transactions (Clearly marked DEMO)
        dao.insertTransactions(
            listOf(
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-101", type = "INCOME", category = "Ticket Sales",
                    description = "DEMO: TK 687 Business Ticket (PNR ST884K)", amountUsd = 2250.0,
                    linkedBookingRef = "ST884K", isDemo = true, createdAtMillis = now - 6 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-102", type = "INCOME", category = "Hotel Income",
                    description = "DEMO: Çırağan Palace 4 Nights (HTL-50412)", amountUsd = 2480.0,
                    linkedBookingRef = "HTL-50412", isDemo = true, createdAtMillis = now - 5 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-103", type = "INCOME", category = "Umrah Revenue",
                    description = "DEMO: Umrah VIP Deposit (UMR-30198)", amountUsd = 4500.0,
                    linkedBookingRef = "UMR-30198", isDemo = true, createdAtMillis = now - 4 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-104", type = "INCOME", category = "Hajj Revenue",
                    description = "DEMO: Hajj Royal Deposit (HJJ-90211)", amountUsd = 5000.0,
                    linkedBookingRef = "HJJ-90211", isDemo = true, createdAtMillis = now - 2 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-105", type = "INCOME", category = "Visa Revenue",
                    description = "DEMO: Turkey Visa Service Fee (VSA-71820)", amountUsd = 140.0,
                    linkedBookingRef = "VSA-71820", isDemo = true, createdAtMillis = now - 7 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-201", type = "EXPENSE", category = "Supplier Payments",
                    description = "DEMO: Airline & Hotel Net Settlement Batch", amountUsd = 5400.0,
                    linkedBookingRef = "BATCH-NOV", isDemo = true, createdAtMillis = now - 4 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-202", type = "EXPENSE", category = "Salaries",
                    description = "DEMO: Travel Consultants & Mutawwif Payroll", amountUsd = 2100.0,
                    linkedBookingRef = "PAYROLL-NOV", isDemo = true, createdAtMillis = now - 3 * dayMs
                ),
                FinancialTransactionEntity(
                    referenceCode = "TX-DEMO-203", type = "EXPENSE", category = "Office Expenses",
                    description = "DEMO: Mogadishu Maka Al-Mukarama HQ Suite & Utilities", amountUsd = 850.0,
                    linkedBookingRef = "HQ-OPS", isDemo = true, createdAtMillis = now - 2 * dayMs
                )
            )
        )

        // 12. Editorial Content (Special Offers, Blog, FAQ, Testimonials)
        dao.insertEditorialList(
            listOf(
                EditorialContentEntity(
                    contentType = "OFFER",
                    title = "Early Bird Hajj 2027 Royal Allocation",
                    categoryOrSubtitle = "Save $600 per Pilgrim • Limited VIP Mina Tents",
                    body = "Reserve before January 30 with a flexible installment plan, Haramain high-speed rail, and Fairmont Makkah Kaaba-view suites.",
                    badgeText = "LIMITED VIP", coverKey = "makkah", isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "OFFER",
                    title = "Mogadishu – Istanbul Business Upgrade",
                    categoryOrSubtitle = "Turkish Airlines Executive Lounge & Chauffeur",
                    body = "Enjoy 2 x 32kg luggage, Istanbul Bosphorus VIP transfer, and 48-hour express Turkish visa coordination.",
                    badgeText = "SAVE 18%", coverKey = "aviation", isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "BLOG",
                    title = "Haramain High-Speed Rail Guide for Somali Umrah Pilgrims",
                    categoryOrSubtitle = "Pilgrimage Briefing • 5 min read",
                    body = "Travel seamlessly between Jeddah Airport, Makkah, and Madinah in under 2 hours at 300 km/h with SOM TREVEL VIP station escort.",
                    badgeText = "PILGRIMAGE", coverKey = "makkah", isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "BLOG",
                    title = "Exploring Somalia's Indian Ocean & Red Sea Jewels",
                    categoryOrSubtitle = "Heritage Dispatch • 4 min read",
                    body = "From Lido Beach's vibrant sunrise cafes in Mogadishu to Neolithic rock art at Laas Geel near Hargeisa and Berbera's coral reefs.",
                    badgeText = "SOMALIA", coverKey = "mogadishu", isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "FAQ",
                    title = "How does SOM TREVEL ticket verification work?",
                    categoryOrSubtitle = "Tickets & PNR",
                    body = "Enter your 6-character SOM TREVEL PNR and passenger surname in the Tickets tab to verify bookings stored in our agency system. Note: Live airline check-in happens directly on the operating carrier's website 24h before departure.",
                    isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "FAQ",
                    title = "What currencies can I pay in?",
                    categoryOrSubtitle = "Payments & Accounting",
                    body = "SOM TREVEL accepts USD, EUR, SAR, and Somali Shilling (SOS) with transparent real-time conversion rates configured by our accounting desk.",
                    isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "FAQ",
                    title = "Do Hajj & Umrah packages include visas and scholar guidance?",
                    categoryOrSubtitle = "Hajj & Umrah",
                    body = "Yes. Every SOM TREVEL Hajj and Umrah package includes official visa processing, hotel stays near the Two Holy Mosques, ground transfers, and bilingual Somali/English scholar guidance.",
                    isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "TESTIMONIAL",
                    title = "Dr. Mohamed Abdi — London & Mogadishu",
                    categoryOrSubtitle = "VIP Umrah & Business Travel Client",
                    body = "“SOM TREVEL coordinated our entire family Umrah from London and Mogadishu flawlessly. Our Kaaba-view rooms and Haramain train seats were ready before we landed.”",
                    badgeText = "5.0 ★", isDemo = true
                ),
                EditorialContentEntity(
                    contentType = "TESTIMONIAL",
                    title = "Fadumo Ali — Hargeisa",
                    categoryOrSubtitle = "Turkish Airlines & Hotel Guest",
                    body = "“Instant PDF e-ticket delivery, transparent pricing in USD and SOS, and genuine 24/7 WhatsApp care.”",
                    badgeText = "5.0 ★", isDemo = true
                )
            )
        )

        // 13. Media Gallery Assets
        dao.insertMediaAssets(
            listOf(
                MediaAssetEntity(title = "SOM TREVEL Flagship Jetliner", category = "Airplane", drawableKey = "aviation", isDemo = true),
                MediaAssetEntity(title = "Mogadishu Lido Coastline", category = "Destination", drawableKey = "mogadishu", isDemo = true),
                MediaAssetEntity(title = "Masjid al-Haram Twilight", category = "Hajj & Umrah", drawableKey = "makkah", isDemo = true),
                MediaAssetEntity(title = "Luxury Infinity Resort", category = "Hotel", drawableKey = "hotel", isDemo = true)
            )
        )

        // 14. Service Requests & Notifications & Audit Logs
        dao.insertServiceRequest(
            ServiceRequestEntity(
                userId = customerId, customerName = "Amina Hassan Warsame",
                serviceType = "TRANSFER", subject = "VIP Chauffeur Pickup — MGQ & IST",
                details = "Mercedes V-Class requested for arrival on Dec 4 at Istanbul Airport.",
                status = "Confirmed", adminReply = "Driver assigned: Mehmet K. (+90 555 219 0410).",
                isDemo = true, createdAtMillis = now - 4 * dayMs
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                userId = customerId,
                title = "E-Ticket Issued • PNR ST884K",
                message = "Your Turkish Airlines Business Class ticket MGQ → IST is ready for PDF download.",
                category = "Ticket", isDemo = true, createdAtMillis = now - 6 * dayMs
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                userId = customerId,
                title = "Turkey e-Visa Approved",
                message = "Reference VSA-71820 has been approved and added to your Document Center.",
                category = "Visa", isDemo = true, createdAtMillis = now - 5 * dayMs
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = "Zakariye Salaad", actorRole = "Super Admin",
                action = "INITIALIZED_DEMO_PLATFORM",
                targetSummary = "Seeded SOM TREVEL catalog, flights, hotels, Hajj/Umrah, and demo ledger",
                isDemo = true, createdAtMillis = now - 7 * dayMs
            )
        )
    }
}
