package com.taska.android.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
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
    val error: String? = null
)

class TodayViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val projectsDeferred = async { RetrofitClient.api.getProjects() }
                val tasksDeferred = async { RetrofitClient.api.getTasks(showCompleted = true) }

                val projectsMap = projectsDeferred.await().associateBy { it.id }
                val tasks = tasksDeferred.await()

                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayStr = fmt.format(Calendar.getInstance().time)
                val tomorrowStr = fmt.format(
                    Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, 1) }.time
                )

                val overdue = tasks
                    .filter { it.isCompleted != true && it.dueAt != null && dueAtLocalDate(it.dueAt) < todayStr }
                    .sortedBy { it.dueAt }

                val today = tasks
                    .filter { it.dueAt != null && dueAtLocalDate(it.dueAt) == todayStr }
                    .sortedWith(compareBy({ it.isCompleted == true }, { it.dueAt }))

                val tomorrow = tasks
                    .filter { it.isCompleted != true && it.dueAt != null && dueAtLocalDate(it.dueAt) == tomorrowStr }
                    .sortedBy { it.dueAt }

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

    fun closeTask(taskId: String) {
        viewModelScope.launch {
            try {
                val closed = RetrofitClient.api.closeTask(taskId)
                _uiState.update { state ->
                    state.copy(
                        overdueTasks = state.overdueTasks.filter { it.id != taskId },
                        todayTasks = state.todayTasks.map { if (it.id == taskId) closed else it },
                        tomorrowTasks = state.tomorrowTasks.filter { it.id != taskId }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun reopenTask(taskId: String) {
        viewModelScope.launch {
            try {
                val reopened = RetrofitClient.api.reopenTask(taskId)
                _uiState.update { state ->
                    state.copy(
                        todayTasks = state.todayTasks.map { if (it.id == taskId) reopened else it }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

private fun dueAtLocalDate(dueAt: String): String = try {
    val instant = java.time.Instant.parse(dueAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    "%04d-%02d-%02d".format(zoned.year, zoned.monthValue, zoned.dayOfMonth)
} catch (_: Exception) { dueAt.take(10) }
