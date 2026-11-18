package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomTrevelViewModel

@Composable
fun CustomerPortalScreen(
    vm: SomTrevelViewModel,
    onOpenAdminConsole: () -> Unit
) {
    val prefs by vm.preferences.collectAsState()
    val rates by vm.currencyRates.collectAsState()
    val currentUser by vm.currentUser.collectAsState()
    val myFlights by vm.myFlightBookings.collectAsState()
    val myHotels by vm.myHotelBookings.collectAsState()
    val myPackages by vm.myPackageBookings.collectAsState()
    val myVisas by vm.myVisaApplications.collectAsState()
    val myRequests by vm.myServiceRequests.collectAsState()
    val myNotifications by vm.myNotifications.collectAsState()

    val context = LocalContext.current

    if (currentUser == null) {
        AuthPortalView(vm = vm)
        return
    }

    val user = currentUser!!
    val userRole = UserRole.valueOf(user.role)
    var portalTab by remember { mutableStateOf("OVERVIEW") }

    val totalBookings = myFlights.size + myHotels.size + myPackages.size + myVisas.size
    val confirmedTickets = myFlights.count { it.ticketStatus in listOf("Issued", "Confirmed") }
    val totalSpentUsd = myFlights.filter { it.paymentStatus == "PAID" }.sumOf { it.totalUsd } +
        myHotels.filter { it.paymentStatus == "PAID" }.sumOf { it.totalUsd } +
        myPackages.sumOf { it.paidUsd } +
        myVisas.sumOf { it.feeUsd }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Customer Header Banner
        Surface(color = DeepNavy, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LuxuryBadge(text = userRole.displayName.uppercase())
                        Spacer(Modifier.height(6.dp))
                        Text(user.fullName, style = MaterialTheme.typography.titleLarge, color = PureWhite)
                        Text("${user.email} • PP: ${user.passportNumber.ifBlank { "Not set" }}", style = MaterialTheme.typography.bodyMedium, color = LuxuryGoldSoft)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (userRole.isStaff) {
                            Button(
                                onClick = onOpenAdminConsole,
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = DeepNavy),
                                modifier = Modifier.testTag("portal_open_admin_btn")
                            ) { Text("Admin", fontWeight = FontWeight.Bold) }
                        }
                        IconButton(onClick = { vm.logout() }, modifier = Modifier.testTag("portal_logout_btn")) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sign Out", tint = LuxuryGold)
                        }
                    }
                }
            }
        }

        // Portal Sub-navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "OVERVIEW" to "Overview",
                "BOOKINGS" to "Bookings ($totalBookings)",
                "ALERTS" to "Notifications (${myNotifications.count { !it.isRead }})",
                "SUPPORT" to "Messages & Support",
                "PROFILE" to "Profile & Docs"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = portalTab == key,
                    onClick = { portalTab = key },
                    label = { Text(label) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (portalTab) {
                "OVERVIEW" -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PortalKpiCard("Total Bookings", "$totalBookings", Icons.Filled.ConfirmationNumber, Modifier.weight(1f))
                            PortalKpiCard("Active Tickets", "$confirmedTickets", Icons.Filled.Flight, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PortalKpiCard("Total Paid", CurrencyFormatter.format(totalSpentUsd, prefs.currencyCode, rates), Icons.Filled.Payments, Modifier.weight(1f))
                            PortalKpiCard("Visas Tracked", "${myVisas.size}", Icons.Filled.Badge, Modifier.weight(1f))
                        }
                    }
                    item {
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Personal Travel Spend Breakdown", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(10.dp))
                                RevenueExpenseChart(
                                    items = listOf(
                                        "Flights" to myFlights.sumOf { it.totalUsd },
                                        "Hotels" to myHotels.sumOf { it.totalUsd },
                                        "Hajj/Umrah" to myPackages.sumOf { it.paidUsd },
                                        "Visas" to myVisas.sumOf { it.feeUsd }
                                    ),
                                    accentColor = RoyalBlueLight
                                )
                            }
                        }
                    }
                    if (myNotifications.isNotEmpty()) {
                        item { Text("Recent Activity", style = MaterialTheme.typography.titleLarge) }
                        items(myNotifications.take(3)) { n ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(n.title, fontWeight = FontWeight.Bold)
                                    Text(n.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                "BOOKINGS" -> {
                    item { Text("My Flight Reservations", style = MaterialTheme.typography.titleLarge) }
                    if (myFlights.isEmpty()) {
                        item { EmptyStateCard("No Flight Bookings", "Book a flight from the Services tab.") }
                    } else {
                        items(myFlights) { fb ->
                            AirlineTicketCard(
                                booking = fb,
                                currencyCode = prefs.currencyCode,
                                rates = rates,
                                onDownloadPdf = { PdfTicketExporter.shareTicketPdf(context, fb) },
                                onPrintPdf = { PdfTicketExporter.printTicketPdf(context, fb) },
                                onRequestAction = { act -> vm.requestTicketAction(fb, act) }
                            )
                        }
                    }

                    item { Text("My Hotel Reservations", style = MaterialTheme.typography.titleLarge) }
                    items(myHotels) { hb ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(hb.hotelName, style = MaterialTheme.typography.titleMedium)
                                    StatusPill(hb.status)
                                }
                                Text("${hb.roomCategory} • ${hb.checkInDate} → ${hb.checkOutDate} (${hb.nights}N)")
                                Text("Ref: ${hb.referenceCode} • ${CurrencyFormatter.format(hb.totalUsd, prefs.currencyCode, rates)}", color = RoyalBlueLight)
                            }
                        }
                    }

                    item { Text("My Hajj, Umrah & Holiday Packages", style = MaterialTheme.typography.titleLarge) }
                    items(myPackages) { pb ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(pb.packageTitle, style = MaterialTheme.typography.titleMedium)
                                    StatusPill(pb.status)
                                }
                                Text("${pb.category} (${pb.tier}) • Group ${pb.groupCode} • ${pb.travelerCount} Pax")
                                Text("Paid ${CurrencyFormatter.format(pb.paidUsd, prefs.currencyCode, rates)} of ${CurrencyFormatter.format(pb.totalUsd, prefs.currencyCode, rates)}", color = RoyalBlueLight)
                            }
                        }
                    }

                    item { Text("My Visa Applications", style = MaterialTheme.typography.titleLarge) }
                    items(myVisas) { v ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${v.destinationCountry} • ${v.referenceCode}", style = MaterialTheme.typography.titleMedium)
                                    StatusPill(v.status)
                                }
                                Text("${v.visaCategory} • ${v.adminRemarks}")
                            }
                        }
                    }
                }

                "ALERTS" -> {
                    if (myNotifications.isEmpty()) {
                        item { EmptyStateCard("All Caught Up", "No unread alerts.") }
                    }
                    items(myNotifications) { n ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().clickable { vm.markNotificationRead(n.id) }
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, null, tint = if (n.isRead) MutedSlate else LuxuryGoldDark)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(n.title, fontWeight = if (n.isRead) FontWeight.Normal else FontWeight.Bold)
                                    Text(n.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                "SUPPORT" -> {
                    item {
                        var subj by remember { mutableStateOf("") }
                        var msg by remember { mutableStateOf("") }
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Message SOM TREVEL Desk", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(value = subj, onValueChange = { subj = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("How can our agents assist?") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        if (msg.isNotBlank()) {
                                            vm.submitServiceRequest("SUPPORT", subj.ifBlank { "Customer Support" }, msg)
                                            subj = ""
                                            msg = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Send Support Message") }
                            }
                        }
                    }
                    items(myRequests) { r ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(r.subject, fontWeight = FontWeight.Bold)
                                    StatusPill(r.status)
                                }
                                Text(r.details, style = MaterialTheme.typography.bodyMedium)
                                if (r.adminReply.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Agency Reply: ${r.adminReply}", color = EmeraldConfirmed, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                "PROFILE" -> {
                    item {
                        var name by remember(user) { mutableStateOf(user.fullName) }
                        var phone by remember(user) { mutableStateOf(user.phone) }
                        var passport by remember(user) { mutableStateOf(user.passportNumber) }
                        var nat by remember(user) { mutableStateOf(user.nationality) }

                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Account & Passport Settings", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = passport, onValueChange = { passport = it }, label = { Text("Passport Number") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = nat, onValueChange = { nat = it }, label = { Text("Nationality") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = { vm.updateProfile(name, phone, passport, nat) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Save Profile") }
                            }
                        }
                    }
                    item {
                        Text("Downloadable Documents", style = MaterialTheme.typography.titleLarge)
                    }
                    items(myFlights) { fb ->
                        ListItem(
                            headlineContent = { Text("E-Ticket PDF • PNR ${fb.pnr}") },
                            supportingContent = { Text("${fb.originCode} → ${fb.destinationCode} (${fb.ticketStatus})") },
                            trailingContent = {
                                IconButton(onClick = { PdfTicketExporter.shareTicketPdf(context, fb) }) {
                                    Icon(Icons.Filled.Download, contentDescription = "Download PDF")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthPortalView(vm: SomTrevelViewModel) {
    var mode by remember { mutableStateOf("LOGIN") } // LOGIN, REGISTER, FORGOT
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passport by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Somali") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(color = DeepNavy, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    LuxuryBadge(text = "CUSTOMER & STAFF PORTAL")
                    Spacer(Modifier.height(8.dp))
                    Text("SOM TREVEL Account Access", style = MaterialTheme.typography.headlineMedium, color = PureWhite)
                    Text("Each customer account strictly isolates personal bookings, e-tickets, visas, and payments.", style = MaterialTheme.typography.bodyMedium, color = LuxuryGoldSoft)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == "LOGIN", onClick = { mode = "LOGIN" }, label = { Text("Sign In") })
                FilterChip(selected = mode == "REGISTER", onClick = { mode = "REGISTER" }, label = { Text("Register") })
                FilterChip(selected = mode == "FORGOT", onClick = { mode = "FORGOT" }, label = { Text("Reset Password") })
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (mode == "REGISTER") {
                        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (+252...)") }, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth().testTag("auth_email_input"))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (mode == "FORGOT") "New Password (6+ chars)" else "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )
                    if (mode == "REGISTER") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = passport, onValueChange = { passport = it }, label = { Text("Passport #") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = nationality, onValueChange = { nationality = it }, label = { Text("Nationality") }, modifier = Modifier.weight(1f))
                        }
                    }

                    Button(
                        onClick = {
                            when (mode) {
                                "LOGIN" -> vm.login(email, password)
                                "REGISTER" -> vm.registerCustomer(fullName, email, phone, password, passport, nationality)
                                "FORGOT" -> vm.resetPassword(email, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_btn")
                    ) {
                        Text(
                            when (mode) {
                                "LOGIN" -> "Sign In Securely"
                                "REGISTER" -> "Create Customer Account"
                                else -> "Reset Password"
                            }
                        )
                    }
                }
            }
        }

        // Demo Quick-Access Accounts (Clearly labeled DEMO for evaluation)
        item {
            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DEMO Accounts (Instant Role Preview)", style = MaterialTheme.typography.titleMedium)
                    Text("Use these seeded demo profiles to preview Customer Portal isolation or Admin Dashboard RBAC.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { vm.quickSwitchDemoAccount(1L) },
                        modifier = Modifier.fillMaxWidth().testTag("demo_login_customer")
                    ) { Text("Demo Customer: Amina Warsame") }
                    OutlinedButton(
                        onClick = { vm.quickSwitchDemoAccount(3L) },
                        modifier = Modifier.fillMaxWidth().testTag("demo_login_admin")
                    ) { Text("Demo Super Admin: Zakariye Salaad") }
                    OutlinedButton(
                        onClick = { vm.quickSwitchDemoAccount(4L) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Demo Accountant: Hodan Ali") }
                }
            }
        }
    }
}

@Composable
private fun PortalKpiCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = LuxuryGoldDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
