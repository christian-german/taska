package com.taska.android.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.taska.android.MainDispatcherRule
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.repository.LabelRepository
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import com.taska.android.data.repository.TimeEntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TaskDetailViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val taskRepo = mockk<TaskRepository>()
    private val projectRepo = mockk<ProjectRepository> { coEvery { getProjects() } returns emptyList() }
    private val labelRepo = mockk<LabelRepository> { coEvery { getLabels() } returns emptyList() }
    private val timeEntryRepo = mockk<TimeEntryRepository>()

    @Test
    fun `clear schedule preserves deadline and adopts server response`() = runTest {
        val original = task(dueAt = "2026-09-01T00:00:00Z")
        val updated = original.copy(scheduledAt = null, allDay = false)
        prepareLoad(original)
        val request = slot<TaskRequest>()
        coEvery { taskRepo.updateTask("task-1", capture(request)) } returns updated
        val viewModel = viewModel()

        viewModel.clearDue()

        assertNull(request.captured.scheduledAt)
        assertEquals(original.dueAt, request.captured.dueAt)
        assertEquals(original.description, request.captured.description)
        assertEquals(updated, viewModel.uiState.value.task)
        assertNull(viewModel.uiState.value.task?.scheduledAt)
        assertEquals(original.dueAt, viewModel.uiState.value.task?.dueAt)
    }

    @Test
    fun `failed clear keeps assigned schedule visible`() = runTest {
        val original = task(dueAt = null)
        prepareLoad(original)
        coEvery { taskRepo.updateTask(any(), any()) } throws IllegalStateException("failed")
        val viewModel = viewModel()

        viewModel.clearDue()

        assertEquals(original, viewModel.uiState.value.task)
        assertNotNull(viewModel.uiState.value.task?.scheduledAt)
    }

    @Test
    fun `recurring occurrence clear uses selected recurrence scope`() = runTest {
        val occurrence = "2026-08-24T09:00:00Z"
        val original = task(isRecurring = true, occurrenceScheduledAt = occurrence)
        prepareLoad(original)
        val request = slot<TaskRequest>()
        coEvery { taskRepo.updateTask("task-1", capture(request)) } returns original.copy(scheduledAt = null)
        val viewModel = viewModel(occurrence)

        viewModel.clearDue()
        coVerify(exactly = 0) { taskRepo.updateTask(any(), any()) }
        viewModel.confirmReschedule(RecurrenceScope.THIS_ONLY)

        assertNull(request.captured.scheduledAt)
        assertEquals("THIS_ONLY", request.captured.scope)
        assertEquals(occurrence, request.captured.occurrenceScheduledAt)
    }

    private fun prepareLoad(task: TaskDto) {
        coEvery { taskRepo.getTask("task-1") } returns task
        coEvery { taskRepo.getSubtasks("task-1") } returns emptyList()
    }

    private fun viewModel(occurrence: String? = null) = TaskDetailViewModel(
        SavedStateHandle(mapOf("task_id" to "task-1", "scheduled_at" to occurrence)),
        taskRepo, projectRepo, labelRepo, timeEntryRepo
    )

    private fun task(
        dueAt: String? = "2026-09-01T00:00:00Z",
        isRecurring: Boolean = false,
        occurrenceScheduledAt: String? = null,
    ) = TaskDto(
        id = "task-1", content = "Plan launch", description = "Keep this", projectId = "project-1",
        sectionId = null, parentId = null, order = 1, priority = 2, labels = listOf("work"),
        isCompleted = false, scheduledAt = "2026-08-24T09:00:00Z", dueAt = dueAt,
        allDay = false, isRecurring = isRecurring, recurrenceRule = if (isRecurring) "freq=weekly" else null,
        estimateMinutes = 30, createdAt = null, updatedAt = null, completedAt = null,
        occurrenceScheduledAt = occurrenceScheduledAt
    )
}
