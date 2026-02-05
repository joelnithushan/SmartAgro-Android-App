package com.example.smartagro.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val readableFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a").withZone(ZoneId.systemDefault())

fun formatTimestamp(ts: Long?): String {
    if (ts == null) return "Unknown"
    if (ts <= 0L) return "Unknown"

    val oneBillion = 1_000_000_000L
    val oneTrillion = 1_000_000_000_000L

    // If it's device uptime (millis from ESP32), show "Just now" since we track it separately
    if (ts < oneBillion) {
        return "Just now"
    }

    val epochMillis = if (ts < oneTrillion) ts * 1000L else ts

    val year2000 = 946684800000L
    val year2100 = 4102444800000L

    if (epochMillis !in year2000..year2100) {
        return "Just now"
    }

    return try {
        readableFormatter.format(Instant.ofEpochMilli(epochMillis))
    } catch (_: Exception) {
        "Just now"
    }
}

