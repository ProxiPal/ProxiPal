package com.mongodb.app.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mongodb.app.domain.Event
import com.mongodb.app.ui.theme.MyApplicationTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String?,
    eventsViewModel: EventsViewModel,
    navigateBack: () -> Unit,
    navigateToEdit: () -> Unit
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(eventId) {
        eventId?.let {
            eventsViewModel.getEventById(it).collect { event = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Event Details", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    event?.let { eventData ->
                        if (eventsViewModel.isCurrentUserEventOwner(eventData)) {
                            IconButton(onClick = navigateToEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                    )
                }
            ) {
                Tab(
                    text = { Text("Details") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                Tab(
                    text = { Text("Announcement") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                Tab(
                    text = { Text("Attendees") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }

            event?.let { eventData ->
                when (selectedTab) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = eventData.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = "Location")
                                        Text(
                                            text = eventData.location,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Date")
                                        Text(
                                            text = "${eventData.date} at ${eventData.time}",
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = "Duration")
                                        Text(
                                            text = "Duration: ${eventData.duration}",
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = eventData.description,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        Button(
                            onClick = { /* Handle join event */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Join Event")
                        }
                    }
                    1 -> {
                        // Display announcement
                        Text(
                            text = "Announcement",
                            modifier = Modifier.padding(16.dp)
                        )
                        // Display announcement content...
                    }
                    2 -> {
                        // Display attendees
                        Text(
                            text = "Attendees",
                            modifier = Modifier.padding(16.dp)
                        )
                        // Display list of attendees...
                    }
                }
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
//        EventDetailsScreen(eventId = "test", eventsViewModel = , navigateBack = { /*TODO*/ }) {
//
//        }
//    }
//}

