package com.taska.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskCreateRequestTest {

    @Test
    fun `appointment creation request includes appointment type`() {
        assertEquals("APPOINTMENT", TaskCreateRequest(content = "Planning", type = "APPOINTMENT").type)
    }

    @Test
    fun `todo creation request includes todo type`() {
        assertEquals("TODO", TaskCreateRequest(content = "Planning", type = "TODO").type)
    }
}
