package com.taska.android.widget

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WidgetTextTest {
    private val utc = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 6, 17)

    @Test fun `incomplete task before the local current date is overdue`() {
        assertTrue(widgetTaskTextStyle("2026-06-16T23:59:59Z", false, utc, today).overdue)
    }

    @Test fun `task scheduled today keeps its normal style`() {
        assertFalse(widgetTaskTextStyle("2026-06-17T00:00:00Z", false, utc, today).overdue)
    }

    @Test fun `completed task keeps its normal style even when scheduled earlier`() {
        assertFalse(widgetTaskTextStyle("2026-06-16T00:00:00Z", true, utc, today).overdue)
    }

    @Test fun `overdue classification observes the device local date`() {
        assertTrue(widgetTaskTextStyle("2026-06-16T23:30:00Z", false, ZoneId.of("Europe/Paris"), LocalDate.of(2026, 6, 18)).overdue)
        assertFalse(widgetTaskTextStyle("2026-06-16T23:30:00Z", false, ZoneId.of("America/New_York"), LocalDate.of(2026, 6, 16)).overdue)
    }
}
