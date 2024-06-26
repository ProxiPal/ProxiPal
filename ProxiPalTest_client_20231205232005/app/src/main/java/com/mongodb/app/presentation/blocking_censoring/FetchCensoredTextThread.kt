package com.mongodb.app.presentation.blocking_censoring

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.mongodb.app.TAG
import com.mongodb.app.data.blocking_censoring.CensoringData.Companion.delimitersCsv
import com.mongodb.app.data.blocking_censoring.CensoringData.Companion.httpUrlConnectionTimeout
import com.mongodb.app.data.blocking_censoring.CensoringData.Companion.urlCsv
import com.mongodb.app.data.blocking_censoring.CensoringData.Companion.urlTxt
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


/**
 * Attempts to fetch lists of profanity to censor from publicly accessible GitHub repositories.
 * However, sometimes this does not successfully retrieve data from the supplied URLs. In this case,
 * try either shutting down and restarting the emulator or restarting Android Studio
 */
class FetchCensoredTextThread : Thread(){
    // region Variables
    val isDoneFetchingData = mutableStateOf(true)
    val dataTxt: MutableList<String> = mutableListOf()
    val dataCsv: MutableList<String> = mutableListOf()
    // endregion Variables


    // Singleton instance
    companion object{
        private var _instance: FetchCensoredTextThread? = null
        val isProfanityRead = mutableStateOf(false)

        fun getInstance(): FetchCensoredTextThread{
            if (_instance == null){
                _instance = FetchCensoredTextThread()
            }
            return _instance!!
        }

        fun endThread(){
            if (_instance != null){
                Log.i(
                    TAG(),
                    "Thread ended"
                )
                _instance!!.interrupt()
                // To allow renewing the instance
                _instance = null
            }
        }
    }


    init{
        if (_instance == null){
            _instance = this
        }
    }


    // region Functions
    /**
     * Attempts to read .txt and .csv files from GitHub repositories
     */
    override fun run() {
        isDoneFetchingData.value = false
        dataTxt.clear()
        dataCsv.clear()

        var url: URL
        var httpURLConnection: HttpURLConnection
        var httpsURLConnection: HttpsURLConnection
        var inputStream: InputStream
        var inputStreamReader: InputStreamReader
        var bufferedReader: BufferedReader
        var currentLine: String? = null

        // Try reading .txt from a URL
        try{
            url = URL(urlTxt)
            httpURLConnection = url.openConnection() as HttpURLConnection
//            httpsURLConnection = url.openConnection() as HttpsURLConnection
            // Set the timeout to 1 minute
            httpURLConnection.connectTimeout = httpUrlConnectionTimeout
            inputStream = httpURLConnection.inputStream
            inputStreamReader = InputStreamReader(inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            currentLine = bufferedReader.readLine()

            while (currentLine != null){
                // If the phrase is a number, don't censor (should only censor offensive words, not numbers)
                if (currentLine.toDoubleOrNull() != null){
                    Log.i(
                        "FetchCensoredTextThread",
                        "Skipping number = \"$currentLine\""
                    )
                    currentLine = bufferedReader.readLine()
                    continue
                }
                dataTxt.add(currentLine)
                // .readLine() automatically moves to the next line after calling
                // Do not call this method more than once per loop iteration
                currentLine = bufferedReader.readLine()
            }

            bufferedReader.close()
        }
        catch (e: Exception){
            Log.e(
                "FetchCensoredTextThread",
                "Caught exception \"$e\" while reading .txt from URL"
            )
        }

        // Try reading .csv from a URL
        try{
            url = URL(urlCsv)
            httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.connectTimeout = httpUrlConnectionTimeout
            inputStream = httpURLConnection.inputStream
            inputStreamReader = InputStreamReader(inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            currentLine = bufferedReader.readLine()

            // Used to skip the 1st line when reading the .csv
            // The 1st line denotes the headers for the .csv, the rest of the lines are the actual data
            var shouldSkip = true
            while (currentLine != null){
                // Skip reading and recording the 1st line of the .csv
                if (shouldSkip){
                    shouldSkip = false
                    currentLine = bufferedReader.readLine()
                    continue
                }
                // This causes a compile error
//                val (text, canForm1, canForm2, canForm3, cat1, cat2, cat3, sevRating, sevDesc) = currentLine.split(',', ignoreCase = false, limit = 9)
                // Split the current line into the keyword/keyphrase to censor and the rest of that row's text
                val (profanityPhrase, otherCategories) = currentLine.split(
                    delimitersCsv,
                    ignoreCase = false,
                    limit = 2
                )
                // If the phrase is a number, don't censor (should only censor offensive words, not numbers)
                if (profanityPhrase.toDoubleOrNull() != null){
                    Log.i(
                        "FetchCensoredTextThread",
                        "Skipping number = \"$profanityPhrase\""
                    )
                    currentLine = bufferedReader.readLine()
                    continue
                }
                dataCsv.add(profanityPhrase)
                currentLine = bufferedReader.readLine()
            }

            bufferedReader.close()
        }
        catch (e: Exception){
            Log.e(
                "FetchCensoredTextThread",
                "Caught exception \"$e\" while reading .csv from URL"
            )
        }

        isDoneFetchingData.value = true
        isProfanityRead.value = true
    }

    override fun interrupt() {
        super.interrupt()
    }
    // endregion Functions
}