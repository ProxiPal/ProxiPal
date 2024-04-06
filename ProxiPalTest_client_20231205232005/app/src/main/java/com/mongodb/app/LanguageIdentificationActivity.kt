package com.mongodb.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.languageid.LanguageIdentification

class LanguageIdentificationActivity : AppCompatActivity() {

    private lateinit var textInput: EditText // Declare a variable to hold the reference to the EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_identification) // Set the layout for this activity

        // Initialize the 'textInput' variable with the reference to the EditText with ID 'insertText'
        textInput = findViewById(R.id.insertText)

        // Get the reference to the button with ID 'button'
        val button = findViewById<View>(R.id.button) as Button

        // Set a click listener for the button
        button.setOnClickListener {
            // Get the reference to the EditText again (redundant, as it's already done above)
            val textInput = findViewById<EditText>(R.id.insertText)

            // Retrieve the current text from the EditText
            val currentText = textInput.text.toString()

            // Call the function to identify the language of the provided text
            identifyLanguage(currentText)
        }
    }

    private fun identifyLanguage(text: String) {
        // Create an instance of LanguageIdentificationClient to identify the language
        val languageIdentifier = LanguageIdentification.getClient()

        // Initiate the language identification process for the provided text
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                // If the language is successfully identified
                if (languageCode == "und") { // Check if the identified language code is "und" (undetermined)
                    // Display a toast message indicating that the language couldn't be identified
                    Toast.makeText(applicationContext, "Can't identify language.", Toast.LENGTH_LONG).show()
                } else {
                    // Display a toast message showing the identified language code
                    Toast.makeText(applicationContext, languageCode, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                // If an error occurs during language identification
                // Display a toast message indicating the failure
                Toast.makeText(applicationContext, "Model couldn’t be loaded or other internal error.", Toast.LENGTH_LONG).show()
            }
    }
}
