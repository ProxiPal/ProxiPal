package com.mongodb.app.ui.events

import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongodb.app.domain.Event

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EventDetailsScreen(sharedViewModel: SharedViewModel, navigateBack:()-> Unit) {
//    val event = sharedViewModel.event
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    if (event != null) {
//                        Text(text = event.name)
//                    }
//                },
//                navigationIcon = {
//                    // Add a back button to navigate back to the previous screen
//                    IconButton(onClick = { navigateBack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            if (event != null) {
//                Text(
//                    text = event.description,
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
//            // Add more details about the event
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String?,
    eventsViewModel: EventsViewModel,
    navigateBack: () -> Unit
) {
    var event by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(eventId) {
        eventId?.let {
            eventsViewModel.getEventById(it).collect { event = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Event Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            event?.let { eventData ->
                // Display the event details
                Text(
                    text = eventData.name,
                    modifier = Modifier.padding(16.dp)
                )
                // Display other event details...
            } ?: run {
                // Display a loading state or error message if the event data is not available
                Text(
                    text = "Loading event...",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


//@Composable
//@Preview
//fun EventDetailsScreenPreview(){
//    MyApplicationTheme {
//        EventDetailsScreen(sharedViewModel = SharedViewModel()){}
//    }
//}

