/*
Programmer: Brian Poon
 */
package com.mongodb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Defining the class `LanguageActivity` that extends `AppCompatActivity`
class LanguageActivity : AppCompatActivity() {

    // Overriding the `onCreate` method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the content view of the activity to the layout resource `activity_language`
        setContentView(R.layout.activity_language)
    }
}