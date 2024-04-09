package com.mongodb.app.presentation.blocking_censoring

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class FetchCensoredTextThread : Thread(){
    val data = mutableStateOf("")


    override fun run() {
        try{
            val url: URL = URL("https://raw.githubusercontent.com/dsojevic/profanity-list/main/en.txt")
            val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            val inputStream: InputStream = httpURLConnection.inputStream
            val inputStreamReader: InputStreamReader = InputStreamReader(inputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            var line: String? = bufferedReader.readLine()

            while (line != null){
                data.value += line
                data.value += "\n"
                // .readLine() automatically moves to the next line after calling
                // Do not call this method more than once per loop iteration
                line = bufferedReader.readLine()
            }

//            if (!data.value.isEmpty()){
//                val jsonObject: JSONObject = JSONObject(data)
//            }
            Log.i(
                "TAG()",
                "BlockUsersUI: Read data = \"$data\""
            )
        }
        catch (e: Exception){
            Log.e(
                "TAG()",
                "BlockUsersUI: Caught exception \"$e\" while trying to load URL"
            )
            Log.i(
                "TAG()",
                "BlockUsersUI: Data is currently = \"${data.value}\""
            )
        }
    }
}