package com.taska.android

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.model.RegisterDeviceRequest
import com.taska.android.data.repository.DeviceRepository
import com.taska.android.widget.TaskWidgetRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FCM", "Nouveau token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.init(this@TaskaFirebaseMessagingService)
                DeviceRepository().registerDevice(RegisterDeviceRequest(token))
            } catch (exception: Exception) {
                Log.w("FCM", "Échec de l'enregistrement du token", exception)
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["event"] == "tasks_changed") {
            TaskWidgetRefresh.request(this)
            return
        }
        ensureChannel()

        val title = message.data["title"] ?: message.notification?.title ?: "Tâche"
        val body = message.data["body"] ?: message.notification?.body ?: ""
        val taskId = message.data["task_id"]
        val projectId = message.data["project_id"]
        val notifId = System.currentTimeMillis().toInt()

        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (taskId != null) {
            val tapPendingIntent = TaskStackBuilder.create(this).run {
                addNextIntent(Intent(this@TaskaFirebaseMessagingService, TodayActivity::class.java))
                if (projectId != null) {
                    addNextIntent(Intent(this@TaskaFirebaseMessagingService, ProjectActivity::class.java).apply {
                        putExtra("project_id", projectId)
                    })
                }
                addNextIntent(Intent(this@TaskaFirebaseMessagingService, TaskDetailActivity::class.java).apply {
                    putExtra("task_id", taskId)
                })
                getPendingIntent(notifId, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            builder.setContentIntent(tapPendingIntent)

            val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
                putExtra("task_id", taskId)
                putExtra("notif_id", notifId)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                this,
                notifId,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_notification, "Rappel dans 15 min", snoozePendingIntent)
        }

        NotificationManagerCompat.from(this).notify(notifId, builder.build())
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rappels de tâches",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications de rappel pour les tâches à venir"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "taska_reminders"
    }
}
