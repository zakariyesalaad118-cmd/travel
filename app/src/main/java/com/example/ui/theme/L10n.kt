package com.example.ui.theme

import com.example.data.CurrencyRateEntity
import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "EN"),
    SO("so", "SO")
}

object L10n {
    fun text(lang: AppLanguage, en: String, so: String): String =
        if (lang == AppLanguage.SO) so else en

    fun navHome(lang: AppLanguage) = text(lang, "Home", "Bogga Hore")
    fun navExplore(lang: AppLanguage) = text(lang, "Services", "Adeegyada")
    fun navTickets(lang: AppLanguage) = text(lang, "Tickets", "Tigidhada")
    fun navPortal(lang: AppLanguage) = text(lang, "Portal", "Xisaabta")
    fun navAdmin(lang: AppLanguage) = text(lang, "Admin", "Maamulka")
}

object CurrencyFormatter {
    fun format(
        amountUsd: Double,
        currencyCode: String,
        rates: List<CurrencyRateEntity>
    ): String {
        val rate = rates.firstOrNull { it.code == currencyCode }
        val multiplier = rate?.rateFromUsd ?: 1.0
        val converted = amountUsd * multiplier
        val symbol = rate?.symbol ?: "$"
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = if (currencyCode == "SOS") 0 else 2
            minimumFractionDigits = if (currencyCode == "SOS") 0 else 0
        }
        return "$symbol${nf.format(converted)}"
    }
}
