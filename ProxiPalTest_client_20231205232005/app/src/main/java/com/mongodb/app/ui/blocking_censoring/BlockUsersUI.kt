package com.mongodb.app.ui.blocking_censoring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.R
import com.mongodb.app.data.blocking_censoring.MockBlockingCensoringData
import com.mongodb.app.presentation.blocking_censoring.BlockingAction
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch


@Deprecated(
    message = "Not currently in use; Use BlockingContextualMenu function instead"
)
@Composable
fun BlockUsersLayout(
    blockingViewModel: BlockingViewModel,
    censoringViewModel: CensoringViewModel,
    /* The other user's ID involved in the current conversation */
    userIdInFocus: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ){
        BlockingContextualMenu(
            userId = userIdInFocus,
            blockingViewModel = blockingViewModel,
            censoringViewModel = censoringViewModel
        )
        CensoringTestButtons(
            censoringViewModel = censoringViewModel
        )
    }
}

/**
 * The main [Composable] function for setting up the block/unblock user contextual menu
 */
@Composable
fun BlockingContextualMenu(
    userId: String,
    blockingViewModel: BlockingViewModel,
    censoringViewModel: CensoringViewModel,
    modifier: Modifier = Modifier
) {
    var isContextualMenuOpen by rememberSaveable { mutableStateOf(false) }
    val isUserBlocked = blockingViewModel.isUserBlocked(userId)

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
                        text =
                        if (isUserBlocked) stringResource(id = R.string.blocking_contextual_menu_unblock_user)
                        else stringResource(id = R.string.blocking_contextual_menu_block_user)
                    )
                },
                onClick = {
                    isContextualMenuOpen = false
                    if (isUserBlocked){
                        blockingViewModel.unblockUserStart(
                            userIdToUnblock = userId
                        )
                    }
                    else{
                        blockingViewModel.blockUserStart(
                            userIdToBlock = userId
                        )
                    }
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text =
                        if (!censoringViewModel.isCensoringText.value) stringResource(id = R.string.censoring_enable_text_censoring)
                        else stringResource(id = R.string.censoring_disable_text_censoring)
                    )
                },
                onClick = {
                    isContextualMenuOpen = false
                    censoringViewModel.toggleShouldCensorText()
                },
                modifier = modifier
            )
        }
    }

    // Start unblocking the user
    if (blockingViewModel.blockingAction.value == BlockingAction.UNBLOCKING) {
        BlockingAlert(
            userIdToBlock = userId,
            isBlocking = false,
            onDismissRequest = { blockingViewModel.blockUnblockUserEnd() },
            onDismissButtonClick = { blockingViewModel.blockUnblockUserEnd() },
            onConfirmButtonClick = { blockingViewModel.unblockUser() },
            blockingViewModel = blockingViewModel
        )
    }
    // Start blocking the user
    else if (blockingViewModel.blockingAction.value == BlockingAction.BLOCKING){
        BlockingAlert(
            userIdToBlock = userId,
            isBlocking = true,
            onDismissRequest = { blockingViewModel.blockUnblockUserEnd() },
            onDismissButtonClick = { blockingViewModel.blockUnblockUserEnd() },
            onConfirmButtonClick = { blockingViewModel.blockUser() },
            blockingViewModel = blockingViewModel
        )
    }
}

/**
 * Shows an alert dialog when the user is about to (un)block another user
 */
@Composable
fun BlockingAlert(
    userIdToBlock: String,
    isBlocking: Boolean,
    onDismissRequest: (() -> Unit),
    onDismissButtonClick: (() -> Unit),
    onConfirmButtonClick: (() -> Unit),
    blockingViewModel: BlockingViewModel,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarText = if (isBlocking) stringResource(id = R.string.blocking_snackbar_block_text)
    else stringResource(id = R.string.blocking_snackbar_unblock_text)
    val isUserBlocked = blockingViewModel.isUserBlocked(userIdToBlock)
    val userNameToBlock = blockingViewModel.focusedUserName.value

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
                onClick = {
                    onConfirmButtonClick()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            snackbarText
                        )
                    }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.blocking_alert_confirm)
                )
            }
        },
        title = {
            Text(
                text =
                if (isUserBlocked)
                    stringResource(id = R.string.blocking_alert_unblock_title, userNameToBlock)
                else
                    stringResource(id = R.string.blocking_alert_block_title, userNameToBlock)
            )
        },
        text = {
            Text(
                text =
                if (isUserBlocked)
                    stringResource(id = R.string.blocking_alert_unblock_text)
                else
                    stringResource(id = R.string.blocking_alert_block_text)
            )
        },
        modifier = modifier
    )
}

/**
 * Shows a temporary switch to toggle between censoring and showing all texts
 */
@Deprecated(
    message = "May use eventually, but for now the action to censor text is in a contextual menu in " +
            "the top app bar of the messages screen"
)
@Composable
fun SwitchToggleTextCensoring(
    switchToggleState: Boolean,
    onSwitchToggle: (() -> Unit),
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
    ){
        Switch(
            checked = switchToggleState,
            onCheckedChange = {
                onSwitchToggle()
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = Purple200
            )
        )
    }
}

/**
 * Shows buttons to quickly test loading list of text from a URL and censoring test strings
 */
@Deprecated(
    message = "This is for testing purposes only; Do not use this in the final product"
)
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
                text = "Read censored text list"
            )
        }
        Button(
            onClick = { censoringViewModel.testTextCensoring() }
        ) {
            Text(
                text = "Test text censoring"
            )
        }
    }
}


// region Previews
@Composable
@Preview(showBackground = true)
fun BlockUsersLayoutPreview() {
    MyApplicationTheme {
        BlockUsersLayout(
            blockingViewModel = MockBlockingCensoringData.mockBlockingViewModel,
            censoringViewModel = MockBlockingCensoringData.mockCensoringViewModel,
            userIdInFocus = "placeholder"
        )
    }
}

@Composable
@Preview(showBackground = true)
fun BlockingContextualMenuPreview() {
    MyApplicationTheme {
        BlockingContextualMenu(
            userId = stringResource(id = R.string.user_profile_test_string),
            blockingViewModel = MockBlockingCensoringData.mockBlockingViewModel,
            censoringViewModel = MockBlockingCensoringData.mockCensoringViewModel
        )
    }
}

@Composable
@Preview(showBackground = true)
fun BlockingAlertPreview() {
    MyApplicationTheme {
        BlockingAlert(
            userIdToBlock = stringResource(id = R.string.user_profile_test_string),
            isBlocking = true,
            onDismissRequest = {},
            onDismissButtonClick = {},
            onConfirmButtonClick = {},
            blockingViewModel = MockBlockingCensoringData.mockBlockingViewModel
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SwitchToggleTextCensoringPreview(){
    MyApplicationTheme {
        SwitchToggleTextCensoring(
            switchToggleState = false,
            onSwitchToggle = { /*TODO*/ }
        )
    }
}
// endregion Previews