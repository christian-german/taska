package com.taska.android.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taska.android.data.model.OccurrenceUpdateRequest
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRepresentationKind
import com.taska.android.data.model.toTaskUpdateRequest
import com.taska.android.data.repository.ProjectRepository
import com.taska.android.data.repository.TaskRepository
import java.util.Calendar
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskBlock(
  val task: TaskDto,
  val startMin: Int,
  val endMin: Int,
  val col: Int,
  val totalCols: Int,
)

data class WeekUiState(
  val weekOffset: Int = 0,
  val weekDays: List<Calendar> = emptyList(),
  val tasksByDay: List<List<TaskBlock>> = List(7) { emptyList() },
  val allDayTasksByDay: List<List<TaskDto>> = List(7) { emptyList() },
  val projects: Map<String, ProjectDto> = emptyMap(),
  val isLoading: Boolean = false,
  val error: String? = null,
)

class WeekViewModel(
  private val taskRepo: TaskRepository,
  private val projectRepo: ProjectRepository,
) : ViewModel() {

  constructor() : this(TaskRepository(), ProjectRepository())

  private val _uiState = MutableStateFlow(WeekUiState())
  val uiState: StateFlow<WeekUiState> = _uiState.asStateFlow()

  init {
    loadForOffset(0)
  }

  fun load() = loadForOffset(_uiState.value.weekOffset)

  fun nextWeek() = loadForOffset(_uiState.value.weekOffset + 1)

  fun prevWeek() = loadForOffset(_uiState.value.weekOffset - 1)

  fun requestRescheduleTask(task: TaskDto, newScheduledAt: String, newEstimateMinutes: Int) {
    executeReschedule(task, newScheduledAt, newEstimateMinutes)
  }

  private fun executeReschedule(
    task: TaskDto,
    newScheduledAt: String,
    newEstimateMinutes: Int,
  ) {
    viewModelScope.launch {
      try {
        val request =
          task
            .toTaskUpdateRequest()
            .copy(
              scheduledAt = newScheduledAt,
              allDay = false,
              estimateMinutes = newEstimateMinutes,
            )
        if (task.kind == TaskRepresentationKind.RECURRING_OCCURRENCE) {
          taskRepo.updateOccurrence(
            task.id,
            checkNotNull(task.occurrenceScheduledAt),
            OccurrenceUpdateRequest(newScheduledAt),
          )
        } else {
          taskRepo.updateTask(task.id, request)
        }
        loadForOffset(_uiState.value.weekOffset)
      } catch (exception: Exception) {
        _uiState.update { it.copy(error = exception.message) }
      }
    }
  }

  private fun loadForOffset(offset: Int) {
    val weekDays = computeWeekDays(offset)
    val from = formatDayStr(weekDays.first())
    val to = formatDayStr(weekDays.last())
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        val tasksDef = async { taskRepo.getTasks(from = from, to = to) }
        val projectsDef = async { projectRepo.getProjects() }
        val tasks = tasksDef.await()
        val projectMap = projectsDef.await().associateBy { it.id }
        val tasksByDay = weekDays.map { day ->
          val dayStr = formatDayStr(day)
          computeLayout(
            tasks.filter { !it.allDay && it.scheduledAt?.let(::scheduledAtLocalDate) == dayStr }
          )
        }
        val allDayTasksByDay = weekDays.map { day ->
          val dayStr = formatDayStr(day)
          tasks.filter { it.allDay && it.scheduledAt?.let(::scheduledAtLocalDate) == dayStr }
        }
        _uiState.update {
          it.copy(
            weekOffset = offset,
            weekDays = weekDays,
            tasksByDay = tasksByDay,
            allDayTasksByDay = allDayTasksByDay,
            projects = projectMap,
            isLoading = false,
          )
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = e.message) }
      }
    }
  }

  private fun computeWeekDays(offset: Int): List<Calendar> {
    val cal =
      Calendar.getInstance().apply {
        add(Calendar.WEEK_OF_YEAR, offset)
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
    return (0 until 7).map { i -> (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) } }
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

    val raws =
      tasks
        .mapNotNull { task ->
          val scheduledAt = task.scheduledAt ?: return@mapNotNull null
          val (h, m) = scheduledAtLocalHourMinute(scheduledAt) ?: return@mapNotNull null
          val startMin = h * 60 + m
          RawBlock(task, startMin, startMin + (task.estimateMinutes ?: 60))
        }
        .sortedBy { it.startMin }

    if (raws.isEmpty()) return emptyList()

    val colEndTimes = mutableListOf<Int>()
    val cols = IntArray(raws.size)
    for (i in raws.indices) {
      val col = colEndTimes.indexOfFirst { it <= raws[i].startMin }
      if (col == -1) {
        cols[i] = colEndTimes.size
        colEndTimes.add(raws[i].endMin)
      } else {
        cols[i] = col
        colEndTimes[col] = raws[i].endMin
      }
    }

    val parent = IntArray(raws.size) { it }
    fun find(x: Int): Int {
      if (parent[x] != x) parent[x] = find(parent[x])
      return parent[x]
    }
    for (i in raws.indices) for (j in i + 1 until raws.size) {
      if (raws[i].endMin > raws[j].startMin) {
        val ri = find(i)
        val rj = find(j)
        if (ri != rj) parent[ri] = rj
      }
    }

    val clusterMaxCol = mutableMapOf<Int, Int>()
    for (i in raws.indices) {
      val root = find(i)
      clusterMaxCol[root] = maxOf(clusterMaxCol.getOrDefault(root, 0), cols[i])
    }

    return raws.mapIndexed { i, raw ->
      TaskBlock(raw.task, raw.startMin, raw.endMin, cols[i], (clusterMaxCol[find(i)] ?: 0) + 1)
    }
  }
}

private fun scheduledAtLocalDate(scheduledAt: String): String =
  try {
    val instant = java.time.Instant.parse(scheduledAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    "%04d-%02d-%02d".format(zoned.year, zoned.monthValue, zoned.dayOfMonth)
  } catch (_: Exception) {
    scheduledAt.take(10)
  }

private fun scheduledAtLocalHourMinute(scheduledAt: String): Pair<Int, Int>? =
  try {
    val instant = java.time.Instant.parse(scheduledAt)
    val zoned = instant.atZone(java.time.ZoneId.systemDefault())
    Pair(zoned.hour, zoned.minute)
  } catch (_: Exception) {
    null
  }
