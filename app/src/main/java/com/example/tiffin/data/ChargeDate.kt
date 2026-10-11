package com.example.tiffin.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The second charge happens exactly 24 hours after purchase: a fixed duration,
 * not "the same clock time tomorrow" (those differ when clocks change).
 */
const val CHARGE_DELAY_MILLIS: Long = 24L * 60L * 60L * 1000L

fun chargeTimeMillis(purchasedAtMillis: Long): Long = purchasedAtMillis + CHARGE_DELAY_MILLIS

/** e.g. "Sunday, 11 October 2026" */
fun formatDate(
    millis: Long,
    zone: TimeZone = TimeZone.getDefault(),
    locale: Locale = Locale.getDefault(),
): String = formatWithPattern("EEEE, d MMMM yyyy", millis, zone, locale)

/** e.g. "Sunday, 11 October 2026 at 3:30 PM IST" */
fun formatDateTime(
    millis: Long,
    zone: TimeZone = TimeZone.getDefault(),
    locale: Locale = Locale.getDefault(),
): String = formatWithPattern("EEEE, d MMMM yyyy 'at' h:mm a z", millis, zone, locale)

private fun formatWithPattern(pattern: String, millis: Long, zone: TimeZone, locale: Locale): String {
    // SimpleDateFormat is not thread-safe and remembers the zone it was built with,
    // so build a fresh one per call and read the default zone at call time.
    val formatter = SimpleDateFormat(pattern, locale)
    formatter.timeZone = zone
    return formatter.format(Date(millis))
}