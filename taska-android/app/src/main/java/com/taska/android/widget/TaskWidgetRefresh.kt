package com.taska.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.taska.android.R
import com.taska.android.TaskDetailActivity
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.TaskDto
import com.taska.android.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TaskWidgetRefresh {
    private const val MAX_ROWS = 8
    private val rowIds = intArrayOf(R.id.widget_row_0, R.id.widget_row_1, R.id.widget_row_2, R.id.widget_row_3, R.id.widget_row_4, R.id.widget_row_5, R.id.widget_row_6, R.id.widget_row_7)
    private val checkIds = intArrayOf(R.id.widget_check_0, R.id.widget_check_1, R.id.widget_check_2, R.id.widget_check_3, R.id.widget_check_4, R.id.widget_check_5, R.id.widget_check_6, R.id.widget_check_7)
    private val taskIds = intArrayOf(R.id.widget_task_0, R.id.widget_task_1, R.id.widget_task_2, R.id.widget_task_3, R.id.widget_task_4, R.id.widget_task_5, R.id.widget_task_6, R.id.widget_task_7)

    fun request(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch { refresh(appContext) }
    }

    suspend fun refresh(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, TaskWidgetProvider::class.java)
        val widgetIds = manager.getAppWidgetIds(component)
        if (widgetIds.isEmpty()) return
        val week = currentWeek()
        widgetIds.forEach {
            manager.updateAppWidget(it, render(context, emptyList(), week, "Loading scheduled tasks…"))
        }
        try {
            RetrofitClient.init(context)
            val tasks = filterScheduledTasks(
                TaskRepository().getTasks(from = week.first.toString(), to = week.second.toString()),
                week
            )
            widgetIds.forEach { manager.updateAppWidget(it, render(context, tasks, week, null)) }
        } catch (_: Exception) {
            widgetIds.forEach { manager.updateAppWidget(it, render(context, emptyList(), week, "Unable to refresh tasks")) }
        }
    }

    private fun render(context: Context, tasks: List<TaskDto>, week: Pair<LocalDate, LocalDate>, error: String?): RemoteViews =
        RemoteViews(context.packageName, R.layout.task_widget).apply {
            setTextViewText(R.id.widget_title, "Taska · ${week.first.format(DateTimeFormatter.ofPattern("MMM d"))}–${week.second.format(DateTimeFormatter.ofPattern("MMM d"))}")
            setTextViewText(R.id.widget_status, error ?: if (tasks.isEmpty()) "No scheduled tasks" else "${tasks.size} scheduled task${if (tasks.size == 1) "" else "s"}")
            rowIds.indices.forEach { index ->
                val task = tasks.getOrNull(index)
                setViewVisibility(rowIds[index], if (task == null) View.GONE else View.VISIBLE)
                if (task != null) bindTask(context, index, task)
            }
        }

    private fun RemoteViews.bindTask(context: Context, index: Int, task: TaskDto) {
        val scheduled = task.scheduledAt!!
        setTextViewText(taskIds[index], "${scheduledDate(scheduled).format(DateTimeFormatter.ofPattern("EEE"))}  ${formatTime(scheduled)}${if (task.allDay) "" else " "}${task.content}")
        val completion = Intent(context, TaskWidgetCompletionReceiver::class.java).apply {
            action = TaskWidgetCompletionReceiver.ACTION_COMPLETE
            putExtra(TaskWidgetCompletionReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskWidgetCompletionReceiver.EXTRA_OCCURRENCE, task.occurrenceScheduledAt)
            data = android.net.Uri.parse("taska://widget/complete/${task.id}/${task.occurrenceScheduledAt ?: "single"}")
        }
        setOnClickPendingIntent(checkIds[index], PendingIntent.getBroadcast(context, index, completion, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        val open = Intent(context, TaskDetailActivity::class.java).apply {
            putExtra("task_id", task.id)
            task.occurrenceScheduledAt?.let { putExtra("scheduled_at", it) }
            data = android.net.Uri.parse("taska://widget/task/${task.id}/${task.occurrenceScheduledAt ?: "single"}")
        }
        setOnClickPendingIntent(taskIds[index], PendingIntent.getActivity(context, index, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    internal fun currentWeek(today: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
        val monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday to monday.plusDays(6)
    }

    internal fun filterScheduledTasks(tasks: List<TaskDto>, week: Pair<LocalDate, LocalDate>): List<TaskDto> =
        tasks.filter { it.isCompleted != true && it.scheduledAt != null && scheduledDate(it.scheduledAt) in week.first..week.second }
            .sortedBy { it.scheduledAt }
            .take(MAX_ROWS)

    private fun scheduledDate(value: String): LocalDate = Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDate()
    private fun formatTime(value: String): String = Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
}
