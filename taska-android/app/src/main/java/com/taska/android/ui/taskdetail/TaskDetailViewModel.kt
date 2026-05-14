package com.taska.android.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.model.TimeEntryRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TaskDetailUiState(
    val task: TaskDto? = null,
    val project: ProjectDto? = null,
    val projects: List<ProjectDto> = emptyList(),
    val allLabels: List<LabelDto> = emptyList(),
    val subtasks: List<TaskDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val timerStarted: Boolean = false
)

class TaskDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle["task_id"])

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val taskDef = async { RetrofitClient.api.getTask(taskId) }
                val subtasksDef = async { RetrofitClient.api.getSubtasks(taskId) }
                val projectsDef = async { RetrofitClient.api.getProjects() }
                val labelsDef = async { RetrofitClient.api.getLabels() }

                val task = taskDef.await()
                val subtasks = subtasksDef.await()
                val projects = projectsDef.await()
                val labels = labelsDef.await()
                val project = projects.firstOrNull { it.id == task.projectId }

                _uiState.update {
                    it.copy(
                        task = task,
                        project = project,
                        projects = projects.filter { p -> p.isInboxProject != true },
                        allLabels = labels,
                        subtasks = subtasks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Central update helper — preserves all current task fields
    private fun applyUpdate(transform: TaskRequest.() -> TaskRequest) {
        val task = _uiState.value.task ?: return
        val base = TaskRequest(
            content = task.content,
            description = task.description,
            projectId = task.projectId,
            priority = task.priority,
            labels = task.labels,
            dueDate = task.dueDate,
            estimateMinutes = task.estimateMinutes
        )
        viewModelScope.launch {
            try {
                val updated = RetrofitClient.api.updateTask(taskId, base.transform())
                val newProject = _uiState.value.projects.firstOrNull { it.id == updated.projectId }
                _uiState.update { it.copy(task = updated, project = newProject ?: if (updated.projectId == null) null else it.project) }
            } catch (_: Exception) {}
        }
    }

    fun updateContent(content: String) {
        if (content.isBlank()) return
        applyUpdate { copy(content = content) }
    }

    fun updateDescription(desc: String) = applyUpdate { copy(description = desc.ifEmpty { null }) }

    fun reschedule(dateStr: String) = applyUpdate { copy(dueDate = dateStr) }

    fun updateProject(projectId: String?) = applyUpdate { copy(projectId = projectId) }

    fun updateEstimate(minutes: Int?) = applyUpdate { copy(estimateMinutes = minutes) }

    fun updateLabels(labels: List<String>) = applyUpdate { copy(labels = labels.ifEmpty { null }) }

    fun updatePriority(priority: Int) = applyUpdate { copy(priority = priority) }

    fun deleteTask(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.deleteTask(taskId)
                onDeleted()
            } catch (_: Exception) {}
        }
    }

    fun toggleSubtask(subtask: TaskDto) {
        viewModelScope.launch {
            try {
                if (subtask.isCompleted == true) {
                    RetrofitClient.api.reopenTask(subtask.id)
                } else {
                    RetrofitClient.api.closeTask(subtask.id)
                }
                val updated = RetrofitClient.api.getSubtasks(taskId)
                _uiState.update { it.copy(subtasks = updated) }
            } catch (_: Exception) {}
        }
    }

    fun addSubtask(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                RetrofitClient.api.createTask(
                    TaskRequest(
                        content = content,
                        projectId = _uiState.value.task?.projectId,
                        parentId = taskId
                    )
                )
                val updated = RetrofitClient.api.getSubtasks(taskId)
                _uiState.update { it.copy(subtasks = updated) }
            } catch (_: Exception) {}
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            try {
                RetrofitClient.api.createTimeEntry(
                    TimeEntryRequest(
                        startAt = currentIsoDateTime(),
                        projectId = task.projectId,
                        description = task.content
                    )
                )
                _uiState.update { it.copy(timerStarted = true) }
            } catch (_: Exception) {}
        }
    }

    fun millisToApiDate(millis: Long): String {
        val cal = Calendar.getInstance().also { it.timeInMillis = millis }
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    private fun currentIsoDateTime(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
}
