package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomTrevelViewModel

@Composable
fun DiscoverHomeScreen(
    vm: SomTrevelViewModel,
    onNavigateToService: (String, String) -> Unit, // serviceTab, optionalSearchQuery
    onNavigateToTickets: () -> Unit,
    onNavigateToPortal: () -> Unit
) {
    val lang by vm.language.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val rates by vm.currencyRates.collectAsState()
    val destinations by vm.destinations.collectAsState()
    val hotels by vm.hotels.collectAsState()
    val editorial by vm.editorialContent.collectAsState()
    val media by vm.mediaAssets.collectAsState()

    val context = LocalContext.current

    // Home Flight Search Widget state
    var tripType by remember { mutableStateOf("ROUND_TRIP") }
    var fromQuery by remember { mutableStateOf("MGQ") }
    var toQuery by remember { mutableStateOf("") }
    var cabinClass by remember { mutableStateOf("Economy") }

    // Public Info Sheet state (About, Insurance, Transfers, Gallery, Blog, FAQ, Contact)
    var activePublicSheet by remember { mutableStateOf<String?>(null) }
    var newsletterEmail by remember { mutableStateOf("") }

    val somaliDestinations = remember(destinations) { destinations.filter { it.isSomali } }
    val globalDestinations = remember(destinations) { destinations.filter { !it.isSomali } }
    val offers = remember(editorial) { editorial.filter { it.contentType == "OFFER" } }
    val testimonials = remember(editorial) { editorial.filter { it.contentType == "TESTIMONIAL" } }
    val blogs = remember(editorial) { editorial.filter { it.contentType == "BLOG" } }
    val faqs = remember(editorial) { editorial.filter { it.contentType == "FAQ" } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Cinematic Aviation Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                CoverArtworkImage(
                    coverKey = "aviation",
                    contentDescription = "SOM TREVEL Luxury Aircraft",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    DeepNavy.copy(alpha = 0.25f),
                                    DeepNavy.copy(alpha = 0.72f),
                                    DeepNavy.copy(alpha = 0.96f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    LuxuryBadge(text = L10n.text(lang, "EST. MOGADISHU • WORLDWIDE", "MUQDISHO ILAA CAALAMKA"))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = L10n.text(
                            lang,
                            "Elevate Every Journey Across Continents & Sacred Lands",
                            "U Safar Heer Caalami ah adigoo Helaya Adeeg Raaxo leh"
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        color = PureWhite
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = L10n.text(
                            lang,
                            "International & Domestic Flights • 5-Star Hotels • Hajj & Umrah • Visas",
                            "Duulimaadyo • Huteelo 5-Xiddig • Xaj & Cumro • Dal-ku-gal (Visa)"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LuxuryGoldSoft
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onNavigateToService("FLIGHTS", "") },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = DeepNavy),
                            modifier = Modifier.testTag("hero_book_flight_btn")
                        ) {
                            Icon(Icons.Filled.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(L10n.text(lang, "Book Flights", "Gooso Tigidh"), fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onNavigateToTickets,
                            border = BorderStroke(1.dp, LuxuryGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                            modifier = Modifier.testTag("hero_verify_ticket_btn")
                        ) {
                            Text(L10n.text(lang, "Verify PNR", "Hubi Tigidhka"))
                        }
                    }
                }
            }
        }

        // 2. Interactive Flight Search Widget
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = L10n.text(lang, "Flight Search", "Raadi Duulimaad"),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LuxuryBadge(text = "MGQ • HGA • GLOBAL")
                    }
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ROUND_TRIP" to "Round Trip", "ONE_WAY" to "One Way", "MULTI_CITY" to "Multi-City").forEach { (key, label) ->
                            FilterChip(
                                selected = tripType == key,
                                onClick = { tripType = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fromQuery,
                            onValueChange = { fromQuery = it },
                            label = { Text(L10n.text(lang, "From (City/Code)", "Ka bax")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("home_search_from")
                        )
                        OutlinedTextField(
                            value = toQuery,
                            onValueChange = { toQuery = it },
                            label = { Text(L10n.text(lang, "To (e.g. IST, DXB)", "U soco")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("home_search_to")
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Economy", "Premium Economy", "Business", "First Class").forEach { cls ->
                            FilterChip(
                                selected = cabinClass == cls,
                                onClick = { cabinClass = cls },
                                label = { Text(cls, maxLines = 1) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigateToService("FLIGHTS", toQuery.ifBlank { fromQuery }) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("home_search_flights_btn")
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(L10n.text(lang, "Search Available Flights", "Raadi Duulimaadyada"))
                    }
                }
            }
        }

        // 3. Public Website Pages & Services Directory (All 17 Public Modules Accessible!)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = L10n.text(lang, "Travel Services & Concierge", "Adeegyada & Hagaha Safarka"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                val quickLinks = listOf(
                    Triple("FLIGHTS", "Int'l & Domestic", Icons.Filled.Flight),
                    Triple("HOTELS", "Luxury Hotels", Icons.Filled.Hotel),
                    Triple("HAJJ", "Hajj 2027", Icons.Filled.Mosque),
                    Triple("UMRAH", "Umrah VIP", Icons.Filled.Star),
                    Triple("VISAS", "Visa Desk", Icons.Filled.Badge),
                    Triple("HOLIDAYS", "Holiday Tours", Icons.Filled.BeachAccess),
                    Triple("INSURANCE", "Travel Insurance", Icons.Filled.HealthAndSafety),
                    Triple("TRANSFERS", "Airport Transfers", Icons.Filled.DirectionsCar),
                    Triple("OFFERS", "Special Offers", Icons.Filled.LocalOffer),
                    Triple("GALLERY", "Photo Gallery", Icons.Filled.PhotoLibrary),
                    Triple("BLOG", "Travel News", Icons.Filled.Article),
                    Triple("FAQ", "FAQs", Icons.Filled.HelpOutline),
                    Triple("ABOUT", "About SOM TREVEL", Icons.Filled.Business),
                    Triple("CONTACT", "Contact & WhatsApp", Icons.Filled.SupportAgent)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickLinks.forEach { (key, title, icon) ->
                        ServiceQuickPill(
                            title = title,
                            icon = icon,
                            onClick = {
                                when (key) {
                                    "FLIGHTS", "HOTELS", "HAJJ", "UMRAH", "VISAS", "HOLIDAYS" ->
                                        onNavigateToService(key, "")
                                    else -> activePublicSheet = key
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. Sacred Hajj & Umrah Promotion Spotlight
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = DeepNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        CoverArtworkImage(
                            coverKey = "makkah",
                            contentDescription = "Masjid al-Haram Makkah",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, DeepNavy)))
                        )
                        LuxuryBadge(
                            text = L10n.text(lang, "SACRED PILGRIMAGE", "XAJKA & CUMRADA"),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = L10n.text(lang, "Hajj & Umrah Sanctuary Programs", "Barnaamijyada Xajka & Cumrada"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = L10n.text(
                                lang,
                                "Haram-view 5-star suites, Haramain high-speed rail, official Nusuk visas, and bilingual Somali/English scholars.",
                                "Huteelo ku yaal Xaramka agtiisa, tareenka degdega ah, fiisaha rasmiga ah, iyo culimo hagaysa."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LuxuryGoldSoft
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onNavigateToService("HAJJ", "") },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = DeepNavy),
                                modifier = Modifier.weight(1f).testTag("home_hajj_btn")
                            ) {
                                Text("Hajj Packages", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onNavigateToService("UMRAH", "") },
                                border = BorderStroke(1.dp, LuxuryGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                                modifier = Modifier.weight(1f).testTag("home_umrah_btn")
                            ) {
                                Text("Umrah Tiers")
                            }
                        }
                    }
                }
            }
        }

        // 5. Popular International Destinations
        item {
            SectionHeaderRow(
                title = L10n.text(lang, "Popular Global Destinations", "Magaalooyinka Caalamka"),
                actionLabel = L10n.text(lang, "Explore All", "Dhammaan"),
                onAction = { onNavigateToService("DESTINATIONS", "") }
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(globalDestinations.take(8)) { dest ->
                    DestinationShowcaseCard(
                        dest = dest,
                        currencyCode = prefs.currencyCode,
                        rates = rates,
                        onClick = { onNavigateToService("DESTINATIONS", dest.city) }
                    )
                }
            }
        }

        // 6. Popular Somali Destinations
        item {
            SectionHeaderRow(
                title = L10n.text(lang, "Discover Somalia", "Quruxda Dhulka Hooyo"),
                actionLabel = L10n.text(lang, "Domestic Flights", "Gudaha"),
                onAction = { onNavigateToService("FLIGHTS", "MGQ") }
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(somaliDestinations) { dest ->
                    DestinationShowcaseCard(
                        dest = dest,
                        currencyCode = prefs.currencyCode,
                        rates = rates,
                        onClick = { onNavigateToService("DESTINATIONS", dest.city) }
                    )
                }
            }
        }

        // 7. Featured Luxury Hotels
        item {
            SectionHeaderRow(
                title = L10n.text(lang, "Featured Luxury Hotels", "Huteelada Ugu Caansan"),
                actionLabel = L10n.text(lang, "Reserve", "Ballanso"),
                onAction = { onNavigateToService("HOTELS", "") }
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hotels) { hotel ->
                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .width(240.dp)
                            .clickable { onNavigateToService("HOTELS", hotel.city) }
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().height(124.dp)) {
                                CoverArtworkImage(
                                    coverKey = hotel.coverKey,
                                    customUri = hotel.customImageUri,
                                    contentDescription = hotel.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = DeepNavy.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.padding(10.dp).align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = "${hotel.stars} ★",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LuxuryGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(hotel.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${hotel.city}, ${hotel.country}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "${CurrencyFormatter.format(hotel.pricePerNightUsd, prefs.currencyCode, rates)} / night",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RoyalBlueLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. Special Travel Offers
        if (offers.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = L10n.text(lang, "Special Travel Offers", "Dalabyo Gaar ah"),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    offers.forEach { offer ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToService("HOLIDAYS", "") }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(offer.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    if (offer.badgeText.isNotBlank()) LuxuryBadge(text = offer.badgeText)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(offer.categoryOrSubtitle, style = MaterialTheme.typography.labelLarge, color = RoyalBlueLight)
                                Spacer(Modifier.height(4.dp))
                                Text(offer.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 9. Why Choose SOM TREVEL + Testimonials
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = L10n.text(lang, "Why Choose SOM TREVEL?", "Maxaad u Dooranaysaa SOM TREVEL?"),
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WhyCard(
                        icon = Icons.Filled.VerifiedUser,
                        title = "Transparent PNR & PDF",
                        body = "Instant agency e-tickets & verified records.",
                        modifier = Modifier.weight(1f)
                    )
                    WhyCard(
                        icon = Icons.Filled.CurrencyExchange,
                        title = "USD • EUR • SAR • SOS",
                        body = "Pay & view prices in 4 currencies.",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (testimonials.isNotEmpty()) {
                    testimonials.forEach { item ->
                        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                                    Text(item.badgeText, style = MaterialTheme.typography.labelLarge, color = LuxuryGoldDark)
                                }
                                Text(item.categoryOrSubtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(item.body, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }

        // 10. Newsletter & Agency Footer
        item {
            Surface(
                color = DeepNavy,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = L10n.text(lang, "VIP Fare Alerts & Hajj Updates", "Ogeysiisyada Qiimaha & Xajka"),
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Maka Al-Mukarama Road, Mogadishu • 26 June Road, Hargeisa • +252 61 900 1000",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LuxuryGoldSoft
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newsletterEmail,
                        onValueChange = { newsletterEmail = it },
                        placeholder = { Text("Enter email for exclusive alerts", color = LuxuryGoldSoft.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = RoyalBlueLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                vm.subscribeNewsletter(newsletterEmail)
                                newsletterEmail = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = DeepNavy),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Subscribe", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onNavigateToPortal,
                            border = BorderStroke(1.dp, LuxuryGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Customer Portal")
                        }
                    }
                }
            }
        }
    }

    // Sub-page Modal Sheet for About, Insurance, Transfers, Offers, Gallery, Blog, FAQ, Contact
    if (activePublicSheet != null) {
        PublicPageModalDialog(
            pageKey = activePublicSheet!!,
            offers = offers,
            blogs = blogs,
            faqs = faqs,
            media = media,
            onDismiss = { activePublicSheet = null },
            onSubmitServiceRequest = { type, subject, details ->
                vm.submitServiceRequest(type, subject, details)
                activePublicSheet = null
            },
            onOpenWhatsApp = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/252619001000?text=Hello%20SOM%20TREVEL%20Concierge"))
                runCatching { context.startActivity(intent) }
            }
        )
    }
}

@Composable
private fun ServiceQuickPill(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = LuxuryGoldDark, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SectionHeaderRow(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) {
            Text(actionLabel, color = RoyalBlueLight, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DestinationShowcaseCard(
    dest: DestinationEntity,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(210.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                CoverArtworkImage(
                    coverKey = dest.coverKey,
                    customUri = dest.customImageUri,
                    contentDescription = "${dest.city}, ${dest.country}",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, DeepNavy.copy(alpha = 0.85f))))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(dest.city, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                    Text(dest.country, style = MaterialTheme.typography.labelSmall, color = LuxuryGold)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = dest.attractions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "From ${CurrencyFormatter.format(dest.startingPriceUsd, currencyCode, rates)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun WhyCard(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = LuxuryGoldDark)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PublicPageModalDialog(
    pageKey: String,
    offers: List<EditorialContentEntity>,
    blogs: List<EditorialContentEntity>,
    faqs: List<EditorialContentEntity>,
    media: List<MediaAssetEntity>,
    onDismiss: () -> Unit,
    onSubmitServiceRequest: (String, String, String) -> Unit,
    onOpenWhatsApp: () -> Unit
) {
    var formTitle by remember { mutableStateOf("") }
    var formDetails by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (pageKey) {
                    "ABOUT" -> "About SOM TREVEL"
                    "INSURANCE" -> "Travel Insurance Desk"
                    "TRANSFERS" -> "Luxury Airport Transfers"
                    "OFFERS" -> "Special Offers"
                    "GALLERY" -> "SOM TREVEL Gallery"
                    "BLOG" -> "Blog & Travel News"
                    "FAQ" -> "Frequently Asked Questions"
                    else -> "Contact & WhatsApp Concierge"
                },
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (pageKey) {
                    "ABOUT" -> {
                        Text("SOM TREVEL is a premier Somali and international travel agency headquartered in Mogadishu with regional offices in Hargeisa, Nairobi, Dubai, and Istanbul.")
                        Text("We specialize in seamless international & domestic ticketing, curated 5-star hotel stays, sacred Hajj & Umrah pilgrimages, and diplomatic/tourist visa dossiers.")
                    }
                    "INSURANCE" -> {
                        Text("Request comprehensive Schengen, Worldwide Medical, or Hajj/Umrah Travel Protection.")
                        OutlinedTextField(value = formTitle, onValueChange = { formTitle = it }, label = { Text("Destination & Trip Dates") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = formDetails, onValueChange = { formDetails = it }, label = { Text("Travelers & Coverage Notes") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = { onSubmitServiceRequest("INSURANCE", "Insurance: $formTitle", formDetails.ifBlank { "Standard worldwide coverage" }) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Request Insurance Quote") }
                    }
                    "TRANSFERS" -> {
                        Text("Book private armored or VIP Mercedes/SUV airport pickups at MGQ, HGA, JED, DXB, IST, and LHR.")
                        OutlinedTextField(value = formTitle, onValueChange = { formTitle = it }, label = { Text("Airport & Flight Number") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = formDetails, onValueChange = { formDetails = it }, label = { Text("Drop-off Hotel & Passengers") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = { onSubmitServiceRequest("TRANSFER", "Transfer: $formTitle", formDetails.ifBlank { "Private VIP chauffeur" }) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Reserve Airport Transfer") }
                    }
                    "OFFERS" -> {
                        offers.forEach { o ->
                            ElevatedCard { Column(Modifier.padding(12.dp)) { Text(o.title, fontWeight = FontWeight.Bold); Text(o.body) } }
                        }
                    }
                    "GALLERY" -> {
                        media.forEach { m ->
                            ElevatedCard {
                                Column {
                                    CoverArtworkImage(m.drawableKey, m.localUri, m.title, Modifier.fillMaxWidth().height(110.dp))
                                    Text("${m.title} • ${m.category}", Modifier.padding(10.dp), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    "BLOG" -> {
                        blogs.forEach { b ->
                            ElevatedCard {
                                Column(Modifier.padding(12.dp)) {
                                    Text(b.categoryOrSubtitle, style = MaterialTheme.typography.labelSmall, color = RoyalBlueLight)
                                    Text(b.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(b.body, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    "FAQ" -> {
                        faqs.forEach { f ->
                            var expanded by remember { mutableStateOf(false) }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(f.title, fontWeight = FontWeight.Bold)
                                    AnimatedVisibility(expanded) {
                                        Text(f.body, modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                    "CONTACT" -> {
                        Text("24/7 Concierge: +252 61 900 1000\nEmail: concierge@somtrevel.com\nMogadishu HQ: Maka Al-Mukarama Road")
                        OutlinedButton(onClick = onOpenWhatsApp, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Chat, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Open WhatsApp Concierge")
                        }
                        OutlinedTextField(value = formTitle, onValueChange = { formTitle = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = formDetails, onValueChange = { formDetails = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = { onSubmitServiceRequest("MESSAGE", formTitle.ifBlank { "General Inquiry" }, formDetails) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Send Message to Agency") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
