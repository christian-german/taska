package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TaskWidgetCompletionReceiverTest {
    private val repository = mockk<TaskRepository>()

    @Test fun `reopens a completed non-recurring task`() = runTest {
        coEvery { repository.reopenTask("task-1", null) } returns task("task-1")

        TaskWidgetCompletionReceiver.performAction(
            repository,
            TaskWidgetCompletionReceiver.ACTION_REOPEN,
            "task-1",
            null,
        )

        coVerify(exactly = 1) { repository.reopenTask("task-1", null) }
        coVerify(exactly = 0) { repository.closeTask(any(), any()) }
    }

    @Test fun `reopens only the selected recurring occurrence`() = runTest {
        val occurrence = "2026-08-04T09:00:00Z"
        coEvery { repository.reopenTask("task-2", occurrence) } returns task("task-2")

        TaskWidgetCompletionReceiver.performAction(
            repository,
            TaskWidgetCompletionReceiver.ACTION_REOPEN,
            "task-2",
            occurrence,
        )

        coVerify(exactly = 1) { repository.reopenTask("task-2", occurrence) }
    }

    @Test fun `does not swallow a rejected reopen action`() = runTest {
        coEvery { repository.reopenTask("task-3", null) } throws IllegalStateException("rejected")

        org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                TaskWidgetCompletionReceiver.performAction(
                    repository,
                    TaskWidgetCompletionReceiver.ACTION_REOPEN,
                    "task-3",
                    null,
                )
            }
        }
    }

    private fun task(id: String) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = false,
        scheduledAt = null, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null,
    )
}
