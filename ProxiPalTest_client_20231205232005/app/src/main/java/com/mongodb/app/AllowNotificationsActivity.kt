/*
Programmer: Brian Poon
 */
package com.mongodb.app

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


// Defining the class `AllowNotificationsActivity` that extends `ComponentActivity`
class AllowNotificationsActivity : ComponentActivity() {

    // Overriding the `onCreate` method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting up the UI using Jetpack Compose
        setContent {
            // Retrieve the current context
            val context = LocalContext.current

            // Declaring a boolean variable `hasNotificationPermission` and remembering its state across recompositions
            var hasNotificationPermission by remember {
                // Checking if the device's SDK version is TIRAMISU or higher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Assigning the result of the check for the notification permission to `hasNotificationPermission`
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                } else {
                    // If the device's SDK version is below TIRAMISU, default to true (permission is assumed to be granted)
                    mutableStateOf(true)
                }
            }

            // Creating a column layout with buttons
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Remembering the result of the permission request
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // Updating the `hasNotificationPermission` variable based on the permission result
                        hasNotificationPermission = isGranted
                    }
                )

                // Button to request permission
                Button(onClick = {
                    // If notification permission is already granted, show a toast message
                    if (hasNotificationPermission) {
                        showNotificationPermissionsAlreadyAllowedMessage()
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // If SDK version is TIRAMISU or above, request permission
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text(text = "Allow permission")
                }

                // Button to test sending a notification
                Button(onClick = {
                    // If notification permission is granted, show a test notification
                    if (hasNotificationPermission) {
                        showNotification()
                    }
                }) {
                    Text(text = "Test notification")
                }
            }
        }
    }

    // Function to display a notification
    private fun showNotification() {
        // Building a notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("ProxiPal") // Title of the notification
            .setContentText("Notification permissions have been allowed!")
            .build()

        // Getting the NotificationManager service
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Showing the notification
        notificationManager.notify(1, notification)
    }

    // Function to show a toast message when notification permissions are already allowed
    private fun showNotificationPermissionsAlreadyAllowedMessage() {
        Toast.makeText(this, "Notifications already allowed!", Toast.LENGTH_SHORT).show()
    }

    // Companion object to hold constants
    companion object {
        // The ID of the notification channel for this service
        const val CHANNEL_ID = "test_notification_permissions_allowed"
    }
}


