package com.taska.android.ui.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskItemTest {

    @Test
    fun `meeting task has an accessible meeting label`() {
        assertEquals("Réunion", taskTypeAccessibilityLabel("MEETING"))
    }

    @Test
    fun `todo and legacy tasks retain the todo label`() {
        assertEquals("À faire", taskTypeAccessibilityLabel("TODO"))
        assertEquals("À faire", taskTypeAccessibilityLabel(null))
    }

    @Test
    fun `only meeting type uses the meeting icon`() {
        org.junit.Assert.assertTrue(isMeetingTask("MEETING"))
        org.junit.Assert.assertFalse(isMeetingTask("TODO"))
        org.junit.Assert.assertFalse(isMeetingTask(null))
    }
}
