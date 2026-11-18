package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomTrevelViewModel

@Composable
fun AdminDashboardScreen(
    vm: SomTrevelViewModel,
    onGoToPortal: () -> Unit
) {
    val prefs by vm.preferences.collectAsState()
    val rates by vm.currencyRates.collectAsState()
    val currentUser by vm.currentUser.collectAsState()
    val accounting by vm.accountingSummary.collectAsState()

    val allUsers by vm.allUsers.collectAsState()
    val allFlights by vm.flights.collectAsState()
    val allHotels by vm.hotels.collectAsState()
    val allFlightBookings by vm.allFlightBookings.collectAsState()
    val allPackageBookings by vm.allPackageBookings.collectAsState()
    val allVisas by vm.allVisaApplications.collectAsState()
    val allTransactions by vm.allTransactions.collectAsState()
    val allRequests by vm.allServiceRequests.collectAsState()
    val mediaAssets by vm.mediaAssets.collectAsState()
    val auditLogs by vm.auditLogs.collectAsState()

    val context = LocalContext.current

    val role = currentUser?.let { UserRole.valueOf(it.role) }
    if (currentUser == null || role == null || !role.isStaff) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = LuxuryGoldDark, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Staff & Admin Authorization Required", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Customer accounts cannot access agency operations, financial ledgers, or other travelers' records. Sign in with a Super Admin, Accountant, or Booking Manager account on the Portal tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onGoToPortal, modifier = Modifier.testTag("admin_go_portal_btn")) {
                        Text("Open Staff / Customer Portal")
                    }
                }
            }
        }
        return
    }

    var adminTab by remember { mutableStateOf("OVERVIEW") }
    var showConfirmPurgeDemo by remember { mutableStateOf(false) }

    val adminTabs = listOf(
        "OVERVIEW" to "Overview",
        "TICKETS" to "Tickets & Bookings",
        "VISAS" to "Visas",
        "ACCOUNTING" to "Accounting & FX",
        "CATALOG" to "Catalog & Blog",
        "MEDIA" to "Media Upload",
        "GOVERNANCE" to "Staff, Support & Audit"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(color = DeepNavy, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    LuxuryBadge(text = "ROLE: ${role.displayName.uppercase()}")
                    Spacer(Modifier.height(4.dp))
                    Text("SOM TREVEL Management Console", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                }
                if (role.canManageAdminSettings) {
                    OutlinedButton(
                        onClick = { showConfirmPurgeDemo = true },
                        border = BorderStroke(1.dp, LuxuryGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxuryGold),
                        modifier = Modifier.testTag("admin_purge_demo_btn")
                    ) { Text("Purge Demo") }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            adminTabs.forEach { (key, label) ->
                FilterChip(
                    selected = adminTab == key,
                    onClick = { adminTab = key },
                    label = { Text(label) },
                    modifier = Modifier.testTag("admin_tab_$key")
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (adminTab) {
                "OVERVIEW" -> {
                    item {
                        val customersCount = allUsers.count { it.role == UserRole.CUSTOMER.name }
                        val issuedCount = allFlightBookings.count { it.ticketStatus == "Issued" }
                        val pendingBookings = allFlightBookings.count { it.ticketStatus == "Pending" } +
                            allPackageBookings.count { it.status == "Pending" }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminStatTile("Customers", "$customersCount", Modifier.weight(1f))
                            AdminStatTile("Tickets Issued", "$issuedCount", Modifier.weight(1f))
                            AdminStatTile("Pending", "$pendingBookings", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminStatTile("Revenue", CurrencyFormatter.format(accounting.totalRevenueUsd, prefs.currencyCode, rates), Modifier.weight(1f), EmeraldConfirmed)
                            AdminStatTile("Expenses", CurrencyFormatter.format(accounting.totalExpensesUsd, prefs.currencyCode, rates), Modifier.weight(1f), CrimsonCancelled)
                            AdminStatTile("Net Profit", CurrencyFormatter.format(accounting.netProfitUsd, prefs.currencyCode, rates), Modifier.weight(1f), RoyalBlueLight)
                        }
                    }

                    item {
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Revenue Streams Breakdown (USD)", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(10.dp))
                                RevenueExpenseChart(
                                    items = listOf(
                                        "Ticket Sales" to accounting.ticketSalesUsd,
                                        "Hotels" to accounting.hotelIncomeUsd,
                                        "Hajj" to accounting.hajjRevenueUsd,
                                        "Umrah" to accounting.umrahRevenueUsd,
                                        "Visas" to accounting.visaRevenueUsd
                                    ),
                                    accentColor = LuxuryGoldDark
                                )
                            }
                        }
                    }

                    item { Text("Recent Ledger Activity", style = MaterialTheme.typography.titleLarge) }
                    items(allTransactions.take(6)) { tx ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${tx.category} • ${tx.referenceCode}", fontWeight = FontWeight.Bold)
                                    Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val sign = if (tx.type == "INCOME") "+" else "-"
                                Text(
                                    "$sign${CurrencyFormatter.format(tx.amountUsd, prefs.currencyCode, rates)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "INCOME") EmeraldConfirmed else CrimsonCancelled
                                )
                            }
                        }
                    }
                }

                "TICKETS" -> {
                    item { Text("Flight Bookings & Ticket Issuance", style = MaterialTheme.typography.titleLarge) }
                    items(allFlightBookings) { fb ->
                        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("PNR ${fb.pnr} • ${fb.customerName}", fontWeight = FontWeight.Bold)
                                    StatusPill(fb.ticketStatus)
                                }
                                Text("${fb.airlineName} ${fb.flightNumber} • ${fb.originCode}→${fb.destinationCode} (${fb.departureDate})")
                                Text("Payment: ${fb.paymentStatus} • ${CurrencyFormatter.format(fb.totalUsd, prefs.currencyCode, rates)}")
                                if (role.canManageBookings) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { vm.adminUpdateTicket(fb, "Issued", "PAID") },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Issue Ticket") }
                                        OutlinedButton(
                                            onClick = { vm.adminUpdateTicket(fb, "Refunded", "REFUNDED") },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Refund") }
                                    }
                                }
                            }
                        }
                    }

                    item { Text("Hajj & Umrah Pilgrim Roster", style = MaterialTheme.typography.titleLarge) }
                    items(allPackageBookings) { pb ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${pb.category} • ${pb.referenceCode}", fontWeight = FontWeight.Bold)
                                    StatusPill(pb.status)
                                }
                                Text("${pb.packageTitle} • Group: ${pb.groupCode}")
                                Text("Pilgrims (${pb.travelerCount}): ${pb.travelerNames}")
                                Text("Paid ${CurrencyFormatter.format(pb.paidUsd, prefs.currencyCode, rates)} / ${CurrencyFormatter.format(pb.totalUsd, prefs.currencyCode, rates)}", color = RoyalBlueLight)
                            }
                        }
                    }
                }

                "VISAS" -> {
                    item { Text("Visa Review Queue", style = MaterialTheme.typography.titleLarge) }
                    items(allVisas) { v ->
                        var remarks by remember(v.id) { mutableStateOf(v.adminRemarks) }
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${v.destinationCountry} • ${v.referenceCode}", fontWeight = FontWeight.Bold)
                                    StatusPill(v.status)
                                }
                                Text("Applicant: ${v.applicantName} • PP: ${v.passportNumber}")
                                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Desk Remarks") }, modifier = Modifier.fillMaxWidth())
                                if (role.canManageBookings) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(onClick = { vm.adminReviewVisa(v, "Approved", remarks) }, modifier = Modifier.weight(1f)) { Text("Approve") }
                                        OutlinedButton(onClick = { vm.adminReviewVisa(v, "Additional Documents Required", remarks) }, modifier = Modifier.weight(1f)) { Text("Docs Req") }
                                        OutlinedButton(onClick = { vm.adminReviewVisa(v, "Rejected", remarks) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                                    }
                                }
                            }
                        }
                    }
                }

                "ACCOUNTING" -> {
                    if (!role.canManageAccounting) {
                        item { EmptyStateCard("Accounting Restricted", "Only Super Admin, Administrator, and Accountant roles can edit financial records.") }
                    } else {
                        item {
                            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Profit & Loss Summary", style = MaterialTheme.typography.titleLarge)
                                    Text("Total Revenue = ${CurrencyFormatter.format(accounting.totalRevenueUsd, prefs.currencyCode, rates)}", color = EmeraldConfirmed, fontWeight = FontWeight.Bold)
                                    Text("Total Expenses = ${CurrencyFormatter.format(accounting.totalExpensesUsd, prefs.currencyCode, rates)}", color = CrimsonCancelled, fontWeight = FontWeight.Bold)
                                    Text("Net Profit (Revenue - Expenses) = ${CurrencyFormatter.format(accounting.netProfitUsd, prefs.currencyCode, rates)}", style = MaterialTheme.typography.titleMedium, color = RoyalBlueLight)
                                    Button(
                                        onClick = {
                                            PdfTicketExporter.shareFinancialReportPdf(
                                                context,
                                                accounting.totalRevenueUsd,
                                                accounting.totalExpensesUsd,
                                                accounting.netProfitUsd,
                                                allTransactions
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("export_pl_pdf_btn")
                                    ) {
                                        Icon(Icons.Filled.PictureAsPdf, null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Export Financial P&L PDF Report")
                                    }
                                }
                            }
                        }

                        item {
                            var txType by remember { mutableStateOf("EXPENSE") }
                            var txCat by remember { mutableStateOf("Office Expenses") }
                            var txDesc by remember { mutableStateOf("") }
                            var txAmt by remember { mutableStateOf("") }
                            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Record Financial Entry", style = MaterialTheme.typography.titleMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(selected = txType == "INCOME", onClick = { txType = "INCOME"; txCat = "Other Income" }, label = { Text("Income") })
                                        FilterChip(selected = txType == "EXPENSE", onClick = { txType = "EXPENSE"; txCat = "Office Expenses" }, label = { Text("Expense") })
                                    }
                                    OutlinedTextField(value = txCat, onValueChange = { txCat = it }, label = { Text("Category (Salaries, Office Expenses, Supplier Payments...)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = txDesc, onValueChange = { txDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = txAmt, onValueChange = { txAmt = it }, label = { Text("Amount (USD)") }, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            val amt = txAmt.toDoubleOrNull() ?: 0.0
                                            if (amt > 0.0 && txDesc.isNotBlank()) {
                                                vm.adminRecordTransaction(txType, txCat, txDesc, amt)
                                                txDesc = ""
                                                txAmt = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Save Ledger Entry") }
                                }
                            }
                        }

                        item {
                            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Multi-Currency Exchange Rates (vs 1 USD)", style = MaterialTheme.typography.titleMedium)
                                    rates.filter { it.code != "USD" }.forEach { r ->
                                        var rateInput by remember(r.code, r.rateFromUsd) { mutableStateOf(r.rateFromUsd.toString()) }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(r.code, fontWeight = FontWeight.Bold, modifier = Modifier.width(46.dp))
                                            OutlinedTextField(value = rateInput, onValueChange = { rateInput = it }, singleLine = true, modifier = Modifier.weight(1f))
                                            Button(onClick = { rateInput.toDoubleOrNull()?.let { vm.adminUpdateCurrencyRate(r.code, it) } }) { Text("Save") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "CATALOG" -> {
                    item {
                        var airline by remember { mutableStateOf("Daallo Airlines") }
                        var flightNum by remember { mutableStateOf("D3 501") }
                        var fromCode by remember { mutableStateOf("MGQ") }
                        var toCode by remember { mutableStateOf("DXB") }
                        var priceStr by remember { mutableStateOf("650") }
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Add Flight Offer", style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = airline, onValueChange = { airline = it }, label = { Text("Airline") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = flightNum, onValueChange = { flightNum = it }, label = { Text("Flight #") }, modifier = Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = fromCode, onValueChange = { fromCode = it.uppercase() }, label = { Text("From Code") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = toCode, onValueChange = { toCode = it.uppercase() }, label = { Text("To Code") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("USD") }, modifier = Modifier.weight(1f))
                                }
                                Button(
                                    onClick = {
                                        val p = priceStr.toDoubleOrNull() ?: 500.0
                                        vm.adminAddFlight(
                                            FlightOfferEntity(
                                                airlineName = airline, airlineCode = flightNum.take(2), flightNumber = flightNum,
                                                originCode = fromCode, originCity = fromCode, destinationCode = toCode, destinationCity = toCode,
                                                isDomestic = toCode in listOf("HGA", "KMX", "BBO", "MGQ"),
                                                departureTime = "09:00", arrivalTime = "13:30", durationLabel = "4h 30m",
                                                baggageAllowance = "30kg Checked",
                                                economyPriceUsd = p, premiumEconomyPriceUsd = p * 1.35, businessPriceUsd = p * 2.4, firstClassPriceUsd = p * 3.5,
                                                seatsAvailable = 20
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Publish Flight Offer") }
                            }
                        }
                    }

                    item {
                        var blogTitle by remember { mutableStateOf("") }
                        var blogBody by remember { mutableStateOf("") }
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Publish Blog / Travel News", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(value = blogTitle, onValueChange = { blogTitle = it }, label = { Text("Headline") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = blogBody, onValueChange = { blogBody = it }, label = { Text("Article Body") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        if (blogTitle.isNotBlank() && blogBody.isNotBlank()) {
                                            vm.adminAddBlogPost(blogTitle, "SOM TREVEL Editorial", blogBody)
                                            blogTitle = ""
                                            blogBody = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Publish Article") }
                            }
                        }
                    }

                    item { Text("Active Hotels (${allHotels.size}) & Flights (${allFlights.size})", style = MaterialTheme.typography.titleMedium) }
                    items(allFlights) { f ->
                        ListItem(
                            headlineContent = { Text("${f.airlineName} ${f.flightNumber}") },
                            supportingContent = { Text("${f.originCode} → ${f.destinationCode} • $${f.economyPriceUsd.toInt()}") },
                            trailingContent = {
                                IconButton(onClick = { vm.adminDeleteFlight(f.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete flight")
                                }
                            }
                        )
                    }
                }

                "MEDIA" -> {
                    item {
                        var mediaTitle by remember { mutableStateOf("Executive Fleet Photo") }
                        var mediaCat by remember { mutableStateOf("Gallery") }
                        val picker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickVisualMedia(),
                            onResult = { uri -> if (uri != null) vm.adminUploadMedia(mediaTitle, mediaCat, uri) }
                        )
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Media & Image Upload Manager", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(value = mediaTitle, onValueChange = { mediaTitle = it }, label = { Text("Image Title") }, modifier = Modifier.fillMaxWidth())
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Gallery", "Destination", "Hotel", "Airplane", "Hajj & Umrah", "Blog", "Banner").forEach { cat ->
                                        FilterChip(selected = mediaCat == cat, onClick = { mediaCat = cat }, label = { Text(cat) })
                                    }
                                }
                                Button(
                                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_media_picker_btn")
                                ) {
                                    Icon(Icons.Filled.AddPhotoAlternate, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Upload Photograph via Photo Picker")
                                }
                            }
                        }
                    }
                    items(mediaAssets) { m ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CoverArtworkImage(m.drawableKey, m.localUri, m.title, Modifier.size(72.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(m.title, fontWeight = FontWeight.Bold)
                                    Text(m.category, style = MaterialTheme.typography.labelSmall, color = RoyalBlueLight)
                                }
                                IconButton(onClick = { vm.adminDeleteMedia(m.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete media")
                                }
                            }
                        }
                    }
                }

                "GOVERNANCE" -> {
                    item { Text("Customer Support & Service Queue", style = MaterialTheme.typography.titleLarge) }
                    items(allRequests) { req ->
                        var reply by remember(req.id) { mutableStateOf(req.adminReply) }
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${req.serviceType}: ${req.subject}", fontWeight = FontWeight.Bold)
                                    StatusPill(req.status)
                                }
                                Text("${req.customerName}: ${req.details}")
                                OutlinedTextField(value = reply, onValueChange = { reply = it }, label = { Text("Agency Reply") }, modifier = Modifier.fillMaxWidth())
                                Button(onClick = { vm.adminReplyRequest(req, "Resolved", reply) }) { Text("Send Reply & Resolve") }
                            }
                        }
                    }

                    if (role.canManageAdminSettings) {
                        item { Text("User Roles & RBAC Permissions", style = MaterialTheme.typography.titleLarge) }
                        items(allUsers) { u ->
                            ElevatedCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("${u.fullName} (${u.email})", fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        UserRole.entries.forEach { r ->
                                            FilterChip(
                                                selected = u.role == r.name,
                                                onClick = { vm.adminChangeUserRole(u, r) },
                                                label = { Text(r.displayName) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Text("Security Audit Logs", style = MaterialTheme.typography.titleLarge) }
                    items(auditLogs.take(15)) { log ->
                        ListItem(
                            headlineContent = { Text("${log.action} • ${log.actorName}") },
                            supportingContent = { Text("${log.actorRole} — ${log.targetSummary}") }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmPurgeDemo) {
        AlertDialog(
            onDismissRequest = { showConfirmPurgeDemo = false },
            title = { Text("Remove Demo Data Safely?") },
            text = { Text("This deletes all records tagged DEMO (sample flights, hotels, demo bookings, demo accounting entries) while preserving every real customer account and booking.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.adminPurgeDemoData()
                        showConfirmPurgeDemo = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCancelled, contentColor = PureWhite)
                ) { Text("Remove Demo Records") }
            },
            dismissButton = { TextButton(onClick = { showConfirmPurgeDemo = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
