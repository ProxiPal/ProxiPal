
/*
* Programmer: Brian Poon
* Date: 2/17/24
* This Class creates notifications for new friend requests
* */

package com.mongodb.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NewFriendRequestNotificationService(
    private val context: Context // The context of the application
) {
    // Initializes the NotificationManager for managing notifications
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Shows a notification for a new friend request
    fun showNotification() {
        // Create an intent to open the main activity of the app when the notification is tapped
        val activityIntent = Intent(context, TemplateApp::class.java)

        // Create a pending intent to be executed when the notification is tapped
        val activityPendingIntent = PendingIntent.getActivity(
            context, // Context
            1, // Unique request code
            activityIntent, // The intent to execute
            PendingIntent.FLAG_IMMUTABLE // Flags for the pending intent
        )

        // Build the notification with basic information
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_3p_24) // Icon to display in the notification bar
            .setContentTitle("ProxiPal") // Title of the notification
            .setContentText("You have a new friend request!") // Body text of the notification
            .setContentIntent(activityPendingIntent) // Intent to execute when the notification is tapped
            .build()

        // Show the notification using the notification manager
        notificationManager.notify(1, notification)
    }

    companion object {
        // The ID of the notification channel for this service
        const val CHANNEL_ID = "counter_channel2"
    }
}
