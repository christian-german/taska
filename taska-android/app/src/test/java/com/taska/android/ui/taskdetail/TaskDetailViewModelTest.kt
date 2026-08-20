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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
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

    @Test
    fun `active task completion adopts the server response`() = runTest {
        val original = task()
        val completed = original.copy(isCompleted = true, completedAt = "2026-08-24T10:00:00Z")
        prepareLoad(original)
        coEvery { taskRepo.closeTask("task-1", null) } returns completed
        val viewModel = viewModel()

        viewModel.toggleCompletion()
        advanceUntilIdle()

        assertEquals(completed, viewModel.uiState.value.task)
        assertEquals(false, viewModel.uiState.value.isCompletionPending)
        coVerify(exactly = 1) { taskRepo.closeTask("task-1", null) }
        coVerify(exactly = 0) { taskRepo.reopenTask(any(), any()) }
    }

    @Test
    fun `completed recurring occurrence is reopened with its occurrence identity`() = runTest {
        val occurrence = "2026-08-24T09:00:00Z"
        val original = task(isRecurring = true, occurrenceScheduledAt = occurrence).copy(isCompleted = true)
        val reopened = original.copy(isCompleted = false, completedAt = null)
        prepareLoad(original)
        coEvery { taskRepo.reopenTask("task-1", occurrence) } returns reopened
        val viewModel = viewModel(occurrence)

        viewModel.toggleCompletion()
        advanceUntilIdle()

        assertEquals(reopened, viewModel.uiState.value.task)
        coVerify(exactly = 1) { taskRepo.reopenTask("task-1", occurrence) }
        coVerify(exactly = 0) { taskRepo.closeTask(any(), any()) }
    }

    @Test
    fun `failed completion retains confirmed task`() = runTest {
        val original = task()
        prepareLoad(original)
        coEvery { taskRepo.closeTask("task-1", null) } throws IllegalStateException("rejected")
        val viewModel = viewModel()

        viewModel.toggleCompletion()
        advanceUntilIdle()

        assertEquals(original, viewModel.uiState.value.task)
        assertEquals(false, viewModel.uiState.value.isCompletionPending)
    }

    @Test
    fun `second completion tap is ignored while request is pending`() = runTest {
        val original = task()
        val response = CompletableDeferred<TaskDto>()
        prepareLoad(original)
        coEvery { taskRepo.closeTask("task-1", null) } coAnswers { response.await() }
        val viewModel = viewModel()

        viewModel.toggleCompletion()
        viewModel.toggleCompletion()
        coVerify(exactly = 1) { taskRepo.closeTask("task-1", null) }

        response.complete(original.copy(isCompleted = true))
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.task?.isCompleted)
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
