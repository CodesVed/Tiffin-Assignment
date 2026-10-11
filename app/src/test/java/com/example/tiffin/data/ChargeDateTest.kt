package com.example.tiffin.data

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargeDateTest {

    private val kolkata = TimeZone.getTimeZone("Asia/Kolkata")
    private val utc = TimeZone.getTimeZone("UTC")
    private val newYork = TimeZone.getTimeZone("America/New_York")
    private val locale = Locale.US

    private fun millisOf(zoneId: String, y: Int, mo: Int, d: Int, h: Int, min: Int): Long =
        ZonedDateTime.of(y, mo, d, h, min, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()

    @Test
    fun chargeTime_isExactly24HoursAfterPurchase() {
        val purchase = 1_000_000L
        assertEquals(purchase + 86_400_000L, chargeTimeMillis(purchase))
    }

    @Test
    fun chargeDate_isTheNextCalendarDay_inIst() {
        val purchase = millisOf("Asia/Kolkata", 2026, 10, 10, 15, 30)
        assertEquals("Sunday, 11 October 2026", formatDate(chargeTimeMillis(purchase), kolkata, locale))
    }

    @Test
    fun chargeDate_justBeforeMidnight_isStillNextDay() {
        val purchase = millisOf("Asia/Kolkata", 2026, 10, 10, 23, 50)
        assertEquals("Sunday, 11 October 2026", formatDate(chargeTimeMillis(purchase), kolkata, locale))
    }

    @Test
    fun sameInstant_formatsToDifferentDates_inDifferentTimeZones() {
        // 20:00 UTC on 10 Oct -> charge at 20:00 UTC on 11 Oct, which is 01:30 on 12 Oct in India.
        val purchase = millisOf("UTC", 2026, 10, 10, 20, 0)
        val charge = chargeTimeMillis(purchase)
        assertEquals("Sunday, 11 October 2026", formatDate(charge, utc, locale))
        assertEquals("Monday, 12 October 2026", formatDate(charge, kolkata, locale))
    }

    @Test
    fun clocksGoingBack_stillMeansExactly24HoursNotSameWallClockTime() {
        // New York clocks go back on 1 Nov 2026. 23:30 EDT + 24h = 22:30 EST the next day.
        val purchase = millisOf("America/New_York", 2026, 10, 31, 23, 30)
        val text = formatDateTime(chargeTimeMillis(purchase), newYork, locale)
        assertTrue(text, text.startsWith("Sunday, 1 November 2026 at 10:30 PM"))
    }
}