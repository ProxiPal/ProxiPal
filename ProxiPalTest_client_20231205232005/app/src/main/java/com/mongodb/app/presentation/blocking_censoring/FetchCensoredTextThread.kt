package com.mongodb.app.presentation.blocking_censoring

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


// If this ends up not working, try restarting the app and re-running
// If this still does not work, try shutting down the emulator and restarting it
// ... or try restarting Android Studio
class FetchCensoredTextThread : Thread(){
    // region Variables
    val isDoneFetchingData = mutableStateOf(true)
    val dataTxt: MutableList<String> = mutableListOf()
    private val _urlTxt = "https://raw.githubusercontent.com/dsojevic/profanity-list/main/en.txt"
    private val _urlCsv = "https://raw.githubusercontent.com/surge-ai/profanity/main/profanity_en.csv"
    private val _httpUrlConnectionTimeout = 60000
    // endregion Variables


    // Singleton instance
    companion object{
        private var _instance: FetchCensoredTextThread? = null

        fun getInstance(): FetchCensoredTextThread{
            if (_instance == null){
                _instance = FetchCensoredTextThread()
            }
            return _instance!!
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

        var url: URL
        var httpURLConnection: HttpURLConnection
        var httpsURLConnection: HttpsURLConnection
        var inputStream: InputStream
        var inputStreamReader: InputStreamReader
        var bufferedReader: BufferedReader
        var currentLine: String? = null

        try{
            url = URL(_urlTxt)
            httpURLConnection = url.openConnection() as HttpURLConnection
//            httpsURLConnection = url.openConnection() as HttpsURLConnection
            // Set the timeout to 1 minute
            httpURLConnection.connectTimeout = _httpUrlConnectionTimeout
            inputStream = httpURLConnection.inputStream
            inputStreamReader = InputStreamReader(inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            currentLine = bufferedReader.readLine()

            while (currentLine != null){
                dataTxt.add(currentLine)
                // .readLine() automatically moves to the next line after calling
                // Do not call this method more than once per loop iteration
                currentLine = bufferedReader.readLine()
            }

            bufferedReader.close()
        }
        catch (e: Exception){
            Log.e(
                "TAG()",
                "FetchCensoredTextThread: Caught exception \"$e\" while trying to load .txt from URL"
            )
        }

        val newData: MutableList<String> = mutableListOf()
        try{
            url = URL(_urlCsv)
            httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.connectTimeout = _httpUrlConnectionTimeout
            inputStream = httpURLConnection.inputStream
            inputStreamReader = InputStreamReader(inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            currentLine = bufferedReader.readLine()

            while (currentLine != null){
                // This causes a compile error
//                val (text, canForm1, canForm2, canForm3, cat1, cat2, cat3, sevRating, sevDesc) = currentLine.split(',', ignoreCase = false, limit = 9)
                // Split the current line into the keyword/keyphrase to censor and the rest of that row's text
                val (keyPhrase, otherCategories) = currentLine.split(',', ignoreCase = false, limit = 2)
                Log.i(
                    "TAG()",
                    "FetchCensoredTextThread: .csv split = \"$keyPhrase\""
                )

                newData.add(currentLine)
                currentLine = bufferedReader.readLine()
            }

            bufferedReader.close()
        }
        catch (e: Exception){
            Log.e(
                "TAG()",
                "FetchCensoredTextThread: Caught exception \"$e\" while trying to load .csv from URL"
            )
        }


        isDoneFetchingData.value = true
    }
    // endregion Functions
}