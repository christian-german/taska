package com.taska.android.ui.shared

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class MobileDatePickerLocaleTest {
    @Test
    fun `mobile date picker week starts on Monday and ends on Sunday`() {
        val calendar = Calendar.getInstance(MobileDatePickerLocale)

        assertEquals(Calendar.MONDAY, calendar.firstDayOfWeek)
        assertEquals(Calendar.SUNDAY, lastDayOfWeek(calendar.firstDayOfWeek))
    }

    private fun lastDayOfWeek(firstDay: Int): Int =
        ((firstDay - Calendar.SUNDAY + 6) % 7) + Calendar.SUNDAY
}
