package com.mongodb.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration

lateinit var app: App

// global Kotlin extension that resolves to the short version
// of the name of the current class. Used for labelling logs.
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/*
*  Sets up the App and enables Realm-specific logging in debug mode.
*/
class TemplateApp: Application() {

    override fun onCreate() {
        super.onCreate()
        createNewFriendRequestNotificationChannel() // Create notification channel for new friend request push notifications
        createTestNotificationPermissionsNotificationChannel() // Create notification channel for testing notification permissions
        getFirebaseMessagingToken() // Get Device FCM Token



        app = App.create(
            AppConfiguration.Builder(getString(R.string.realm_app_id))
                .baseUrl(getString(R.string.realm_base_url))
                .build()
        )




        Log.v(TAG(), "Initialized the App configuration for: ${app.configuration.appId}")
        // If you're getting this app code by cloning the repository at
        // https://github.com/mongodb/template-app-kotlin-todo, 
        // it does not contain the data explorer link. Download the
        // app template from the Atlas UI to view a link to your data.
        Log.v(TAG(),"To see your data in Atlas, follow this link:" + getString(R.string.realm_data_explorer_link))


        // Show Friend Request Notification
        val service = NewFriendRequestNotificationService(applicationContext)
        service.showNotification()
    }

    // Programmer: Brian Poon
    // Creates a new notification channel to handle new friend request notifications
    private fun createNewFriendRequestNotificationChannel() {
        // Creates a new notification channel with the given ID, name, and importance level
        val channel = NotificationChannel(
            NewFriendRequestNotificationService.CHANNEL_ID, // The ID of the channel
            "New Friend Request", // The name of the channel
            NotificationManager.IMPORTANCE_HIGH // The importance level of the channel
        )

        // Sets a description for the channel
        channel.description = "Used for new friend request notifications"

        // Gets the notification manager service
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Creates the notification channel
        notificationManager.createNotificationChannel(channel)
    }

    // Programmer: Brian Poon
    // Creates a new notification channel to handle testing if notification permissions are enabled
    private fun createTestNotificationPermissionsNotificationChannel() {
        // Creates a new notification channel with the given ID, name, and importance level
        val channel = NotificationChannel(
            AllowNotificationsActivity.CHANNEL_ID, // The ID of the channel
            "Test Notification For Permissions Enabled", // The name of the channel
            NotificationManager.IMPORTANCE_HIGH // The importance level of the channel
        )

        // Sets a description for the channel
        channel.description = "Used for testing if notification permissions are enabled"

        // Gets the notification manager service
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Creates the notification channel
        notificationManager.createNotificationChannel(channel)
    }

    // Programmer: Brian Poon
    private fun getFirebaseMessagingToken() {
        // Get instance of FirebaseMessaging
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            // Check if token retrieval was successful
            if (!task.isSuccessful) {
                // Log an error message if token retrieval failed
                Log.w(TAG(), "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Token retrieval was successful, retrieve the token
            val token = task.result

            // Display Token with a Toast
            val msg = "THE TOKEN IS $token"
            // Show token in a Toast message
            Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
            // Log token for debugging purposes
            Log.d(TAG(), msg)
        })
    }

}
