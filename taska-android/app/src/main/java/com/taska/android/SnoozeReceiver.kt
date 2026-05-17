package com.taska.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.TaskRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val notifId = intent.getIntExtra("notif_id", 0)

        val pendingResult = goAsync()
        RetrofitClient.init(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = RetrofitClient.api.getTask(taskId)
                val newDueAt = task.dueAt?.let { addMinutes(it, 15) } ?: return@launch

                RetrofitClient.api.updateTask(
                    taskId,
                    TaskRequest(
                        content = task.content,
                        description = task.description,
                        projectId = task.projectId,
                        priority = task.priority,
                        labels = task.labels,
                        dueAt = newDueAt,
                        allDay = task.allDay,
                        estimateMinutes = task.estimateMinutes
                    )
                )

                NotificationManagerCompat.from(context).cancel(notifId)
            } catch (e: Exception) {
                Log.e("SnoozeReceiver", "Erreur lors du report de la tâche", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun addMinutes(dueAt: String, minutes: Int): String = try {
        java.time.Instant.parse(dueAt).plusSeconds(minutes * 60L).toString()
    } catch (_: Exception) { dueAt }
}
