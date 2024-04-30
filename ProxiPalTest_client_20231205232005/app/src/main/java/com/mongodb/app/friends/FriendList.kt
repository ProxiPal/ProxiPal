package com.mongodb.app.friends

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Friendslist() {
    val showMenu = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf(TextFieldValue()) }

    Column {
        SmallTopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = { showMenu.value = !showMenu.value }) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color.Black
                    )
                }
            },
            actions = {
                TextField(
                    value = searchText.value,
                    onValueChange = { newText ->
                        if (newText.text.length <= 30) {
                            searchText.value = newText
                        }
                    },
                    singleLine = true,
                    placeholder = { Text("Search in Messages") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, // Hide the indicator line when focused
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .fillMaxWidth()
                        .padding(start = 30.dp)
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        // Display the popup menu
        if (showMenu.value) {
            Popup(
                onDismissRequest = { showMenu.value = false },
                properties = PopupProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                // Use a Surface for the menu background and shadow
                Surface(
                    modifier = Modifier.width(intrinsicSize = IntrinsicSize.Min), // Set the width as needed
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .width(150.dp) // Adjust the width here to make options narrower
                    ) {
                        DropdownMenuItem(
                            text = { Text("Menu Item 1") },
                            onClick = {
                                // Handle action for Menu Item 1
                                showMenu.value = false
                            },
                            modifier = Modifier.fillMaxWidth() // Ensure item fills the constrained width
                        )
                        DropdownMenuItem(
                            text = { Text("Menu Item 2") },
                            onClick = {
                                // Handle action for Menu Item 2
                                showMenu.value = false
                            },
                            modifier = Modifier.fillMaxWidth() // Ensure item fills the constrained width
                        )
                        DropdownMenuItem(
                            text = { Text("Menu Item 3") },
                            onClick = {
                                // Handle action for Menu Item 3
                                showMenu.value = false
                            },
                            modifier = Modifier.fillMaxWidth() // Ensure item fills the constrained width
                        )
                    }
                }
            }
        }
    }
}