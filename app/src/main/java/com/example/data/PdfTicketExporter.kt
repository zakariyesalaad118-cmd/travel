package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfTicketExporter {

    private fun generateTicketPdfFile(context: Context, booking: FlightBookingEntity): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val navyPaint = Paint().apply { color = Color.rgb(7, 27, 54) }
        val royalPaint = Paint().apply { color = Color.rgb(21, 63, 112) }
        val goldPaint = Paint().apply { color = Color.rgb(214, 173, 96) }
        val softGrayPaint = Paint().apply { color = Color.rgb(244, 246, 249) }
        val borderPaint = Paint().apply {
            color = Color.rgb(210, 218, 228)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val titleWhite = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val goldLabel = Paint().apply {
            color = Color.rgb(214, 173, 96)
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val darkHeading = Paint().apply {
            color = Color.rgb(7, 27, 54)
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val mutedPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val monoBig = Paint().apply {
            color = Color.rgb(7, 27, 54)
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // Header Band
        canvas.drawRect(0f, 0f, 595f, 115f, navyPaint)
        canvas.drawRect(0f, 111f, 595f, 115f, goldPaint)
        canvas.drawText("SOM TREVEL", 36f, 48f, titleWhite)
        canvas.drawText("LUXURY AVIATION & GLOBAL TRAVEL AGENCY • ELECTRONIC TICKET", 36f, 70f, goldLabel)
        canvas.drawText("PNR: ${booking.pnr}", 430f, 52f, titleWhite)
        canvas.drawText("STATUS: ${booking.ticketStatus.uppercase()}", 430f, 74f, goldLabel)

        // Route Banner Card
        canvas.drawRoundRect(36f, 140f, 559f, 265f, 14f, 14f, softGrayPaint)
        canvas.drawRoundRect(36f, 140f, 559f, 265f, 14f, 14f, borderPaint)

        canvas.drawText("FROM", 56f, 168f, mutedPaint)
        canvas.drawText(booking.originCode, 56f, 200f, monoBig)
        canvas.drawText(booking.originCity, 56f, 222f, bodyPaint)

        canvas.drawText("TO", 340f, 168f, mutedPaint)
        canvas.drawText(booking.destinationCode, 340f, 200f, monoBig)
        canvas.drawText(booking.destinationCity, 340f, 222f, bodyPaint)

        canvas.drawLine(180f, 192f, 315f, 192f, royalPaint)
        canvas.drawText("${booking.airlineName} • ${booking.flightNumber}", 180f, 182f, goldLabel.apply { color = Color.rgb(21, 63, 112) })
        canvas.drawText("${booking.departureTime} → ${booking.arrivalTime}", 195f, 212f, bodyPaint)
        canvas.drawText("Departure: ${booking.departureDate}   |   Cabin: ${booking.cabinClass}", 56f, 248f, bodyPaint)

        // Passenger & Details
        var y = 305f
        canvas.drawText("PASSENGER & RESERVATION DETAILS", 36f, y, darkHeading)
        y += 22f
        val rows = listOf(
            "Primary Passenger" to booking.customerName,
            "Passenger Breakdown" to booking.passengerSummary,
            "Trip Type" to booking.tripType.replace("_", " "),
            "Return Date" to (booking.returnDate.ifBlank { "One-Way Itinerary" }),
            "Baggage Allowance" to booking.baggageAllowance,
            "Payment Status" to booking.paymentStatus,
            "Total Fare" to "$${String.format(Locale.US, "%.2f", booking.totalUsd)} USD",
            "Special Remarks" to (booking.notes.ifBlank { "Standard SOM TREVEL concierge care" })
        )
        rows.forEach { (label, value) ->
            canvas.drawText(label.uppercase(), 36f, y, mutedPaint)
            canvas.drawText(value, 185f, y, bodyPaint)
            y += 24f
        }

        // Barcode Simulation Strip
        y += 20f
        canvas.drawRect(36f, y, 559f, y + 64f, softGrayPaint)
        canvas.drawRect(36f, y, 559f, y + 64f, borderPaint)
        var bx = 56f
        booking.pnr.forEachIndexed { idx, ch ->
            val count = (ch.code % 5) + 3
            for (k in 0 until count) {
                val w = if ((idx + k) % 2 == 0) 3.5f else 1.8f
                canvas.drawRect(bx, y + 12f, bx + w, y + 48f, navyPaint)
                bx += w + 3.2f
            }
        }
        canvas.drawText("E-TKT REF: SOM-${booking.pnr}-${booking.id}", 330f, y + 36f, darkHeading)

        // Footer Notice
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        canvas.drawText(
            "Agency Record Verification: SOM TREVEL Internal System • Generated $dateStr",
            36f, 785f, mutedPaint
        )
        canvas.drawText(
            "Please present passport and valid travel visas at airport check-in 3 hours prior to departure.",
            36f, 802f, mutedPaint
        )

        doc.finishPage(page)

        val dir = File(context.filesDir, "tickets").apply { mkdirs() }
        val file = File(dir, "SOM_TREVEL_${booking.pnr}.pdf")
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()
        return file
    }

    fun shareTicketPdf(context: Context, booking: FlightBookingEntity) {
        val file = generateTicketPdfFile(context, booking)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SOM TREVEL E-Ticket • PNR ${booking.pnr}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share or Save E-Ticket PDF"))
    }

    fun printTicketPdf(context: Context, booking: FlightBookingEntity) {
        val file = generateTicketPdfFile(context, booking)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("SOM_TREVEL_${booking.pnr}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                try {
                    FileInputStream(file).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                }
            }
        }
        printManager.print("SOM_TREVEL_Ticket_${booking.pnr}", adapter, PrintAttributes.Builder().build())
    }

    fun shareFinancialReportPdf(
        context: Context,
        totalRevenue: Double,
        totalExpenses: Double,
        netProfit: Double,
        transactions: List<FinancialTransactionEntity>
    ) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val navy = Paint().apply { color = Color.rgb(7, 27, 54) }
        val gold = Paint().apply { color = Color.rgb(214, 173, 96) }
        val whiteTitle = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val heading = Paint().apply {
            color = Color.rgb(7, 27, 54)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val body = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10.5f
            isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, 595f, 90f, navy)
        canvas.drawRect(0f, 86f, 595f, 90f, gold)
        canvas.drawText("SOM TREVEL — PROFIT & LOSS FINANCIAL REPORT", 36f, 48f, whiteTitle)
        canvas.drawText("Generated ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}", 36f, 70f, Paint().apply {
            color = Color.rgb(214, 173, 96)
            textSize = 10.5f
        })

        canvas.drawText("Total Revenue: $${String.format(Locale.US, "%.2f", totalRevenue)} USD", 36f, 124f, heading)
        canvas.drawText("Total Expenses: $${String.format(Locale.US, "%.2f", totalExpenses)} USD", 36f, 146f, heading)
        canvas.drawText("Net Profit: $${String.format(Locale.US, "%.2f", netProfit)} USD", 36f, 168f, heading)

        var y = 206f
        canvas.drawText("LEDGER ENTRIES (LATEST 24)", 36f, y, heading)
        y += 20f
        transactions.take(24).forEach { tx ->
            val sign = if (tx.type == "INCOME") "+" else "-"
            canvas.drawText("${tx.referenceCode} | ${tx.category} | $sign$${String.format(Locale.US, "%.2f", tx.amountUsd)}", 36f, y, body)
            y += 16f
            canvas.drawText("   ${tx.description.take(78)}", 36f, y, Paint().apply { color = Color.GRAY; textSize = 9.5f })
            y += 18f
        }

        doc.finishPage(page)
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "SOM_TREVEL_PL_Report.pdf")
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SOM TREVEL Financial P&L Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Financial PDF Report"))
    }
}
