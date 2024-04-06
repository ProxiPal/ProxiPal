/*
Programmer: Brian Poon
This class file is for receiving push notifications from Firebase Cloud Messaging
 */
package com.mongodb.app

import android.R
import android.app.Notification
import android.app.Notification.DEFAULT_SOUND
import android.app.Notification.DEFAULT_VIBRATE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Programmer: Brian Poon
class MyFirebaseMsgService : FirebaseMessagingService() {

    // This function is called when a new message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Log the received message
        Log.d("MyFirebaseMsgService", "onMessageReceived: " + remoteMessage.data["message"])

        // Create an intent to launch when notification is clicked
        val intent = Intent(this, MyFirebaseMsgService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Create a PendingIntent for the intent
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Define notification channel ID
        val channelId = "MyFirebaseMsgNotificationService"

        // Build the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body)
            .setAutoCancel(true).setContentIntent(pendingIntent)
            .setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)

        // Get notification manager
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        val channel = NotificationChannel(
            channelId,
            "Firebase Cloud Messaging Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Used for firebase cloud messaging"
        channel.setShowBadge(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(channel)

        // Notify with the built notification
        manager.notify(0, builder.build())
    }
}