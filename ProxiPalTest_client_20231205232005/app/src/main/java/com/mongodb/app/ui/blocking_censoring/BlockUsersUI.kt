package com.mongodb.app.ui.blocking_censoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.presentation.blocking_censoring.CSVFileReader
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.blocking_censoring.censor
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch


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
            Text(
                text = "Load censored text list"
            )
        }
        Button(
            onClick = { censoringViewModel.testTextCensoring() }
        ){
            Text(
                text = "Test string censoring"
            )
        }
        Button(
            onClick = { CSVFileReader().readCsvFile() }
        ){
            Text(
                text = "Test .csv reading"
            )
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