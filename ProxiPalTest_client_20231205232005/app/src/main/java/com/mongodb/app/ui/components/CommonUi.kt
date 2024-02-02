package com.mongodb.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mongodb.app.R
import com.mongodb.app.presentation.tasks.ContextualMenuViewModel

/**
 * Contains Composable functions to be used multiple times throughout the app (to help reduce redundancy)
 */

/**
 * The [Composable] function for providing a contextual menu for deleting items in the in-app list and the database (eventually)
 */
@Composable
fun ContextualMenu(
    contextualMenuViewModel: ContextualMenuViewModel,
    onClickContextualMenuViewModelOpen: () -> Unit,
    onClickContextualMenuViewModelClose: () -> Unit,
    onClickContextualMenuViewModelDeleteEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClickContextualMenuViewModelOpen
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Open Contextual Menu"
            )
        }

        DropdownMenu(
            expanded = contextualMenuViewModel.visible.value,
            onDismissRequest = onClickContextualMenuViewModelClose
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.delete)) },
                onClick = onClickContextualMenuViewModelDeleteEntry
            )
        }
    }
}