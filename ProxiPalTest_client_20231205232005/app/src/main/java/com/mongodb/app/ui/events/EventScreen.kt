package com.mongodb.app.ui.events

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHost


import androidx.navigation.NavType

import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mongodb.app.domain.Event
import com.mongodb.app.navigation.Routes
import com.mongodb.app.ui.theme.MyApplicationTheme


val myEvents = listOf(
    Event().apply {
        title = "Birthday Party"
        description = "Join us for a fun-filled birthday celebration!"
        date = "05-06-2024"
    }
)
//sample other events
val otherEvents = listOf(
    Event().apply {
        title = "Conference"
        description = "Attend the annual technology conference."
        date = "07-12-2024"
    },
    Event().apply {
        title = "Workshop"
        description = "Learn new skills in our hands-on workshop."
        date = "08-30-2024"
    },
    Event().apply {
        title = "Conference1"
        description = "Attend the annual technology conference."
        date = "07-12-2024"
    },
    Event().apply {
        title = "Workshop1"
        description = "Learn new skills in our hands-on workshop."
        date = "08-30-2024"
    },
    Event().apply {
        title = "Conference2"
        description = "Attend the annual technology conference."
        date = "07-12-2024"
    },
    Event().apply {
        title = "Workshop2"
        description = "Learn new skills in our hands-on workshop."
        date = "08-30-2024"
    }
)

@Composable
fun EventScreen(sharedViewModel: SharedViewModel, navigateToEvent:()-> Unit, navigateToCreateEvent:()-> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Events") },
                actions = {
                    IconButton(
                        onClick = navigateToCreateEvent
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Event"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = "My Events",
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(myEvents) { event ->
                    EventCard(event) {
                        sharedViewModel.addEvent(event)
                        navigateToEvent()
                        // Handle click event for "My Events"
                        // Example: Navigate to event details screen
                        // navController.navigate("event_details/${event.id}")
                    }
                }
            }

            Text(
                text = "Other Events",
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(otherEvents) { event ->
                    EventCard(event) {
                        sharedViewModel.addEvent(event)
                        navigateToEvent()
                        // Handle click event for "Other Events"
                        // Example: Navigate to event details screen
                        // navController.navigate("event_details/${event.id}")
                    }
                }
            }
        }
    }
}

//event card for each event
@Composable
fun EventCard(event: Event, onClick:()->Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.title,
            )
            Text(
                text = event.date,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = event.description,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
@Preview
fun EventScreenPreview(){
    MyApplicationTheme {
        EventScreen( sharedViewModel =  SharedViewModel(), navigateToEvent = {}, navigateToCreateEvent={})
    }
}
