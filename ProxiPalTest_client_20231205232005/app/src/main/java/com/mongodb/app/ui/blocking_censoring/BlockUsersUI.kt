package com.mongodb.app.ui.blocking_censoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.R
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CSVFileReader
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch


class BlockUsersUI : ComponentActivity() {
    // region Variables
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val blockingViewModel: BlockingViewModel by viewModels {
        BlockingViewModel.factory(
            repository = repository,
            this
        )
    }

    private val censoringViewModel: CensoringViewModel by viewModels {
        CensoringViewModel.factory(
            repository = repository,
            this
        )
    }
    // endregion Variables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockingViewModel.updateRepositories(repository)
        censoringViewModel.updateRepositories(repository)

        setContent {
            BlockUsersLayout(
                blockingViewModel = blockingViewModel,
                censoringViewModel = censoringViewModel
            )
        }
    }
}


@Composable
fun BlockUsersLayout(
    blockingViewModel: BlockingViewModel,
    censoringViewModel: CensoringViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ){
        BlockingContextualMenu(
            // TODO
            userId = "placeholder",
            blockingViewModel = blockingViewModel
        )
        CensoringTestButtons(
            censoringViewModel = censoringViewModel
        )
    }
}

@Composable
fun BlockingContextualMenu(
    userId: String,
    blockingViewModel: BlockingViewModel,
    modifier: Modifier = Modifier
) {
    var isContextualMenuOpen by rememberSaveable { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Open Contextual Menu",
            modifier = Modifier
                .clickable {
                    isContextualMenuOpen = true
                }
        )

        DropdownMenu(
            expanded = isContextualMenuOpen,
            onDismissRequest = {
                isContextualMenuOpen = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.blocking_contextual_menu_block_user)
                    )
                },
                onClick = {
                    isContextualMenuOpen = false
                    blockingViewModel.blockUserStart(
                        userIdToBlock = userId
                    )
                }
            )
        }
    }

    if (blockingViewModel.isBlockingUser.value) {
        BlockingAlert(
            // TODO
            userIdToBlock = "placeholder",
            userNameToBlock = "placeholder",
            onDismissRequest = { blockingViewModel.blockUserEnd() },
            onDismissButtonClick = { blockingViewModel.blockUserEnd() },
            onConfirmButtonClick = { blockingViewModel.blockUser() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingAlert(
    userIdToBlock: String,
    userNameToBlock: String,
    onDismissRequest: (() -> Unit),
    onDismissButtonClick: (() -> Unit),
    onConfirmButtonClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        dismissButton = {
            TextButton(
                onClick = { onDismissButtonClick() }
            ) {
                Text(
                    text = stringResource(id = R.string.blocking_alert_cancel)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmButtonClick() }
            ) {
                Text(
                    text = stringResource(id = R.string.blocking_alert_confirm)
                )
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.blocking_alert_title, userNameToBlock)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.blocking_alert_text)
            )
        },
        modifier = modifier
    )
}

@Composable
fun CensoringTestButtons(
    censoringViewModel: CensoringViewModel,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
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
        ) {
            Text(
                text = "Test string censoring"
            )
        }
    }
}


// region Previews
@Composable
@Preview(showBackground = true)
fun BlockUsersLayoutPreview() {
    val mockRepository = MockRepository()
    val mockBlockingViewModel = BlockingViewModel(mockRepository)
    val mockCensoringViewModel = CensoringViewModel(mockRepository)
    MyApplicationTheme {
        BlockUsersLayout(
            blockingViewModel = mockBlockingViewModel,
            censoringViewModel = mockCensoringViewModel
        )
    }
}

@Composable
@Preview(showBackground = true)
fun BlockingContextualMenuPreview() {
    val mockRepository = MockRepository()
    val mockBlockingViewModel = BlockingViewModel(mockRepository)
    MyApplicationTheme {
        BlockingContextualMenu(
            userId = stringResource(id = R.string.user_profile_test_string),
            blockingViewModel = mockBlockingViewModel
        )
    }
}

@Composable
@Preview(showBackground = true)
fun BlockingAlertPreview() {
    MyApplicationTheme {
        BlockingAlert(
            userIdToBlock = stringResource(id = R.string.user_profile_test_string),
            userNameToBlock = stringResource(id = R.string.user_profile_test_string),
            onDismissRequest = {},
            onDismissButtonClick = {},
            onConfirmButtonClick = {}
        )
    }
}
// endregion Previews