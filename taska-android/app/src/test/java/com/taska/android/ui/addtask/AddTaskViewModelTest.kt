package com.taska.android.ui.addtask

import com.taska.android.MainDispatcherRule
import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AddTaskViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val taskRepository = mockk<TaskRepository>()
    private val projectRepository = mockk<ProjectRepository> {
        coEvery { getProjects() } returns emptyList()
    }

    @Test
    fun `successful creation invokes success feedback callback once`() = runTest {
        coEvery { taskRepository.createTask(any()) } returns mockk<TaskDto>()
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val viewModel = AddTaskViewModel(taskRepository, projectRepository)
        viewModel.updateContent("New task")

        viewModel.createTask(onSuccess)

        verify(exactly = 1) { onSuccess() }
    }

    @Test
    fun `failed creation does not invoke success feedback callback`() = runTest {
        coEvery { taskRepository.createTask(any()) } throws IllegalStateException("failed")
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val viewModel = AddTaskViewModel(taskRepository, projectRepository)
        viewModel.updateContent("New task")

        viewModel.createTask(onSuccess)

        verify(exactly = 0) { onSuccess() }
    }
}
