package com.taska.android.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class TaskBlock(
    val task: TaskDto,
    val startMin: Int,
    val endMin: Int,
    val col: Int,
    val totalCols: Int
)

data class WeekUiState(
    val weekOffset: Int = 0,
    val weekDays: List<Calendar> = emptyList(),
    val tasksByDay: List<List<TaskBlock>> = List(7) { emptyList() },
    val allDayTasksByDay: List<List<TaskDto>> = List(7) { emptyList() },
    val projects: Map<String, ProjectDto> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WeekViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeekUiState())
    val uiState: StateFlow<WeekUiState> = _uiState.asStateFlow()

    private var allTasks: List<TaskDto> = emptyList()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tasksDef = async { RetrofitClient.api.getTasks() }
                val projectsDef = async { RetrofitClient.api.getProjects() }
                allTasks = tasksDef.await()
                val projectMap = projectsDef.await().associateBy { it.id }
                _uiState.update { it.copy(projects = projectMap, isLoading = false) }
                recompute(_uiState.value.weekOffset)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun nextWeek() = recompute(_uiState.value.weekOffset + 1)
    fun prevWeek() = recompute(_uiState.value.weekOffset - 1)

    fun rescheduleTask(taskId: String, newDueAt: String, newEstimateMinutes: Int) {
        val current = allTasks.find { it.id == taskId } ?: return
        viewModelScope.launch {
            try {
                val updated = RetrofitClient.api.updateTask(
                    taskId,
                    TaskRequest(
                        content = current.content,
                        description = current.description,
                        projectId = current.projectId,
                        priority = current.priority,
                        labels = current.labels,
                        dueAt = newDueAt,
                        allDay = false,
                        estimateMinutes = newEstimateMinutes
                    )
                )
                allTasks = allTasks.map { if (it.id == taskId) updated else it }
                recompute(_uiState.value.weekOffset)
            } catch (_: Exception) {}
        }
    }

    private fun recompute(offset: Int) {
        val weekDays = computeWeekDays(offset)
        val tasksByDay = weekDays.map { day ->
            val dayStr = formatDayStr(day)
            computeLayout(allTasks.filter { task ->
                task.isCompleted != true && !task.allDay && task.dueAt != null && dueAtLocalDate(task.dueAt) == dayStr
            })
        }
        val allDayTasksByDay = weekDays.map { day ->
            val dayStr = formatDayStr(day)
            allTasks.filter { task ->
                task.isCompleted != true && task.allDay && task.dueAt != null && dueAtLocalDate(task.dueAt) == dayStr
            }
        }
        _uiState.update {
            it.copy(
                weekOffset = offset,
                weekDays = weekDays,
                tasksByDay = tasksByDay,
                allDayTasksByDay = allDayTasksByDay
            )
        }
    }

    private fun computeWeekDays(offset: Int): List<Calendar> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, offset)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (0 until 7).map { i ->
            (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
        }
    }

    private fun formatDayStr(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    private fun computeLayout(tasks: List<TaskDto>): List<TaskBlock> {
        if (tasks.isEmpty()) return emptyList()

        data class RawBlock(val task: TaskDto, val startMin: Int, val endMin: Int)

        val raws = tasks.mapNotNull { task ->
            val dueAt = task.dueAt ?: return@mapNotNull null
            val (h, m) = dueAtLocalHourMinute(dueAt) ?: return@mapNotNull null
            val startMin = h * 60 + m
            RawBlock(task, startMin, startMin + (task.estimateMinutes ?: 60))
        }.sortedBy { it.startMin }

        if (raws.isEmpty()) return emptyList()

        val colEndTimes = mutableListOf<Int>()
        val cols = IntArray(raws.size)
        for (i in raws.indices) {
            val col = colEndTimes.indexOfFirst { it <= raws[i].startMin }
            if (col == -1) { cols[i] = colEndTimes.size; colEndTimes.add(raws[i].endMin) }
            else { cols[i] = col; colEndTimes[col] = raws[i].endMin }
        }

        val parent = IntArray(raws.size) { it }
        fun find(x: Int): Int { if (parent[x] != x) parent[x] = find(parent[x]); return parent[x] }
        for (i in raws.indices) for (j in i + 1 until raws.size) {
            if (raws[i].endMin > raws[j].startMin) { val ri = find(i); val rj = find(j); if (ri != rj) parent[ri] = rj }
        }

        val clusterMaxCol = mutableMapOf<Int, Int>()
        for (i in raws.indices) { val root = find(i); clusterMaxCol[root] = maxOf(clusterMaxCol.getOrDefault(root, 0), cols[i]) }

        return raws.mapIndexed { i, raw ->
            TaskBlock(raw.task, raw.startMin, raw.endMin, cols[i], (clusterMaxCol[find(i)] ?: 0) + 1)
        }
    }
}

private fun dueAtLocalDate(dueAt: String): String = try {
    val instant = java.time.Instant.parse(dueAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    "%04d-%02d-%02d".format(zoned.year, zoned.monthValue, zoned.dayOfMonth)
} catch (_: Exception) { dueAt.take(10) }

private fun dueAtLocalHourMinute(dueAt: String): Pair<Int, Int>? = try {
    val instant = java.time.Instant.parse(dueAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    Pair(zoned.hour, zoned.minute)
} catch (_: Exception) { null }
