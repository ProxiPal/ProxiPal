package com.mongodb.app.ui.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.blocking_censoring.FetchCensoredTextThread
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class BlockUsersUI : ComponentActivity(){
    // region Variables
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val censoringViewModel: CensoringViewModel by viewModels{
        CensoringViewModel.factory(
            repository = repository,
            this
        )
    }
    // endregion Variables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set this call in a button UI or something similar eventually
        FetchCensoredTextThread().start()
        setContent {
            BlockUsersLayout(
                censoringViewModel = censoringViewModel
            )
        }
    }
}


@Composable
fun BlockUsersLayout(
    censoringViewModel: CensoringViewModel,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Button(
            onClick = { censoringViewModel.readCensoredTextList() }
        ) {
        }
    }
}


// region Previews
@Composable
@Preview(showBackground = true)
fun BlockUsersUIPreview(){
    val mockRepository = MockRepository()
    val mockCensoringViewModel = CensoringViewModel(mockRepository)
    MyApplicationTheme {
        BlockUsersLayout(
            censoringViewModel = mockCensoringViewModel
        )
    }
}
// endregion Previews