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
import java.time.format.FormatStyle
import java.util.Locale

internal sealed interface WeekWidgetItem {
    data class DateHeader(val date: LocalDate) : WeekWidgetItem
    data class Task(val task: TaskDto) : WeekWidgetItem
}

internal object WeekWidgetItems {
    fun build(tasks: List<TaskDto>, zone: ZoneId = ZoneId.systemDefault()): List<WeekWidgetItem> {
        var previousDate: LocalDate? = null
        return buildList {
            tasks.sortedBy { it.scheduledAt }.forEach { task ->
                val date = Instant.parse(task.scheduledAt!!).atZone(zone).toLocalDate()
                if (date != previousDate) add(WeekWidgetItem.DateHeader(date))
                add(WeekWidgetItem.Task(task))
                previousDate = date
            }
        }
    }

    fun header(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofPattern("EEE dd/MM", locale))

    fun taskText(task: TaskDto, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String {
        val time = Instant.parse(task.scheduledAt!!).atZone(zone).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
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
            is WeekWidgetItem.DateHeader -> RemoteViews(context.packageName, R.layout.week_widget_date_header).apply {
                setTextViewText(R.id.widget_date_header, WeekWidgetItems.header(item.date))
            }
            is WeekWidgetItem.Task -> RemoteViews(context.packageName, R.layout.week_widget_task_row).apply {
                setTextViewText(R.id.widget_task_text, WeekWidgetItems.taskText(item.task))
                val completion = Intent().apply {
                    action = TaskWidgetCompletionReceiver.ACTION_COMPLETE
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_TASK_ID, item.task.id)
                    putExtra(TaskWidgetCompletionReceiver.EXTRA_OCCURRENCE, item.task.occurrenceScheduledAt)
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
