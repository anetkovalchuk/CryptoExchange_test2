package com.example.cryptoexchange.presentation

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatUsdPrice(priceUsd: Double?): String {
    if (priceUsd == null || priceUsd.isNaN() || priceUsd.isInfinite()) {
        return "Немає даних"
    }
    return when {
        priceUsd == 0.0 -> "$0.00"
        priceUsd < 0.0001 -> "$" + String.format(Locale.US, "%.8f", priceUsd).trimEnd('0').trimEnd('.')
        priceUsd < 0.01 -> "$" + String.format(Locale.US, "%.6f", priceUsd).trimEnd('0').trimEnd('.')
        priceUsd < 1.0 -> "$" + String.format(Locale.US, "%.4f", priceUsd)
        else -> "$" + String.format(Locale.US, "%,.2f", priceUsd)
    }
}

fun formatUpdatedTimestamp(millis: Long?): String {
    if (millis == null || millis <= 0L) {
        return "Ще не оновлювалося"
    }
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(millis))
}