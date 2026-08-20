package com.taska.android.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.model.TimeEntryRequest
import com.taska.android.data.repository.LabelRepository
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import com.taska.android.data.repository.TimeEntryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class PendingReschedule(val millis: Long?, val timeMinutes: Int?)

data class TaskDetailUiState(
    val task: TaskDto? = null,
    val project: ProjectDto? = null,
    val projects: List<ProjectDto> = emptyList(),
    val allLabels: List<LabelDto> = emptyList(),
    val subtasks: List<TaskDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val timerStarted: Boolean = false,
    val pendingReschedule: PendingReschedule? = null
)

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
    private val labelRepo: LabelRepository,
    private val timeEntryRepo: TimeEntryRepository,
) : ViewModel() {

    constructor(savedStateHandle: SavedStateHandle) : this(
        savedStateHandle,
        TaskRepository(),
        ProjectRepository(),
        LabelRepository(),
        TimeEntryRepository(),
    )

    private val taskId: String = checkNotNull(savedStateHandle["task_id"])
    private val instanceOccurrenceScheduledAt: String? = savedStateHandle["scheduled_at"]

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val taskDef = async { taskRepo.getTask(taskId) }
                val subtasksDef = async { taskRepo.getSubtasks(taskId) }
                val projectsDef = async { projectRepo.getProjects() }
                val labelsDef = async { labelRepo.getLabels() }

                val rawTask = taskDef.await()
                val task = if (instanceOccurrenceScheduledAt != null) rawTask.copy(scheduledAt = instanceOccurrenceScheduledAt) else rawTask
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

    private fun applyUpdate(transform: TaskRequest.() -> TaskRequest) {
        val task = _uiState.value.task ?: return
        val base = TaskRequest(
            content = task.content,
            type = task.type ?: "TODO",
            description = task.description,
            projectId = task.projectId,
            priority = task.priority,
            labels = task.labels,
            scheduledAt = task.scheduledAt,
            dueAt = task.dueAt,
            allDay = task.allDay,
            estimateMinutes = task.estimateMinutes,
            isRecurring = task.isRecurring,
            recurrenceRule = task.recurrenceRule
        )
        viewModelScope.launch {
            try {
                val updated = taskRepo.updateTask(taskId, base.transform())
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

    fun updateTaskType(type: String) = applyUpdate { copy(type = type) }

    fun requestRescheduleAllDay(millis: Long) {
        val task = _uiState.value.task ?: return
        if (task.isRecurring == true && instanceOccurrenceScheduledAt != null) {
            _uiState.update { it.copy(pendingReschedule = PendingReschedule(millis, null)) }
        } else {
            doReschedule(millis, null, scope = null)
        }
    }

    fun requestRescheduleWithTime(millis: Long, hour: Int, minute: Int) {
        val task = _uiState.value.task ?: return
        if (task.isRecurring == true && instanceOccurrenceScheduledAt != null) {
            _uiState.update { it.copy(pendingReschedule = PendingReschedule(millis, hour * 60 + minute)) }
        } else {
            doReschedule(millis, hour * 60 + minute, scope = null)
        }
    }

    fun confirmReschedule(scope: RecurrenceScope?) {
        val pending = _uiState.value.pendingReschedule ?: return
        _uiState.update { it.copy(pendingReschedule = null) }
        if (pending.millis == null) {
            doClearSchedule(scope)
        } else {
            doReschedule(pending.millis, pending.timeMinutes, scope)
        }
    }

    fun dismissRescheduleScope() {
        _uiState.update { it.copy(pendingReschedule = null) }
    }

    private fun doReschedule(millis: Long, timeMinutes: Int?, scope: RecurrenceScope?) {
        val task = _uiState.value.task ?: return
        val request = TaskRequest(
            content = task.content,
            type = task.type,
            description = task.description,
            projectId = task.projectId,
            priority = task.priority,
            labels = task.labels,
            scheduledAt = millisToApiDateTime(millis, timeMinutes),
            dueAt = task.dueAt,
            allDay = timeMinutes == null,
            estimateMinutes = task.estimateMinutes,
            isRecurring = task.isRecurring,
            recurrenceRule = task.recurrenceRule,
            scope = scope?.name,
            occurrenceScheduledAt = instanceOccurrenceScheduledAt
        )
        viewModelScope.launch {
            try {
                val updated = taskRepo.updateTask(taskId, request)
                val newProject = _uiState.value.projects.firstOrNull { it.id == updated.projectId }
                _uiState.update { it.copy(task = updated, project = newProject ?: if (updated.projectId == null) null else it.project) }
            } catch (_: Exception) {}
        }
    }

    fun clearDue() {
        val task = _uiState.value.task ?: return
        if (task.isRecurring == true && instanceOccurrenceScheduledAt != null) {
            _uiState.update { it.copy(pendingReschedule = PendingReschedule(null, null)) }
        } else {
            doClearSchedule(scope = null)
        }
    }

    private fun doClearSchedule(scope: RecurrenceScope?) {
        val task = _uiState.value.task ?: return
        val request = TaskRequest(
            content = task.content,
            type = task.type,
            description = task.description,
            projectId = task.projectId,
            priority = task.priority,
            labels = task.labels,
            scheduledAt = null,
            dueAt = task.dueAt,
            allDay = false,
            estimateMinutes = task.estimateMinutes,
            isRecurring = task.isRecurring,
            recurrenceRule = task.recurrenceRule,
            scope = scope?.name,
            occurrenceScheduledAt = instanceOccurrenceScheduledAt
        )
        viewModelScope.launch {
            try {
                val updated = taskRepo.updateTask(taskId, request)
                _uiState.update { it.copy(task = updated) }
            } catch (_: Exception) {}
        }
    }

    fun updateDueAt(millis: Long) = applyUpdate { copy(dueAt = millisToApiDateTime(millis, null)) }

    fun updateProject(projectId: String?) = applyUpdate { copy(projectId = projectId) }

    fun updateEstimate(minutes: Int?) = applyUpdate { copy(estimateMinutes = minutes) }

    fun updateLabels(labels: List<String>) = applyUpdate { copy(labels = labels.ifEmpty { null }) }

    fun updatePriority(priority: Int) = applyUpdate { copy(priority = priority) }

    fun updateRecurrence(rule: String?) = applyUpdate {
        copy(isRecurring = if (rule != null) true else false, recurrenceRule = rule)
    }

    fun deleteTask(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                taskRepo.deleteTask(taskId)
                onDeleted()
            } catch (_: Exception) {}
        }
    }

    fun toggleSubtask(subtask: TaskDto) {
        viewModelScope.launch {
            try {
                if (subtask.isCompleted == true) {
                    taskRepo.reopenTask(subtask.id)
                } else {
                    taskRepo.closeTask(subtask.id)
                }
                val updated = taskRepo.getSubtasks(taskId)
                _uiState.update { it.copy(subtasks = updated) }
            } catch (_: Exception) {}
        }
    }

    fun addSubtask(content: String, onSuccess: () -> Unit = {}) {
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                taskRepo.createTask(
                    TaskRequest(
                        content = content,
                        projectId = _uiState.value.task?.projectId,
                        parentId = taskId
                    )
                )
                val updated = taskRepo.getSubtasks(taskId)
                _uiState.update { it.copy(subtasks = updated) }
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            try {
                timeEntryRepo.createTimeEntry(
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

    fun scheduledAtToMillis(): Long {
        val scheduledAt = _uiState.value.task?.scheduledAt ?: return todayMillis()
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .parse(scheduledAt.take(10))?.time ?: todayMillis()
        } catch (_: Exception) { todayMillis() }
    }

    fun dueAtToMillis(): Long {
        val dueAt = _uiState.value.task?.dueAt ?: return todayMillis()
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .parse(dueAt.take(10))?.time ?: todayMillis()
        } catch (_: Exception) { todayMillis() }
    }

    private fun todayMillis(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun currentIsoDateTime(): String = java.time.Instant.now().toString()
}
