package com.taska.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskRequestTest {

    @Test
    fun `meeting creation request includes meeting type`() {
        assertEquals("MEETING", TaskRequest(content = "Planning", type = "MEETING").type)
    }

    @Test
    fun `task update request can change a meeting back to todo`() {
        assertEquals("TODO", TaskRequest(content = "Planning", type = "TODO").type)
    }
}
