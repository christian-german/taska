package com.taska.android.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TaskWidgetRefresh {
    private const val MAX_ROWS = 8
    private const val DAY_REFRESH_REQUEST_CODE = 9001
    private val rowIds = intArrayOf(R.id.widget_row_0, R.id.widget_row_1, R.id.widget_row_2, R.id.widget_row_3, R.id.widget_row_4, R.id.widget_row_5, R.id.widget_row_6, R.id.widget_row_7)
    private val checkIds = intArrayOf(R.id.widget_check_0, R.id.widget_check_1, R.id.widget_check_2, R.id.widget_check_3, R.id.widget_check_4, R.id.widget_check_5, R.id.widget_check_6, R.id.widget_check_7)
    private val taskIds = intArrayOf(R.id.widget_task_0, R.id.widget_task_1, R.id.widget_task_2, R.id.widget_task_3, R.id.widget_task_4, R.id.widget_task_5, R.id.widget_task_6, R.id.widget_task_7)
    private val appointmentIds = intArrayOf(R.id.widget_appointment_0, R.id.widget_appointment_1, R.id.widget_appointment_2, R.id.widget_appointment_3, R.id.widget_appointment_4, R.id.widget_appointment_5, R.id.widget_appointment_6, R.id.widget_appointment_7)

    private enum class WidgetType(
        val provider: Class<*>,
        val includeCompleted: Boolean,
    ) {
        WEEK(TaskWidgetProvider::class.java, false),
        TODAY(TodayTaskWidgetProvider::class.java, true),
    }

    fun request(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch { refresh(appContext) }
    }

    suspend fun refresh(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val widgets = WidgetType.entries.associateWith { type ->
            manager.getAppWidgetIds(ComponentName(context, type.provider))
        }
        if (widgets.values.all { it.isEmpty() }) {
            cancelDayRefresh(context)
            return
        }
        scheduleNextDayRefresh(context)
        widgets.filterValues { it.isNotEmpty() }.forEach { (type, widgetIds) ->
            refreshType(context, manager, type, widgetIds)
        }
    }

    private suspend fun refreshType(
        context: Context,
        manager: AppWidgetManager,
        type: WidgetType,
        widgetIds: IntArray,
    ) {
        val range = if (type == WidgetType.WEEK) currentWeek() else currentDay()
        val today = LocalDate.now()
        try {
            RetrofitClient.init(context)
            val repository = TaskRepository()
            val overdueTasks = repository.getTasks(
                showCompleted = false,
                to = today.minusDays(1).toString(),
            )
            val currentTasks = repository.getTasks(
                    showCompleted = type.includeCompleted,
                    from = range.first.toString(),
                    to = range.second.toString(),
                )
            val tasks = filterScheduledTasks(
                overdueTasks + currentTasks,
                range,
                today,
                includeCompleted = type.includeCompleted,
            )
            widgetIds.forEach { widgetId ->
                manager.updateAppWidget(widgetId, render(context, type, widgetId, tasks, range, null))
                if (type == WidgetType.WEEK) manager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_week_list)
            }
        } catch (_: Exception) {
            widgetIds.forEach { widgetId ->
                manager.updateAppWidget(widgetId, render(context, type, widgetId, emptyList(), range, "Unable to refresh tasks"))
                if (type == WidgetType.WEEK) manager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_week_list)
            }
        }
    }

    private fun render(
        context: Context,
        type: WidgetType,
        widgetId: Int,
        tasks: List<TaskDto>,
        range: Pair<LocalDate, LocalDate>,
        error: String?,
    ): RemoteViews = if (type == WidgetType.WEEK) renderWeek(context, widgetId, tasks, range, error) else RemoteViews(context.packageName, R.layout.task_widget).apply {
        setTextViewText(R.id.widget_title, titleFor(type, range))
        setTextViewText(R.id.widget_status, error ?: taskStatus(tasks))
        rowIds.indices.forEach { index ->
            val task = tasks.getOrNull(index)
            setViewVisibility(rowIds[index], if (task == null) View.GONE else View.VISIBLE)
            if (task != null) bindTask(context, index, task, type.includeCompleted)
        }
    }

    private fun renderWeek(
        context: Context,
        widgetId: Int,
        tasks: List<TaskDto>,
        range: Pair<LocalDate, LocalDate>,
        error: String?,
    ): RemoteViews {
        WeekWidgetDataStore.save(context, widgetId, tasks)
        val serviceIntent = Intent(context, WeekTaskWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            data = android.net.Uri.parse("taska://widget/week/$widgetId")
        }
        val actionTemplate = Intent(context, TaskWidgetCompletionReceiver::class.java).apply {
            data = android.net.Uri.parse("taska://widget/week/action/$widgetId")
        }
        return RemoteViews(context.packageName, R.layout.week_task_widget).apply {
            setTextViewText(R.id.widget_title, titleFor(WidgetType.WEEK, range))
            setTextViewText(R.id.widget_status, error ?: taskStatus(tasks))
            setRemoteAdapter(R.id.widget_week_list, serviceIntent)
            setPendingIntentTemplate(
                R.id.widget_week_list,
                PendingIntent.getBroadcast(context, widgetId, actionTemplate, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE),
            )
        }
    }

    private fun titleFor(type: WidgetType, range: Pair<LocalDate, LocalDate>): String = when (type) {
        WidgetType.WEEK -> "Taska · ${range.first.format(DateTimeFormatter.ofPattern("MMM d"))}–${range.second.format(DateTimeFormatter.ofPattern("MMM d"))}"
        WidgetType.TODAY -> "Taska · Today · ${range.first.format(DateTimeFormatter.ofPattern("MMM d"))}"
    }

    private fun taskStatus(tasks: List<TaskDto>): String = when (tasks.size) {
        0 -> "No scheduled tasks"
        1 -> "1 scheduled task"
        else -> "${tasks.size} scheduled tasks"
    }

    private fun RemoteViews.bindTask(context: Context, index: Int, task: TaskDto, canShowCompleted: Boolean) {
        val scheduled = task.scheduledAt!!
        setTextViewText(taskIds[index], "${scheduledDate(scheduled).format(DateTimeFormatter.ofPattern("EEE"))}  ${formatTime(scheduled)}${if (task.allDay) "" else " "}${task.content}")
        val completed = canShowCompleted && task.isCompleted == true
        setImageViewResource(checkIds[index], if (completed) R.drawable.widget_completion_checked else R.drawable.widget_completion_empty)
        setInt(taskIds[index], "setPaintFlags", if (completed) Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG else Paint.ANTI_ALIAS_FLAG)
        bindAppointmentIndicator(context, appointmentIds[index], task.type)

        val action = if (completed) TaskWidgetCompletionReceiver.ACTION_REOPEN else TaskWidgetCompletionReceiver.ACTION_COMPLETE
        val completion = Intent(context, TaskWidgetCompletionReceiver::class.java).apply {
            this.action = action
            putExtra(TaskWidgetCompletionReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskWidgetCompletionReceiver.EXTRA_OCCURRENCE, task.occurrenceScheduledAt)
            data = android.net.Uri.parse("taska://widget/${if (completed) "reopen" else "complete"}/${task.id}/${task.occurrenceScheduledAt ?: "single"}")
        }
        setOnClickPendingIntent(
            checkIds[index],
            PendingIntent.getBroadcast(context, index, completion, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
        )

        val open = Intent(context, TaskDetailActivity::class.java).apply {
            putExtra("task_id", task.id)
            task.occurrenceScheduledAt?.let { putExtra("scheduled_at", it) }
            data = android.net.Uri.parse("taska://widget/task/${task.id}/${task.occurrenceScheduledAt ?: "single"}")
        }
        val pendingOpen = PendingIntent.getActivity(context, index, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        setOnClickPendingIntent(rowIds[index], pendingOpen)
        setOnClickPendingIntent(taskIds[index], pendingOpen)
    }

    private fun RemoteViews.bindAppointmentIndicator(context: Context, viewId: Int, type: String?) {
        val visible = isAppointmentIndicatorVisible(type)
        setViewVisibility(viewId, if (visible) View.VISIBLE else View.GONE)
        setContentDescription(viewId, if (visible) context.getString(R.string.widget_appointment) else null)
    }

    internal fun currentDay(today: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> = today to today

    internal fun currentWeek(today: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
        val monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday to monday.plusDays(6)
    }

    internal fun filterScheduledTasks(
        tasks: List<TaskDto>,
        range: Pair<LocalDate, LocalDate>,
        today: LocalDate = LocalDate.now(),
        includeCompleted: Boolean = false,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<TaskDto> {
        val filtered = tasks
            .distinctBy { it.id to it.occurrenceScheduledAt }
            .filter { task ->
                val date = task.scheduledAt?.let { scheduledDate(it, zone) } ?: return@filter false
                val overdue = date < today && task.isCompleted != true
                val inRange = date in range.first..range.second && (includeCompleted || task.isCompleted != true)
                overdue || inRange
            }
            .sortedWith(compareBy<TaskDto>({ scheduledDate(it.scheduledAt!!, zone) >= today }, { it.scheduledAt }))
        return if (includeCompleted) filtered.take(MAX_ROWS) else filtered
    }

    internal fun nextLocalDayBoundary(now: ZonedDateTime = ZonedDateTime.now()): Instant =
        now.toLocalDate().plusDays(1).atStartOfDay(now.zone).toInstant()

    private fun scheduleNextDayRefresh(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextLocalDayBoundary().toEpochMilli(), dayRefreshIntent(context))
    }

    private fun cancelDayRefresh(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(dayRefreshIntent(context))
    }

    private fun dayRefreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        DAY_REFRESH_REQUEST_CODE,
        Intent(context, WidgetDayChangeReceiver::class.java).setAction(WidgetDayChangeReceiver.ACTION_REFRESH_FOR_NEW_DAY),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun scheduledDate(value: String, zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.parse(value).atZone(zone).toLocalDate()
    private fun formatTime(value: String): String = Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
}

internal fun isAppointmentIndicatorVisible(type: String?): Boolean = type == "APPOINTMENT"
