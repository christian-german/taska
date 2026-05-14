package com.taska.android

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TaskaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Token rafraîchi → mettre à jour le backend
        Log.d("FCM", "Nouveau token: $token")
        // Tu peux réutiliser la même logique que dans MainActivity
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: "Tâche"
        val body = message.notification?.body ?: ""

        val notification = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}