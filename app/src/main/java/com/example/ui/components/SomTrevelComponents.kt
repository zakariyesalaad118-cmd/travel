package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CurrencyRateEntity
import com.example.data.FlightBookingEntity
import com.example.ui.theme.*
import java.io.File

@Composable
fun CoverArtworkImage(
    coverKey: String,
    customUri: String? = null,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (!customUri.isNullOrBlank()) {
        AsyncImage(
            model = File(customUri),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val resId = when (coverKey.lowercase()) {
            "mogadishu", "somalia", "berbera" -> R.drawable.img_destination_mogadishu
            "makkah", "madinah", "hajj", "umrah" -> R.drawable.img_hajj_makkah
            "hotel", "dubai", "maldives" -> R.drawable.img_luxury_hotel
            else -> R.drawable.img_hero_aviation
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val normalized = status.lowercase()
    val (bg, fg) = when {
        normalized in listOf("issued", "confirmed", "approved", "paid", "resolved") ->
            EmeraldContainer to EmeraldConfirmed
        normalized in listOf("cancelled", "rejected", "refunded") ->
            CrimsonContainer to CrimsonCancelled
        else ->
            AmberContainer to AmberPending
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LuxuryBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = LuxuryGold.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, LuxuryGold),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = LuxuryGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SomTrevelTopBar(
    language: AppLanguage,
    currencyCode: String,
    darkMode: Boolean,
    onToggleLanguage: () -> Unit,
    onSelectCurrency: (String) -> Unit,
    onToggleDarkMode: () -> Unit
) {
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = DeepNavy,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(LuxuryGold.copy(alpha = 0.16f))
                        .border(1.dp, LuxuryGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlightTakeoff,
                        contentDescription = "SOM TREVEL emblem",
                        tint = LuxuryGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SOM TREVEL",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = L10n.text(language, "GLOBAL LUXURY TRAVEL", "SAFARKA CAALAMIGA AH"),
                        style = MaterialTheme.typography.labelSmall,
                        color = LuxuryGold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Currency Switcher
                Box {
                    Surface(
                        color = RoyalBlue,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .testTag("currency_selector_button")
                            .clickable { currencyMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currencyCode, style = MaterialTheme.typography.labelSmall, color = LuxuryGold)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Currency", tint = PureWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = currencyMenuExpanded,
                        onDismissRequest = { currencyMenuExpanded = false }
                    ) {
                        listOf("USD", "EUR", "SAR", "SOS").forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    onSelectCurrency(code)
                                    currencyMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Bilingual Switcher EN / SO
                Surface(
                    color = RoyalBlue,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .testTag("language_toggle_button")
                        .clickable { onToggleLanguage() }
                ) {
                    Text(
                        text = language.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }

                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = LuxuryGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AirlineTicketCard(
    booking: FlightBookingEntity,
    currencyCode: String,
    rates: List<CurrencyRateEntity>,
    onDownloadPdf: () -> Unit,
    onPrintPdf: () -> Unit,
    onRequestAction: ((String) -> Unit)? = null
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ticket_card_${booking.pnr}")
    ) {
        Column {
            // Top Boarding Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(DeepNavy, RoyalBlue)))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${booking.airlineName} • ${booking.flightNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            color = LuxuryGold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "PNR ${booking.pnr}",
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite
                        )
                    }
                    StatusPill(status = booking.ticketStatus)
                }
            }

            // Route & Schedule
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(booking.originCode, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text(booking.originCity, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(booking.departureTime, style = MaterialTheme.typography.labelLarge)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Flight, contentDescription = null, tint = LuxuryGold)
                        Text(booking.cabinClass, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(booking.tripType.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = RoyalBlueLight)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(booking.destinationCode, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Text(booking.destinationCity, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(booking.arrivalTime, style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${booking.customerName} • ${booking.departureDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${booking.baggageAllowance} • Payment: ${booking.paymentStatus} (${CurrencyFormatter.format(booking.totalUsd, currencyCode, rates)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Perforated Divider
                Spacer(Modifier.height(12.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                    drawLine(
                        color = SlateBorder,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                }
                Spacer(Modifier.height(12.dp))

                // Barcode graphic
                Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
                    var x = 0f
                    val seed = booking.pnr.ifBlank { "ST0000" }
                    var idx = 0
                    while (x < size.width - 6f) {
                        val barW = if ((seed[idx % seed.length].code + idx) % 3 == 0) 5f else 2.5f
                        drawRect(
                            color = DeepNavy,
                            topLeft = Offset(x, 0f),
                            size = androidx.compose.ui.geometry.Size(barW, size.height)
                        )
                        x += barW + 4.5f
                        idx++
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownloadPdf,
                        modifier = Modifier.weight(1f).testTag("download_pdf_${booking.pnr}"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = PureWhite)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PDF E-Ticket")
                    }
                    OutlinedButton(
                        onClick = onPrintPdf,
                        modifier = Modifier.weight(1f).testTag("print_pdf_${booking.pnr}")
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Print")
                    }
                }

                if (onRequestAction != null && booking.ticketStatus != "Cancelled" && booking.ticketStatus != "Refunded") {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { onRequestAction("Modification") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Request Change") }
                        TextButton(
                            onClick = { onRequestAction("Cancellation") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Request Cancel", color = CrimsonCancelled) }
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueExpenseChart(
    items: List<Pair<String, Double>>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = (items.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { (label, value) ->
            val fraction = (value / maxVal).toFloat().coerceIn(0.04f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(108.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(50))
                            .background(accentColor)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$${value.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Luggage, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
