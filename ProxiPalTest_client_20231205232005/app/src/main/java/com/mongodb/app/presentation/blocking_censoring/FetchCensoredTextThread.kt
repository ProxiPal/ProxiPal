package com.mongodb.app.presentation.blocking_censoring

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


// If this ends up not working, try shutting down the emulator and restarting it
// If still not working, try restarting Android Studio
class FetchCensoredTextThread : Thread(){
    // region Variables
    val isDoneFetchingData = mutableStateOf(true)
    val data: MutableList<String> = mutableListOf()
    // endregion Variables


    // region Functions
    override fun run() {
        isDoneFetchingData.value = false
        data.clear()
        try{
            val url: URL = URL("https://raw.githubusercontent.com/dsojevic/profanity-list/main/en.txt")
            val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
//            val httpsURLConnection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            // Set the timeout to 1 minute
            httpURLConnection.connectTimeout = 60000
            val inputStream: InputStream = httpURLConnection.inputStream
            val inputStreamReader: InputStreamReader = InputStreamReader(inputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            var line: String? = bufferedReader.readLine()

            while (line != null){
                data.add(line)
                // .readLine() automatically moves to the next line after calling
                // Do not call this method more than once per loop iteration
                line = bufferedReader.readLine()
            }
        }
        catch (e: Exception){
            Log.e(
                "TAG()",
                "FetchCensoredTextThread: Caught exception \"$e\" while trying to load URL"
            )
        }
        isDoneFetchingData.value = true
    }
    // endregion Functions
}