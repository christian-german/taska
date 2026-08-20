package com.taska.android.ui.taskdetail

import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TaskDetailDateFormatterTest {
    private val paris = ZoneId.of("Europe/Paris")
    private val today = LocalDate.of(2026, 8, 20)

    @Test
    fun `absent schedule has no display value regardless of all-day metadata`() {
        assertEquals(
            null,
            formatScheduledTaskDetailDate(
                null, allDay = false, zone = paris, locale = Locale.FRENCH, today = today
            )
        )
        assertEquals(
            null,
            formatScheduledTaskDetailDate(
                null, allDay = true, zone = paris, locale = Locale.FRENCH, today = today
            )
        )
    }

    @Test
    fun `all-day schedule displays its localized date without a time`() {
        val result = formatTaskDetailDate(
            "2026-09-01T00:00:00Z", includeTime = false, zone = paris,
            locale = Locale.FRENCH, today = today
        )

        assertEquals("1 sept.", result)
        assertFalse(result.contains("02:00"))
    }

    @Test
    fun `timed schedule displays its localized date and time`() {
        val result = formatTaskDetailDate(
            "2026-09-01T07:15:00Z", includeTime = true, zone = paris,
            locale = Locale.FRENCH, today = today
        )

        assertEquals("1 sept. · 09:15", result)
    }

    @Test
    fun `deadline is date-only in a non-UTC time zone`() {
        val result = formatTaskDetailDate(
            "2026-08-19T23:30:00Z", includeTime = false, zone = paris,
            locale = Locale.FRENCH, today = today
        )

        assertEquals("aujourd'hui", result)
        assertFalse(result.contains(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun `invalid value retains the existing date fallback`() {
        val result = formatTaskDetailDate(
            "2026-09-01Tinvalid", includeTime = true, zone = paris,
            locale = Locale.FRENCH, today = today
        )

        assertEquals("2026-09-01", result)
    }
}
