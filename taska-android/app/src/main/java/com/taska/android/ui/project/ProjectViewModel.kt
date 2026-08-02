package com.taska.android.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ProjectUiState(
    val project: ProjectDto? = null,
    val parentProject: ProjectDto? = null,
    val tasks: List<TaskDto> = emptyList(),
    val overdueCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProjectViewModel(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
) : ViewModel() {

    constructor() : this(TaskRepository(), ProjectRepository())

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    private var projectId: String? = null

    fun load(id: String) {
        projectId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val projectsDeferred = async { projectRepo.getProjects() }
                val tasksDeferred = async { taskRepo.getTasks(projectId = id) }

                val allProjects = projectsDeferred.await()
                val project = allProjects.firstOrNull { it.id == id }
                val parent = project?.parentId?.let { pid -> allProjects.firstOrNull { it.id == pid } }
                val tasks = tasksDeferred.await()

                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(Calendar.getInstance().time)
                val overdueCount = tasks.count {
                    it.isCompleted != true && it.scheduledAt != null && it.scheduledAt.substring(0, 10) < todayStr
                }

                val sorted = tasks.sortedWith(
                    compareByDescending<TaskDto> { it.scheduledAt != null && it.scheduledAt.substring(0, 10) < todayStr }
                        .thenBy { it.scheduledAt }
                )

                _uiState.update {
                    it.copy(
                        project = project,
                        parentProject = parent,
                        tasks = sorted,
                        overdueCount = overdueCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun reload() {
        projectId?.let { load(it) }
    }

    fun closeTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepo.closeTask(taskId)
                _uiState.update { state ->
                    state.copy(tasks = state.tasks.filter { it.id != taskId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
