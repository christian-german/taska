package com.taska.android.ui.shared

import android.content.Context
import android.widget.Toast
import com.taska.android.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class TaskCreationFeedbackTest {
    @Before
    fun setUp() {
        mockkStatic(Toast::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Toast::class)
    }

    @Test
    fun `show presents a short task-created toast`() {
        val applicationContext = mockk<Context>()
        val context = mockk<Context> { every { this@mockk.applicationContext } returns applicationContext }
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(applicationContext, R.string.task_created, Toast.LENGTH_SHORT) } returns toast

        TaskCreationFeedback.show(context)

        verify(exactly = 1) { toast.show() }
    }
}
