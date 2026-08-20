package com.taska.android.ui.search

import com.taska.android.data.model.TaskDto
import com.taska.android.MainDispatcherRule
import com.taska.android.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test
    fun `filter matches content case insensitively and preserves order`() {
        val tasks = listOf(
            task("1", "Prepare Quarterly Report", completed = true),
            task("2", "quarterly planning"),
            task("3", "Other task", description = "quarterly"),
        )
        val results = filterTasks(tasks, "QUARTERLY")
        assertEquals(listOf("1", "2"), results.map { it.id })
        assertTrue(results.first().isCompleted == true)
    }

    @Test fun `blank query has no results`() {
        assertTrue(filterTasks(listOf(task("1", "anything")), "  ").isEmpty())
    }

    @Test fun `load includes completed tasks and publishes success`() = runTest {
        val repository = mockk<TaskRepository>()
        coEvery { repository.getTasks(showCompleted = true) } returns listOf(task("1", "done", true))
        val viewModel = SearchViewModel(repository)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.tasks.single().isCompleted == true)
        coVerify(exactly = 1) { repository.getTasks(showCompleted = true) }
    }

    @Test fun `load failure remains distinct from no matches`() = runTest {
        val repository = mockk<TaskRepository>()
        coEvery { repository.getTasks(showCompleted = true) } throws IllegalStateException("offline")
        val viewModel = SearchViewModel(repository)
        advanceUntilIdle()
        assertEquals("offline", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun task(id: String, content: String, completed: Boolean = false, description: String? = null) =
        TaskDto(
            id = id, content = content, description = description, projectId = null,
            sectionId = null, parentId = null, order = null, priority = null, labels = null,
            isCompleted = completed, scheduledAt = null, isRecurring = false,
            estimateMinutes = null, createdAt = null, updatedAt = null, completedAt = null,
        )
}
