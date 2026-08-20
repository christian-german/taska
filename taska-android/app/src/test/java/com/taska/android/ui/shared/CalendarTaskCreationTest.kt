package com.taska.android.ui.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarTaskCreationTest {
    @Test
    fun `successful creation dismisses the sheet and refreshes the current calendar`() {
        val events = mutableListOf<String>()

        handleCalendarTaskCreated(
            dismissTaskCreation = { events += "dismiss" },
            refreshCalendar = { events += "refresh" },
        )

        assertEquals(listOf("dismiss", "refresh"), events)
    }
}
