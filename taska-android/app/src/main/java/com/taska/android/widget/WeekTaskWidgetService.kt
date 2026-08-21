package com.taska.android.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.taska.android.R
import com.taska.android.data.model.TaskDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal sealed interface WeekWidgetItem {
    data object OverdueHeader : WeekWidgetItem
    data class DateHeader(val date: LocalDate) : WeekWidgetItem
    data class Task(val task: TaskDto) : WeekWidgetItem
}

internal object WeekWidgetItems {
    fun build(
        tasks: List<TaskDto>,
        zone: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zone),
    ): List<WeekWidgetItem> {
        var previousDate: LocalDate? = null
        return buildList {
            tasks.sortedBy { it.scheduledAt }.forEach { task ->
                val date = Instant.parse(task.scheduledAt!!).atZone(zone).toLocalDate()
                if (date < today) {
                    if (none { it is WeekWidgetItem.OverdueHeader }) add(WeekWidgetItem.OverdueHeader)
                } else if (date != previousDate) {
                    add(WeekWidgetItem.DateHeader(date))
                }
                add(WeekWidgetItem.Task(task))
                previousDate = date
            }
        }
    }

    fun header(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofPattern("EEE dd/MM", locale))

    fun taskText(task: TaskDto, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String {
        if (task.allDay) return task.content
        val time = Instant.parse(task.scheduledAt!!).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm", locale))
        return "$time  ${task.content}"
    }
}

internal object WeekWidgetDataStore {
    private const val PREFS = "week_widget_tasks"
    fun save(context: Context, widgetId: Int, tasks: List<TaskDto>) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(widgetId.toString(), Gson().toJson(tasks)).apply()

    fun load(context: Context, widgetId: Int): List<TaskDto> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(widgetId.toString(), null) ?: return emptyList()
        return Gson().fromJson(json, object : TypeToken<List<TaskDto>>() {}.type)
    }
}

internal object WeekWidgetOptimisticState {
    private const val PREFS = "week_widget_optimistic_completion"
    private fun key(target: CompletionTarget) = "${target.widgetId}:${target.taskId}:${target.occurrenceScheduledAt.orEmpty()}"

    fun set(context: Context, target: CompletionTarget, checked: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(key(target), checked).apply()

    fun isChecked(context: Context, widgetId: Int, task: TaskDto): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(
            "$widgetId:${task.id}:${task.occurrenceScheduledAt.orEmpty()}",
            false,
        )

    fun clear(context: Context, target: CompletionTarget) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(key(target)).apply()
}

class WeekTaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(
        applicationContext,
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
    )

    private class Factory(private val context: Context, private val widgetId: Int) : RemoteViewsFactory {
        private var items: List<WeekWidgetItem> = emptyList()
        override fun onCreate() = Unit
        override fun onDataSetChanged() { items = WeekWidgetItems.build(WeekWidgetDataStore.load(context, widgetId)) }
        override fun onDestroy() = Unit
        override fun getCount() = items.size
        override fun getViewAt(position: Int): RemoteViews = when (val item = items[position]) {
            WeekWidgetItem.OverdueHeader -> RemoteViews(context.packageName, R.layout.week_widget_date_header).apply {
                setOverdueText(context, R.id.widget_date_header, context.getString(R.string.widget_overdue_header))
            }
            is WeekWidgetItem.DateHeader -> RemoteViews(context.packageName, R.layout.week_widget_date_header).apply {
                setTextViewText(R.id.widget_date_header, WeekWidgetItems.header(item.date))
            }
            is WeekWidgetItem.Task -> RemoteViews(context.packageName, R.layout.week_widget_task_row).apply {
                val text = WeekWidgetItems.taskText(item.task)
                if (item.task.isOverdueWidgetTask()) setOverdueText(context, R.id.widget_task_text, text)
                else setTextViewText(R.id.widget_task_text, text)
                val appointment = isAppointmentIndicatorVisible(item.task.type)
                setViewVisibility(R.id.widget_task_appointment, if (appointment) View.VISIBLE else View.GONE)
                setContentDescription(
                    R.id.widget_task_appointment,
                    if (appointment) context.getString(R.string.widget_appointment) else null,
                )
                setImageViewResource(
                    R.id.widget_task_check,
                    if (WeekWidgetOptimisticState.isChecked(context, widgetId, item.task)) R.drawable.widget_completion_checked
                    else R.drawable.widget_completion_empty,
                )
                val completion = Intent().apply {
                    action = TaskWidgetCompletionReceiver.ACTION_COMPLETE
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_TASK_ID, item.task.id)
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_OCCURRENCE, item.task.occurrenceScheduledAt)
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_WIDGET_ID, widgetId)
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_WIDGET_TYPE, TaskWidgetCompletionReceiver.WIDGET_TYPE_WEEK)
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_CONTROL_ID, R.id.widget_task_check)
                }
                setOnClickFillInIntent(R.id.widget_task_check, completion)
                val open = Intent().apply {
                    action = TaskWidgetCompletionReceiver.ACTION_OPEN
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_TASK_ID, item.task.id)
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_OCCURRENCE, item.task.occurrenceScheduledAt)
                }
                setOnClickFillInIntent(R.id.widget_task_row, open)
                setOnClickFillInIntent(R.id.widget_task_text, open)
            }
        }
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 2
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = false
    }
}
