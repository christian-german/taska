package com.taska.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskRequestTest {

    @Test
    fun `appointment creation request includes appointment type`() {
        assertEquals("APPOINTMENT", TaskRequest(content = "Planning", type = "APPOINTMENT").type)
    }

    @Test
    fun `task update request can change an appointment back to todo`() {
        assertEquals("TODO", TaskRequest(content = "Planning", type = "TODO").type)
    }
}
