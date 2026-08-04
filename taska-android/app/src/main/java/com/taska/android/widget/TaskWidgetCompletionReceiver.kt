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
        if (intent.action != ACTION_COMPLETE) return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.init(context)
                TaskRepository().closeTask(taskId, intent.getStringExtra(EXTRA_OCCURRENCE))
                TaskWidgetRefresh.refresh(context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.taska.android.widget.COMPLETE_TASK"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_OCCURRENCE = "occurrence_scheduled_at"
    }
}
