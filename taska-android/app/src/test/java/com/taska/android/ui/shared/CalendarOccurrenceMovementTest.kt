package com.taska.android.ui.shared

import com.taska.android.MainDispatcherRule
import com.taska.android.data.model.OccurrenceUpdateRequest
import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import com.taska.android.ui.day.DayViewModel
import com.taska.android.ui.week.WeekViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarOccurrenceMovementTest {
  @get:Rule val dispatcherRule = MainDispatcherRule()

  private val originalSchedule = "2026-08-24T09:00:00Z"
  private val movedSchedule = "2026-08-25T10:00:00Z"
  private val occurrence =
    TaskDto(
      id = "series-1",
      content = "Historical title",
      description = null,
      projectId = null,
      parentId = null,
      order = 0,
      labels = emptyList(),
      isCompleted = false,
      createdAt = null,
      updatedAt = null,
      completedAt = null,
      priority = 2,
      scheduledAt = originalSchedule,
      dueAt = "2026-09-01T00:00:00Z",
      isRecurring = true,
      recurrenceRule = "FREQ=WEEKLY",
      occurrenceScheduledAt = originalSchedule,
      estimateMinutes = 30,
    )
  private val taskRepository =
    mockk<TaskRepository> {
      coEvery { getTasks(any(), any(), any(), any(), any()) } returns emptyList()
      coEvery {
        updateOccurrence("series-1", originalSchedule, OccurrenceUpdateRequest(movedSchedule))
      } returns occurrence
    }
  private val projectRepository =
    mockk<ProjectRepository> { coEvery { getProjects() } returns emptyList() }

  @Test
  fun `day movement sends only schedule using original identity`() = runTest {
    val viewModel = DayViewModel(taskRepository, projectRepository)
    advanceUntilIdle()

    viewModel.requestRescheduleTask(occurrence, movedSchedule, 90)
    advanceUntilIdle()

    coVerify(exactly = 1) {
      taskRepository.updateOccurrence(
        "series-1",
        originalSchedule,
        OccurrenceUpdateRequest(movedSchedule),
      )
    }
    coVerify(exactly = 0) { taskRepository.updateTask(any(), any()) }
  }

  @Test
  fun `week movement sends only schedule using original identity`() = runTest {
    val viewModel = WeekViewModel(taskRepository, projectRepository)
    advanceUntilIdle()

    viewModel.requestRescheduleTask(occurrence, movedSchedule, 90)
    advanceUntilIdle()

    coVerify(exactly = 1) {
      taskRepository.updateOccurrence(
        "series-1",
        originalSchedule,
        OccurrenceUpdateRequest(movedSchedule),
      )
    }
    coVerify(exactly = 0) { taskRepository.updateTask(any(), any()) }
  }
}
