/*
Programmer: Brian Poon
 */
package com.mongodb.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Defining the class `SettingsActivity` that extends `AppCompatActivity`
class SettingsActivity : AppCompatActivity() {

    // Overriding the `onCreate` method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the content view of the activity to the layout resource `activity_settings`
        setContentView(R.layout.activity_settings)

        // Finding the TextView for the Profile Info and attaching a click listener
        val profileInfoTextView = findViewById<TextView>(R.id.tvProfileInfo)
        profileInfoTextView.setOnClickListener {

            // Showing a toast message when the Profile Info is clicked
            Toast.makeText(this, "Profile Info clicked!", Toast.LENGTH_SHORT).show()
        }

        // Finding the TextView for the Language and attaching a click listener
        val languageTextView = findViewById<TextView>(R.id.tvLanguage)
        languageTextView.setOnClickListener {

            // Creating an intent to navigate to the `LanguageActivity` and starting the activity
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }

        // Finding the TextView for the Allow Notifications and attaching a click listener
        val allowNotificationsTextView = findViewById<TextView>(R.id.tvAllowNotifications)
        allowNotificationsTextView.setOnClickListener {

            // Creating an intent to navigate to the `AllowNotificationsActivity` and starting the activity
            val intent = Intent(this, AllowNotificationsActivity::class.java)
            startActivity(intent)
        }

        // Finding the TextView for the User Filters and attaching a click listener
        val userFiltersTextView = findViewById<TextView>(R.id.tvUserFilters)
        userFiltersTextView.setOnClickListener {

            // Showing a toast message when the User Filters is clicked
            Toast.makeText(this, "User Filters clicked!", Toast.LENGTH_SHORT).show()
        }

        // Finding the TextView for the Advanced Settings and attaching a click listener
        val advancedSettingsTextView = findViewById<TextView>(R.id.tvAdvancedSettings)
        advancedSettingsTextView.setOnClickListener {

            // Showing a toast message when the Advanced Settings is clicked
            Toast.makeText(this, "Advanced Settings clicked!", Toast.LENGTH_SHORT).show()
        }

        // Finding the TextView for the Privacy Policy and attaching a click listener
        val privacyPolicyTextView = findViewById<TextView>(R.id.tvPrivacyPolicy)
        privacyPolicyTextView.setOnClickListener {

            // Creating an intent to navigate to the `PrivacyPolicyActivity` and starting the activity
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
    }
}