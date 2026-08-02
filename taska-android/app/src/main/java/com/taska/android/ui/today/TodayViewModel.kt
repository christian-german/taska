package com.taska.android.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.RecurrenceScope
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

data class TodayUiState(
    val overdueTasks: List<TaskDto> = emptyList(),
    val todayTasks: List<TaskDto> = emptyList(),
    val tomorrowTasks: List<TaskDto> = emptyList(),
    val projects: Map<String, ProjectDto> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingDeleteTask: TaskDto? = null
)

class TodayViewModel(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
) : ViewModel() {

    constructor() : this(TaskRepository(), ProjectRepository())

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayStr = fmt.format(Calendar.getInstance().time)
                val tomorrowStr = fmt.format(
                    Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 1) }.time
                )

                val projectsDeferred = async { projectRepo.getProjects() }
                val todayDeferred = async { taskRepo.getTasks(date = todayStr) }
                val tomorrowDeferred = async { taskRepo.getTasks(date = tomorrowStr) }

                val projectsMap = projectsDeferred.await().associateBy { it.id }
                val todayAll = todayDeferred.await()
                val tomorrowAll = tomorrowDeferred.await()

                val overdue = taskRepo.getTasks(showCompleted = false)
                    .filter { it.isCompleted != true && it.isRecurring != true && it.scheduledAt != null && scheduledAtLocalDate(it.scheduledAt) < todayStr }
                    .sortedBy { it.scheduledAt }

                val today = todayAll
                    .sortedWith(compareBy({ it.isCompleted == true }, { it.scheduledAt }))

                val tomorrow = tomorrowAll
                    .filter { it.isCompleted != true }
                    .sortedBy { it.scheduledAt }

                _uiState.update {
                    it.copy(
                        overdueTasks = overdue,
                        todayTasks = today,
                        tomorrowTasks = tomorrow,
                        projects = projectsMap,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun closeTask(task: TaskDto) {
        viewModelScope.launch {
            try {
                val closed = taskRepo.closeTask(task.id, task.occurrenceScheduledAt)
                _uiState.update { state ->
                    state.copy(
                        overdueTasks = state.overdueTasks.filter { it.id != task.id || it.occurrenceScheduledAt != task.occurrenceScheduledAt },
                        todayTasks = state.todayTasks.map {
                            if (it.id == task.id && it.occurrenceScheduledAt == task.occurrenceScheduledAt) closed else it
                        },
                        tomorrowTasks = state.tomorrowTasks.filter { it.id != task.id || it.occurrenceScheduledAt != task.occurrenceScheduledAt }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun reopenTask(task: TaskDto) {
        viewModelScope.launch {
            try {
                val reopened = taskRepo.reopenTask(task.id, task.occurrenceScheduledAt)
                _uiState.update { state ->
                    state.copy(
                        todayTasks = state.todayTasks.map {
                            if (it.id == task.id && it.occurrenceScheduledAt == task.occurrenceScheduledAt) reopened else it
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun requestDeleteTask(task: TaskDto) {
        if (task.isRecurring == true && task.occurrenceScheduledAt != null) {
            _uiState.update { it.copy(pendingDeleteTask = task) }
        } else {
            confirmDeleteTask(task, scope = null)
        }
    }

    fun confirmDeleteTask(task: TaskDto, scope: RecurrenceScope?) {
        _uiState.update { it.copy(pendingDeleteTask = null) }
        viewModelScope.launch {
            try {
                taskRepo.deleteTask(task.id, scope, task.occurrenceScheduledAt)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun dismissDeleteScope() {
        _uiState.update { it.copy(pendingDeleteTask = null) }
    }
}

private fun scheduledAtLocalDate(scheduledAt: String): String = try {
    val instant = java.time.Instant.parse(scheduledAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    "%04d-%02d-%02d".format(zoned.year, zoned.monthValue, zoned.dayOfMonth)
} catch (_: Exception) { scheduledAt.take(10) }
