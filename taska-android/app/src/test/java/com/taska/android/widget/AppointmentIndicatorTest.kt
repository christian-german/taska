package com.taska.android.widget

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppointmentIndicatorTest {
    @Test fun `appointment type shows widget indicator`() {
        assertTrue(isAppointmentIndicatorVisible("APPOINTMENT"))
    }

    @Test fun `todo missing and unknown types hide widget indicator`() {
        assertFalse(isAppointmentIndicatorVisible("TODO"))
        assertFalse(isAppointmentIndicatorVisible(null))
        assertFalse(isAppointmentIndicatorVisible("OTHER"))
    }
}
