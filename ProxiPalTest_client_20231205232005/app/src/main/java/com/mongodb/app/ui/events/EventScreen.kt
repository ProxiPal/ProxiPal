package com.mongodb.app.ui.events

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
import org.mongodb.kbson.ObjectId
import org.mongodb.kbson.serialization.Bson


@Composable
fun EventScreen(eventsViewModel: EventsViewModel, navigateToEvent: (String) -> Unit, navigateToCreateEvent: () -> Unit) {
    val myEventList by eventsViewModel.myEventList.collectAsState()
    val otherEventList by eventsViewModel.otherEventList.collectAsState()
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
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                Text(
                    text = "My Events",
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(myEventList) { event ->
                EventCard(event) {
                    Log.d("eventstring", event._id.toString())
                    val validIdString = event._id.toString().removePrefix("BsonObjectId(").removeSuffix(")")
                    Log.d("eventvalidString", validIdString)
                    navigateToEvent(event._id.toString())
                }
            }
            item {
                Text(
                    text = "Other Events",
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(otherEventList) { event ->
                EventCard(event) {
                    navigateToEvent(event._id.toString())
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
                text = event.name,
            )
            Row {
                Text(
                    text = event.date,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = event.time,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                text = event.location,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = event.description,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

//@Composable
//@Preview
//fun EventScreenPreview(){
//    MyApplicationTheme {
//        EventScreen( eventsViewModel = EventsViewModel(),sharedViewModel =  SharedViewModel(), navigateToEvent = {}, navigateToCreateEvent={})
//    }
//}
