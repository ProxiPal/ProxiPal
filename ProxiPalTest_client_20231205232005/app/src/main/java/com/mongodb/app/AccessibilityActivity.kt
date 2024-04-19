package com.mongodb.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class AccessibilityActivity : AppCompatActivity(), OnInitListener {

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility)

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        // Button click listener
        val buttonSpeak = findViewById<Button>(R.id.button)
        buttonSpeak.setOnClickListener {
            speakOut()
        }
    }

    private fun speakOut() {
        val inputText = findViewById<EditText>(R.id.insertText).text.toString()
        tts.speak(inputText, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language (e.g., Locale.US)
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release TextToSpeech resources
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
