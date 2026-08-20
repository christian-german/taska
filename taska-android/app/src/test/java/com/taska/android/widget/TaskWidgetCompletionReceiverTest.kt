package com.taska.android.widget

import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
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

    @Test fun `checks only the selected occurrence in both widget types before its request`() {
        val presentation = mockk<CompletionPresentation>(relaxed = true)
        val targets = CompletionWidget.entries.mapIndexed { index, widget ->
            CompletionTarget(widget, 41 + index, 12 + index, "recurring-task", "2026-08-04T09:00:00Z")
        }

        targets.forEach { TaskWidgetCompletionReceiver.beginCompletion(presentation, it) }

        targets.forEach { target -> verify(exactly = 1) { presentation.showChecked(target) } }
        verify(exactly = 0) { presentation.showUnchecked(any()) }
    }

    @Test fun `successful today completion refreshes and clears optimistic presentation`() = runTest {
        val presentation = mockk<CompletionPresentation>(relaxed = true)
        val target = CompletionTarget(CompletionWidget.TODAY, 7, 99, "task-4", null)
        var refreshes = 0
        coEvery { repository.closeTask("task-4", null) } returns task("task-4")

        TaskWidgetCompletionReceiver.executeAction(
            repository,
            TaskWidgetCompletionReceiver.ACTION_COMPLETE,
            "task-4",
            null,
            target,
            presentation,
        ) { refreshes++ }

        coVerify(exactly = 1) { repository.closeTask("task-4", null) }
        org.junit.Assert.assertEquals(1, refreshes)
        verify(exactly = 1) { presentation.clear(target) }
    }

    @Test fun `failed completion rolls the same control back in both widget types`() = runTest {
        val presentation = mockk<CompletionPresentation>(relaxed = true)
        val targets = CompletionWidget.entries.mapIndexed { index, widget ->
            CompletionTarget(widget, 18 + index, 44 + index, "task-5", "2026-08-05T10:00:00Z")
        }

        targets.forEach { target ->
            org.junit.Assert.assertThrows(IllegalStateException::class.java) {
                kotlinx.coroutines.runBlocking {
                    TaskWidgetCompletionReceiver.executeWithRollback(target, presentation) {
                        throw IllegalStateException("transport failed")
                    }
                }
            }
        }

        targets.forEach { target -> verify(exactly = 1) { presentation.showUnchecked(target) } }
        verify(exactly = 0) { presentation.clear(any()) }
    }

    @Test fun `completes only the selected recurring occurrence`() = runTest {
        val occurrence = "2026-08-06T11:00:00Z"
        coEvery { repository.closeTask("task-6", occurrence) } returns task("task-6")

        TaskWidgetCompletionReceiver.performAction(
            repository,
            TaskWidgetCompletionReceiver.ACTION_COMPLETE,
            "task-6",
            occurrence,
        )

        coVerify(exactly = 1) { repository.closeTask("task-6", occurrence) }
        coVerify(exactly = 0) { repository.reopenTask(any(), any()) }
    }

    private fun task(id: String) = TaskDto(
        id = id, content = id, description = null, projectId = null, sectionId = null,
        parentId = null, order = 0, priority = null, labels = emptyList(), isCompleted = false,
        scheduledAt = null, estimateMinutes = null, isRecurring = false,
        createdAt = null, updatedAt = null, completedAt = null,
    )
}
