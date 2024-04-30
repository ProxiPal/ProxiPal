/*
 * Programmer: Brian Poon
 * Date: 2/17/24
 * This Class creates notifications for new friend requests
 */
package com.mongodb.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NewFriendRequestNotificationService(// The context of the application
    private val context: Context
) {
    private val notificationManager: NotificationManager

    // Shows a notification for a new friend request
    fun showNotification() {
        // Create an intent to open the main activity of the app when the notification is tapped
        val activityIntent = Intent(context, TemplateApp::class.java)

        // Create a pending intent to be executed when the notification is tapped
        val activityPendingIntent = PendingIntent.getActivity(
            context,  // Context
            1,  // Unique request code
            activityIntent,  // The intent to execute
            PendingIntent.FLAG_IMMUTABLE // Flags for the pending intent
        )

        // Build the notification with basic information
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_3p_24) // Icon to display in the notification bar
                .setContentTitle("ProxiPal") // Title of the notification
                .setContentText(context.getString(R.string.new_friend_request_notification_service_content_body)) // Body text of the notification
                .setContentIntent(activityPendingIntent) // Intent to execute when the notification is tapped
                .setVibrate(
                    longArrayOf(
                        0,
                        250,
                        250,
                        250
                    )
                ) // Vibrate pattern (silent, vibrate, silent, vibrate)

        // Show the notification using the notification manager
        notificationManager.notify(1, notificationBuilder.build())
    }

    init {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        // The ID of the notification channel for this service
        const val CHANNEL_ID = "counter_channel2"
    }
}
