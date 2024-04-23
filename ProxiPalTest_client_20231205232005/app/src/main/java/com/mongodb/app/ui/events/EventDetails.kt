package com.mongodb.app.ui.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mongodb.app.domain.Event
import com.mongodb.app.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(sharedViewModel: SharedViewModel, navigateBack:()-> Unit) {
    val event = sharedViewModel.event
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (event != null) {
                        Text(text = event.name)
                    }
                },
                navigationIcon = {
                    // Add a back button to navigate back to the previous screen
                    IconButton(onClick = { navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (event != null) {
                Text(
                    text = event.description,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // Add more details about the event
        }
    }
}

val test = Event().apply {
    name = "Birthday Party"
    description = "Join us for a fun-filled birthday celebration!"
    date = "05-06-2024"
}

@Composable
@Preview
fun EventDetailsScreenPreview(){
    MyApplicationTheme {
        EventDetailsScreen(sharedViewModel = SharedViewModel()){}
    }
}

