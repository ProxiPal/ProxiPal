package com.mongodb.app.ui.blocking_censoring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.R
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200


/**
 * Provides the option to enable censoring text
 */
@Composable
fun CensoringContextualMenu(
    censoringViewModel: CensoringViewModel,
    modifier: Modifier = Modifier
){
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


// region Previews
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