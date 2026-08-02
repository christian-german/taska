package com.taska.android.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class DayTaskBlock(
    val task: TaskDto,
    val startMin: Int,
    val endMin: Int,
    val col: Int,
    val totalCols: Int
)

data class PendingReschedule(val task: TaskDto, val newScheduledAt: String, val newEstimateMinutes: Int)

data class DayUiState(
    val dayOffset: Int = 0,
    val currentDay: Calendar = Calendar.getInstance(),
    val tasks: List<DayTaskBlock> = emptyList(),
    val allDayTasks: List<TaskDto> = emptyList(),
    val projects: Map<String, ProjectDto> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingReschedule: PendingReschedule? = null
)

class DayViewModel(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository,
) : ViewModel() {

    constructor() : this(TaskRepository(), ProjectRepository())

    private val _uiState = MutableStateFlow(DayUiState())
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    init { loadForOffset(0) }

    fun load() = loadForOffset(_uiState.value.dayOffset)

    fun nextDay() = loadForOffset(_uiState.value.dayOffset + 1)
    fun prevDay() = loadForOffset(_uiState.value.dayOffset - 1)

    fun requestRescheduleTask(task: TaskDto, newScheduledAt: String, newEstimateMinutes: Int) {
        if (task.isRecurring == true && task.occurrenceScheduledAt != null) {
            _uiState.update { it.copy(pendingReschedule = PendingReschedule(task, newScheduledAt, newEstimateMinutes)) }
        } else {
            executeReschedule(task, newScheduledAt, newEstimateMinutes, scope = null)
        }
    }

    fun confirmRescheduleTask(scope: RecurrenceScope?) {
        val pending = _uiState.value.pendingReschedule ?: return
        _uiState.update { it.copy(pendingReschedule = null) }
        executeReschedule(pending.task, pending.newScheduledAt, pending.newEstimateMinutes, scope)
    }

    fun dismissRescheduleScope() {
        _uiState.update { it.copy(pendingReschedule = null) }
    }

    private fun executeReschedule(task: TaskDto, newScheduledAt: String, newEstimateMinutes: Int, scope: RecurrenceScope?) {
        viewModelScope.launch {
            try {
                taskRepo.updateTask(
                    task.id,
                    TaskRequest(
                        content = task.content,
                        description = task.description,
                        projectId = task.projectId,
                        priority = task.priority,
                        labels = task.labels,
                        scheduledAt = newScheduledAt,
                        allDay = false,
                        estimateMinutes = newEstimateMinutes,
                        scope = scope?.name,
                        occurrenceScheduledAt = task.occurrenceScheduledAt
                    )
                )
                loadForOffset(_uiState.value.dayOffset)
            } catch (_: Exception) {}
        }
    }

    private fun loadForOffset(offset: Int) {
        val day = computeDay(offset)
        val dateStr = formatDayStr(day)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tasksDef = async { taskRepo.getTasks(date = dateStr) }
                val projectsDef = async { projectRepo.getProjects() }
                val tasks = tasksDef.await()
                val projectMap = projectsDef.await().associateBy { it.id }
                val timedBlocks = computeLayout(tasks.filter { !it.allDay && it.scheduledAt != null })
                val allDayTasks = tasks.filter { it.allDay }
                _uiState.update {
                    it.copy(
                        dayOffset = offset,
                        currentDay = day,
                        tasks = timedBlocks,
                        allDayTasks = allDayTasks,
                        projects = projectMap,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun computeDay(offset: Int): Calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private fun formatDayStr(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    private fun computeLayout(tasks: List<TaskDto>): List<DayTaskBlock> {
        if (tasks.isEmpty()) return emptyList()
        data class Raw(val task: TaskDto, val startMin: Int, val endMin: Int)
        val raws = tasks.mapNotNull { task ->
            val scheduledAt = task.scheduledAt ?: return@mapNotNull null
            val (h, m) = scheduledAtLocalHourMinute(scheduledAt) ?: return@mapNotNull null
            val start = h * 60 + m
            Raw(task, start, start + (task.estimateMinutes ?: 60))
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
            DayTaskBlock(raw.task, raw.startMin, raw.endMin, cols[i], (clusterMaxCol[find(i)] ?: 0) + 1)
        }
    }
}

private fun scheduledAtLocalHourMinute(scheduledAt: String): Pair<Int, Int>? = try {
    val instant = java.time.Instant.parse(scheduledAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    Pair(zoned.hour, zoned.minute)
} catch (_: Exception) { null }
