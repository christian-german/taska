package com.taska.android.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.taska.android.MainDispatcherRule
import com.taska.android.data.model.OccurrenceUpdateRequest
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskUpdateRequest
import com.taska.android.data.model.copy
import com.taska.android.data.repository.LabelRepository
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {
  @get:Rule val dispatcherRule = MainDispatcherRule()

  private val taskRepo = mockk<TaskRepository>()
  private val projectRepo =
    mockk<ProjectRepository> { coEvery { getProjects() } returns emptyList() }
  private val labelRepo = mockk<LabelRepository> { coEvery { getLabels() } returns emptyList() }

  @Test
  fun `clear schedule preserves deadline and adopts server response`() = runTest {
    val original = task(dueAt = "2026-09-01T00:00:00Z")
    val updated = original.copy(scheduledAt = null, allDay = false)
    prepareLoad(original)
    val request = slot<TaskUpdateRequest>()
    coEvery { taskRepo.updateTask("task-1", capture(request)) } returns updated
    val viewModel = viewModel()

    viewModel.clearDue()

    assertNull(request.captured.scheduledAt)
    assertEquals(original.dueAt, request.captured.dueAt)
    assertEquals(original.description, request.captured.description)
    assertEquals(original.labels, request.captured.labels)
    assertEquals(original.priority, request.captured.priority)
    assertEquals(original.mentionContext, request.captured.mentionContext)
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
  fun `recurring occurrence reset directly targets its stable identity`() = runTest {
    val occurrence = "2026-08-24T09:00:00Z"
    val original = task(isRecurring = true, occurrenceScheduledAt = occurrence)
    prepareLoad(original)
    val request = slot<OccurrenceUpdateRequest>()
    coEvery { taskRepo.updateOccurrence("task-1", occurrence, capture(request)) } returns
      original.copy(scheduledAt = null)
    val viewModel = viewModel(occurrence)

    viewModel.clearDue()
    coVerify(exactly = 0) { taskRepo.updateTask(any(), any()) }

    assertNull(request.captured.scheduledAt)
    assertEquals(occurrence, original.occurrenceScheduledAt)
  }

  @Test
  fun `series generator controls cannot mutate the series`() = runTest {
    val original = task(isRecurring = true)
    prepareLoad(original)
    val viewModel = viewModel()
    viewModel.clearDue()
    viewModel.updateRecurrence("FREQ=WEEKLY")
    advanceUntilIdle()
    assertEquals(original, viewModel.uiState.value.task)
    coVerify(exactly = 0) { taskRepo.updateTask(any(), any()) }
  }

  @Test
  fun `series common fields remain editable without changing its generator`() = runTest {
    val original = task(isRecurring = true)
    prepareLoad(original)
    val request = slot<TaskUpdateRequest>()
    coEvery { taskRepo.updateTask("task-1", capture(request)) } returns original
    val viewModel = viewModel()

    viewModel.updateContent("New series title")
    advanceUntilIdle()

    assertEquals("New series title", request.captured.content)
    assertEquals(original.scheduledAt, request.captured.scheduledAt)
    assertEquals(original.recurrenceRule, request.captured.recurrenceRule)
    assertEquals(original.allDay, request.captured.allDay)
  }

  @Test
  fun `converting a scheduled task to a series removes its absolute deadline`() = runTest {
    prepareLoad(task())
    val request = slot<TaskUpdateRequest>()
    coEvery { taskRepo.updateTask("task-1", capture(request)) } returns task(isRecurring = true)
    val viewModel = viewModel()

    viewModel.updateRecurrence("FREQ=WEEKLY")
    advanceUntilIdle()

    assertEquals(true, request.captured.isRecurring)
    assertNull(request.captured.dueAt)
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
    val original =
      task(isRecurring = true, occurrenceScheduledAt = occurrence).copy(isCompleted = true)
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

  @Test
  fun `occurrence detail loads the occurrence representation`() = runTest {
    val occurrence = "2026-08-24T09:00:00Z"
    val detached = task(isRecurring = true, occurrenceScheduledAt = occurrence, isDetached = true)
    prepareLoad(detached)

    val viewModel = viewModel(occurrence)
    advanceUntilIdle()

    assertEquals(detached, viewModel.uiState.value.task)
    coVerify(exactly = 1) { taskRepo.getOccurrence("task-1", occurrence) }
    coVerify(exactly = 0) { taskRepo.getTask(any()) }
  }

  @Test
  fun `detached occurrence reschedule updates this occurrence without asking for a scope`() =
    runTest {
      val occurrence = "2026-08-24T09:00:00Z"
      val detached = task(isRecurring = true, occurrenceScheduledAt = occurrence, isDetached = true)
      prepareLoad(detached)
      val request = slot<OccurrenceUpdateRequest>()
      coEvery { taskRepo.updateOccurrence("task-1", occurrence, capture(request)) } returns detached
      val viewModel = viewModel(occurrence)

      viewModel.requestRescheduleAllDay(1_788_134_400_000)
      advanceUntilIdle()

      assertNotNull(request.captured.scheduledAt)
      coVerify(exactly = 0) { taskRepo.updateTask(any(), any()) }
    }

  @Test
  fun `detached occurrence historical fields cannot be edited`() = runTest {
    val occurrence = "2026-08-24T09:00:00Z"
    val detached = task(isRecurring = true, occurrenceScheduledAt = occurrence, isDetached = true)
    prepareLoad(detached)
    coEvery { taskRepo.updateOccurrence("task-1", occurrence, any()) } returns
      detached.copy(content = "Updated")
    val viewModel = viewModel(occurrence)

    viewModel.updateContent("Updated")
    viewModel.updateProject("another-project")
    advanceUntilIdle()

    assertEquals(detached.content, viewModel.uiState.value.task?.content)
    coVerify(exactly = 0) { taskRepo.updateOccurrence("task-1", occurrence, any()) }
    coVerify(exactly = 0) { taskRepo.updateTask(any(), any()) }
  }

  @Test
  fun `detached occurrence deletion is always this occurrence only`() = runTest {
    val occurrence = "2026-08-24T09:00:00Z"
    val detached = task(isRecurring = true, occurrenceScheduledAt = occurrence, isDetached = true)
    prepareLoad(detached)
    coEvery { taskRepo.deleteTask("task-1", RecurrenceScope.THIS_ONLY, occurrence) } returns Unit
    val viewModel = viewModel(occurrence)
    var deleted = false

    viewModel.deleteTask(onDeleted = { deleted = true })
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoading)
    assertEquals(true, deleted)
    coVerify(exactly = 1) { taskRepo.deleteTask("task-1", RecurrenceScope.THIS_ONLY, occurrence) }
    coVerify(exactly = 0) { taskRepo.deleteTask("task-1", null, null) }
  }

  @Test
  fun `locked recurrence edit never sends a request`() = runTest {
    val original = task(isRecurring = true)
    val serverDetail =
      "A recurring series with occurrence state must be changed from an explicit occurrence"
    prepareLoad(original)
    coEvery { taskRepo.updateTask("task-1", any()) } throws IllegalStateException(serverDetail)
    val viewModel = viewModel()
    advanceUntilIdle()

    viewModel.updateRecurrence("FREQ=DAILY")
    advanceUntilIdle()

    assertEquals(original, viewModel.uiState.value.task)
    assertNull(viewModel.uiState.value.mutationError)
    assertNull(viewModel.uiState.value.error)
  }

  @Test
  fun `mutation error can be consumed without changing the loaded task`() = runTest {
    val original = task()
    prepareLoad(original)
    coEvery { taskRepo.updateTask("task-1", any()) } throws IllegalStateException("Rejected")
    val viewModel = viewModel()
    advanceUntilIdle()
    viewModel.updateDescription("Changed")
    advanceUntilIdle()

    viewModel.consumeMutationError()

    assertNull(viewModel.uiState.value.mutationError)
    assertEquals(original, viewModel.uiState.value.task)
  }

  private fun prepareLoad(task: TaskDto) {
    coEvery { taskRepo.getTask("task-1") } returns task
    coEvery { taskRepo.getOccurrence("task-1", any()) } returns task
    coEvery { taskRepo.getSubtasks("task-1") } returns emptyList()
  }

  private fun viewModel(occurrence: String? = null) =
    TaskDetailViewModel(
      SavedStateHandle(mapOf("task_id" to "task-1", "scheduled_at" to occurrence)),
      taskRepo,
      projectRepo,
      labelRepo,
    )

  private fun task(
    dueAt: String? = "2026-09-01T00:00:00Z",
    isRecurring: Boolean = false,
    occurrenceScheduledAt: String? = null,
    isDetached: Boolean = false,
  ) =
    TaskDto(
      id = "task-1",
      content = "Plan launch",
      description = "Keep this",
      projectId = "project-1",
      parentId = null,
      order = 1,
      priority = 2,
      labels = listOf("work"),
      isCompleted = false,
      scheduledAt = "2026-08-24T09:00:00Z",
      dueAt = dueAt,
      allDay = false,
      isRecurring = isRecurring,
      recurrenceRule = if (isRecurring) "freq=weekly" else null,
      estimateMinutes = 30,
      mentionContext = "context",
      createdAt = null,
      updatedAt = null,
      completedAt = null,
      occurrenceScheduledAt = occurrenceScheduledAt,
      isDetached = isDetached,
    )
}
