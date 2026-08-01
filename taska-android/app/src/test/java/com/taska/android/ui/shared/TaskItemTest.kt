package com.taska.android.ui.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskItemTest {

    @Test
    fun `appointment task has an accessible appointment label`() {
        assertEquals("Rendez-vous", taskTypeAccessibilityLabel("APPOINTMENT"))
    }

    @Test
    fun `todo and legacy tasks retain the todo label`() {
        assertEquals("À faire", taskTypeAccessibilityLabel("TODO"))
        assertEquals("À faire", taskTypeAccessibilityLabel(null))
    }

    @Test
    fun `only appointment type uses the appointment icon`() {
        org.junit.Assert.assertTrue(isAppointmentTask("APPOINTMENT"))
        org.junit.Assert.assertFalse(isAppointmentTask("TODO"))
        org.junit.Assert.assertFalse(isAppointmentTask(null))
    }
}
