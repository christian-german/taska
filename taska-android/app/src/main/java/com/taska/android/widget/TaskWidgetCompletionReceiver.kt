package com.taska.android.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.init(context)
                performAction(TaskRepository(), action, taskId, intent.getStringExtra(EXTRA_OCCURRENCE))
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

        internal suspend fun performAction(
            repository: TaskRepository,
            action: String,
            taskId: String,
            occurrenceScheduledAt: String?,
        ) {
            when (action) {
                ACTION_COMPLETE -> repository.closeTask(taskId, occurrenceScheduledAt)
                ACTION_REOPEN -> repository.reopenTask(taskId, occurrenceScheduledAt)
            }
        }
    }
}
