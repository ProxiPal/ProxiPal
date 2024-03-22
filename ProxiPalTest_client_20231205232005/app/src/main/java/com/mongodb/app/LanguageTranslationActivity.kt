package com.mongodb.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class LanguageTranslationActivity : AppCompatActivity() {

    private lateinit var textInput: EditText

    private val conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_translation)


        // The text to be identified for its language
        textInput = findViewById(R.id.insertText)

        val englishToSpanishTranslator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build())

        val englishToChineseTranslator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.CHINESE)
                .build())


        val translateEnglishToSpanishButton = findViewById<View>(R.id.translateEnglishToSpanishButton) as Button
        translateEnglishToSpanishButton.setOnClickListener {
            val textInput = findViewById<EditText>(R.id.insertText) // Get the reference to the EditText
            val currentText = textInput.text.toString() // Retrieve the current text from the EditText

            translate(englishToSpanishTranslator, currentText)
        }

        val translateEnglishToChineseButton = findViewById<View>(R.id.translateEnglishToChineseButton) as Button
        translateEnglishToChineseButton.setOnClickListener {
            val textInput = findViewById<EditText>(R.id.insertText) // Get the reference to the EditText
            val currentText = textInput.text.toString() // Retrieve the current text from the EditText

            translate(englishToChineseTranslator, currentText)
        }

    }

    private fun translate(translator: Translator, text: String) {
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                //Toast.makeText(applicationContext, "Model downloaded successfully. Okay to start translating.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Model couldn’t be downloaded or other internal error.", Toast.LENGTH_SHORT).show()
            }

        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                Toast.makeText(applicationContext, translatedText, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error.", Toast.LENGTH_LONG).show()
            }
    }
}