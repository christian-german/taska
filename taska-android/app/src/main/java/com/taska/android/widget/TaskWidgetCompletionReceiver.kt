package com.taska.android.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.taska.android.R
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal enum class CompletionWidget { WEEK, TODAY }

internal data class CompletionTarget(
    val widget: CompletionWidget,
    val widgetId: Int,
    val controlId: Int,
    val taskId: String,
    val occurrenceScheduledAt: String?,
)

internal interface CompletionPresentation {
    fun showChecked(target: CompletionTarget)
    fun showUnchecked(target: CompletionTarget)
    fun clear(target: CompletionTarget)
}

private class RemoteViewsCompletionPresentation(private val context: Context) : CompletionPresentation {
    private val manager = AppWidgetManager.getInstance(context)

    override fun showChecked(target: CompletionTarget) = update(target, checked = true)
    override fun showUnchecked(target: CompletionTarget) = update(target, checked = false)

    override fun clear(target: CompletionTarget) {
        if (target.widget == CompletionWidget.WEEK) WeekWidgetOptimisticState.clear(context, target)
    }

    private fun update(target: CompletionTarget, checked: Boolean) {
        if (target.widget == CompletionWidget.WEEK) {
            WeekWidgetOptimisticState.set(context, target, checked)
            manager.notifyAppWidgetViewDataChanged(target.widgetId, R.id.widget_week_list)
        } else {
            val views = RemoteViews(context.packageName, R.layout.task_widget).apply {
                setImageViewResource(target.controlId, if (checked) R.drawable.widget_completion_checked else R.drawable.widget_completion_empty)
            }
            manager.partiallyUpdateAppWidget(target.widgetId, views)
        }
    }
}

class TaskWidgetCompletionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_OPEN) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
            context.startActivity(Intent(context, com.taska.android.TaskDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("task_id", taskId)
                intent.getStringExtra(EXTRA_OCCURRENCE)?.let { putExtra("scheduled_at", it) }
            })
            return
        }
        if (action != ACTION_COMPLETE && action != ACTION_REOPEN) return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val target = if (action == ACTION_COMPLETE) completionTarget(intent, taskId) ?: return else null
        val presentation = RemoteViewsCompletionPresentation(context)

        // Acknowledge completion before asynchronous initialization and the API request.
        target?.let { beginCompletion(presentation, it) }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                executeWithRollback(target, presentation) {
                    RetrofitClient.init(context)
                    executeAction(
                        repository = TaskRepository(),
                        action = action,
                        taskId = taskId,
                        occurrenceScheduledAt = intent.getStringExtra(EXTRA_OCCURRENCE),
                        target = target,
                        presentation = presentation,
                        refresh = { TaskWidgetRefresh.refresh(context.applicationContext) },
                    )
                }
            } catch (_: Exception) {
                // The presentation has already been restored; BroadcastReceivers cannot surface request errors.
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.taska.android.widget.COMPLETE_TASK"
        const val ACTION_REOPEN = "com.taska.android.widget.REOPEN_TASK"
        const val ACTION_OPEN = "com.taska.android.widget.OPEN_TASK"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_OCCURRENCE = "occurrence_scheduled_at"
        const val EXTRA_WIDGET_ID = "widget_id"
        const val EXTRA_WIDGET_TYPE = "widget_type"
        const val EXTRA_CONTROL_ID = "control_id"
        const val WIDGET_TYPE_WEEK = "week"
        const val WIDGET_TYPE_TODAY = "today"

        private fun completionTarget(intent: Intent, taskId: String): CompletionTarget? {
            val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val controlId = intent.getIntExtra(EXTRA_CONTROL_ID, 0)
            val widget = when (intent.getStringExtra(EXTRA_WIDGET_TYPE)) {
                WIDGET_TYPE_WEEK -> CompletionWidget.WEEK
                WIDGET_TYPE_TODAY -> CompletionWidget.TODAY
                else -> return null
            }
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID || controlId == 0) return null
            return CompletionTarget(widget, widgetId, controlId, taskId, intent.getStringExtra(EXTRA_OCCURRENCE))
        }

        internal fun beginCompletion(presentation: CompletionPresentation, target: CompletionTarget) {
            presentation.showChecked(target)
        }

        internal suspend fun executeWithRollback(
            target: CompletionTarget?,
            presentation: CompletionPresentation,
            action: suspend () -> Unit,
        ) {
            try {
                action()
            } catch (error: Exception) {
                target?.let(presentation::showUnchecked)
                throw error
            }
        }

        internal suspend fun executeAction(
            repository: TaskRepository,
            action: String,
            taskId: String,
            occurrenceScheduledAt: String?,
            target: CompletionTarget? = null,
            presentation: CompletionPresentation? = null,
            refresh: suspend () -> Unit = {},
        ) {
            when (action) {
                ACTION_COMPLETE -> repository.closeTask(taskId, occurrenceScheduledAt)
                ACTION_REOPEN -> repository.reopenTask(taskId, occurrenceScheduledAt)
                else -> return
            }
            refresh()
            if (target != null) presentation?.clear(target)
        }

        internal suspend fun performAction(repository: TaskRepository, action: String, taskId: String, occurrenceScheduledAt: String?) =
            executeAction(repository, action, taskId, occurrenceScheduledAt)
    }
}
