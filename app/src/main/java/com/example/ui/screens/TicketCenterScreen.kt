package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.PdfTicketExporter
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomTrevelViewModel
import com.example.ui.viewmodel.TicketVerifyState

@Composable
fun TicketCenterScreen(
    vm: SomTrevelViewModel,
    onGoToPortal: () -> Unit
) {
    val lang by vm.language.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val rates by vm.currencyRates.collectAsState()
    val currentUser by vm.currentUser.collectAsState()
    val myTickets by vm.myFlightBookings.collectAsState()
    val verifyState by vm.ticketVerifyState.collectAsState()

    val context = LocalContext.current

    var pnrInput by remember { mutableStateOf("") }
    var surnameInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. PNR Verification Desk (Clearly distinguished from live airline check-in)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = LuxuryGoldDark)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = L10n.text(lang, "Agency Ticket Verification", "Hubinta Tigidhka SOM TREVEL"),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Surface(
                        color = AmberContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Internal Verification Notice: Checks booking records stored in SOM TREVEL's system (try demo PNR ST884K + Warsame). Live airport check-in is performed with the operating airline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberPending,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = pnrInput,
                            onValueChange = { pnrInput = it.uppercase() },
                            label = { Text("PNR (e.g. ST884K)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("verify_pnr_input")
                        )
                        OutlinedTextField(
                            value = surnameInput,
                            onValueChange = { surnameInput = it },
                            label = { Text("Surname (e.g. Warsame)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("verify_surname_input")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { vm.verifyTicket(pnrInput, surnameInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("verify_ticket_submit")
                        ) {
                            Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Verify Record")
                        }
                        if (verifyState !is TicketVerifyState.Idle) {
                            OutlinedButton(onClick = {
                                pnrInput = ""
                                surnameInput = ""
                                vm.resetVerification()
                            }) { Text("Clear") }
                        }
                    }

                    when (val st = verifyState) {
                        is TicketVerifyState.Idle -> Unit
                        is TicketVerifyState.NotFound -> {
                            Surface(color = CrimsonContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(st.queryInfo, color = CrimsonCancelled, modifier = Modifier.padding(12.dp))
                            }
                        }
                        is TicketVerifyState.Found -> {
                            Surface(color = EmeraldContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("✓ Record Verified in SOM TREVEL System", color = EmeraldConfirmed, fontWeight = FontWeight.Bold)
                                    Text("${st.booking.customerName} • ${st.booking.airlineName} ${st.booking.flightNumber}", color = DarkText)
                                    Text("${st.booking.originCode} → ${st.booking.destinationCode} on ${st.booking.departureDate} • Status: ${st.booking.ticketStatus}", color = DarkText)
                                }
                            }
                            AirlineTicketCard(
                                booking = st.booking,
                                currencyCode = prefs.currencyCode,
                                rates = rates,
                                onDownloadPdf = { PdfTicketExporter.shareTicketPdf(context, st.booking) },
                                onPrintPdf = { PdfTicketExporter.printTicketPdf(context, st.booking) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Customer's Personal E-Tickets
        item {
            Text(
                text = L10n.text(lang, "My Electronic Tickets", "Tigidhadaada Elektarooniga ah"),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (currentUser == null) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.LockPerson, contentDescription = null, tint = LuxuryGoldDark, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Customer Portal Sign-In Required", style = MaterialTheme.typography.titleMedium)
                        Text("Personal flight tickets are isolated per customer account.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onGoToPortal) { Text("Sign In on Portal") }
                    }
                }
            }
        } else if (myTickets.isEmpty()) {
            item { EmptyStateCard("No Tickets Yet", "Book an international or domestic flight in the Services tab.") }
        } else {
            items(myTickets) { ticket ->
                AirlineTicketCard(
                    booking = ticket,
                    currencyCode = prefs.currencyCode,
                    rates = rates,
                    onDownloadPdf = { PdfTicketExporter.shareTicketPdf(context, ticket) },
                    onPrintPdf = { PdfTicketExporter.printTicketPdf(context, ticket) },
                    onRequestAction = { action -> vm.requestTicketAction(ticket, action) }
                )
            }
        }
    }
}
