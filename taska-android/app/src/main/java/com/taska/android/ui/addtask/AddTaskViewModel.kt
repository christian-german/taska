package com.taska.android.ui.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddTaskUiState(
    val content: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeMinutes: Int? = null,
    val selectedProject: ProjectDto? = null,
    val estimateMinutes: Int? = null,
    val priority: Int = 4,
    val recurrenceRule: String? = null,
    val projects: List<ProjectDto> = emptyList(),
    val isSubmitting: Boolean = false
)

class AddTaskViewModel(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
) : ViewModel() {

    constructor() : this(TaskRepository(), ProjectRepository())

    private val _state = MutableStateFlow(AddTaskUiState())
    val state: StateFlow<AddTaskUiState> = _state.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                val projects = projectRepo.getProjects()
                _state.update { it.copy(projects = projects.filter { p -> p.isInboxProject != true }) }
            } catch (_: Exception) {}
        }
    }

    fun updateContent(content: String) = _state.update { it.copy(content = content) }
    fun updateDueDate(millis: Long?) = _state.update { it.copy(dueDateMillis = millis, dueTimeMinutes = null) }
    fun updateTime(totalMinutes: Int) = _state.update { it.copy(dueTimeMinutes = totalMinutes) }
    fun clearTime() = _state.update { it.copy(dueTimeMinutes = null) }
    fun updateProject(project: ProjectDto?) = _state.update { it.copy(selectedProject = project) }
    fun updateEstimate(minutes: Int?) = _state.update { it.copy(estimateMinutes = minutes) }
    fun updatePriority(priority: Int) = _state.update { it.copy(priority = priority) }
    fun updateRecurrence(rule: String?) = _state.update { it.copy(recurrenceRule = rule) }

    fun reset() = _state.update { AddTaskUiState(projects = it.projects) }

    fun createTask(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.content.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            try {
                val dueAt = current.dueDateMillis?.let { millisToApiDateTime(it, current.dueTimeMinutes) }
                val allDay = if (current.dueDateMillis != null) current.dueTimeMinutes == null else null
                val request = TaskRequest(
                    content = current.content.trim(),
                    projectId = current.selectedProject?.id,
                    priority = current.priority.takeIf { it < 4 },
                    dueAt = dueAt,
                    allDay = allDay,
                    estimateMinutes = current.estimateMinutes,
                    isRecurring = if (current.recurrenceRule != null) true else null,
                    recurrenceRule = current.recurrenceRule
                )
                taskRepo.createTask(request)
                _state.update { it.copy(isSubmitting = false) }
                onSuccess()
            } catch (_: Exception) {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun millisToApiDateTime(millis: Long, timeMinutes: Int?): String {
        val local = Calendar.getInstance().also { it.timeInMillis = millis }
        val y = local.get(Calendar.YEAR)
        val mo = (local.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = local.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return if (timeMinutes != null) {
            val withTime = Calendar.getInstance().apply {
                set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH),
                    timeMinutes / 60, timeMinutes % 60, 0)
                set(Calendar.MILLISECOND, 0)
            }
            java.time.Instant.ofEpochMilli(withTime.timeInMillis).toString()
        } else {
            "${y}-${mo}-${d}T00:00:00Z"
        }
    }
}
