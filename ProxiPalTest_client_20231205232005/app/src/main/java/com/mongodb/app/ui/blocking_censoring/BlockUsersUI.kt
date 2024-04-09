package com.mongodb.app.ui.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.ui.theme.MyApplicationTheme
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class BlockUsersUI : ComponentActivity(){
    // region Variables
    private val data = mutableStateOf("")
    // endregion Variables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set this call in a button UI or something similar eventually
        FetchDataThread().start()
        setContent {
            BlockUsersLayout(
                data = FetchDataThread().data.value
            )
        }
    }


    class FetchDataThread : Thread(){
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
}


@Composable
fun BlockUsersLayout(
    data: String,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(text = data)
    }
}


// region Previews
@Composable
@Preview(showBackground = true)
fun BlockUsersUIPreview(){
    BlockUsersUI.FetchDataThread().start()
    MyApplicationTheme {
        BlockUsersLayout(
//            data = stringResource(id = R.string.user_profile_test_string)
            data = BlockUsersUI.FetchDataThread().data.value
        )
    }
}
// endregion Previews