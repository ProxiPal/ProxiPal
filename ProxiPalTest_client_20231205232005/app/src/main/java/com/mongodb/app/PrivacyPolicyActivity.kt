/*
Programmer: Brian Poon
 */
package com.mongodb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Defining the class `PrivacyPolicyActivity` that extends `AppCompatActivity`
class PrivacyPolicyActivity : AppCompatActivity() {

    // Overriding the `onCreate` method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the content view of the activity to the layout resource `activity_privacy_policy`
        setContentView(R.layout.activity_privacy_policy)
    }
}
