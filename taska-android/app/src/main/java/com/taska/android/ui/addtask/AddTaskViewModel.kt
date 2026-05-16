package com.taska.android.ui.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class AddTaskUiState(
    val content: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeMinutes: Int? = null,
    val selectedProject: ProjectDto? = null,
    val estimateMinutes: Int? = null,
    val priority: Int = 4,
    val projects: List<ProjectDto> = emptyList(),
    val isSubmitting: Boolean = false
)

class AddTaskViewModel : ViewModel() {

    private val _state = MutableStateFlow(AddTaskUiState())
    val state: StateFlow<AddTaskUiState> = _state.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                val projects = RetrofitClient.api.getProjects()
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
                    estimateMinutes = current.estimateMinutes
                )
                RetrofitClient.api.createTask(request)
                _state.update { it.copy(isSubmitting = false) }
                onSuccess()
            } catch (_: Exception) {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun millisToApiDateTime(millis: Long, timeMinutes: Int?): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).also { it.timeInMillis = millis }
        val y = utc.get(Calendar.YEAR)
        val m = (utc.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = utc.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return if (timeMinutes != null) {
            val h = (timeMinutes / 60).toString().padStart(2, '0')
            val min = (timeMinutes % 60).toString().padStart(2, '0')
            "$y-$m-${d}T$h:$min:00"
        } else {
            "$y-$m-${d}T00:00:00"
        }
    }
}
