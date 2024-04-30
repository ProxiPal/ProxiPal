package com.mongodb.app.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TextButton
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
import com.mongodb.app.location.UserProfileCard
import com.mongodb.app.ui.theme.MyApplicationTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String?,
    eventsViewModel: EventsViewModel,
    navigateBack: () -> Unit,
    navigateToEdit: (String) -> Unit
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
            CenterAlignedTopAppBar(
                title = { Text(text = "Event Details", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    event?.let { eventData ->
                        if (eventsViewModel.isCurrentUserEventOwner(eventData)) {
                            IconButton(onClick = { navigateToEdit(event!!._id.toString()) }) {
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
                event?.let {eventData->
                    if (eventsViewModel.isCurrentUserEventAttendee(eventData)){
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
                }
//                Tab(
//                    text = { Text("Announcement") },
//                    selected = selectedTab == 1,
//                    onClick = { selectedTab = 1 }
//                )
//                Tab(
//                    text = { Text("Attendees") },
//                    selected = selectedTab == 2,
//                    onClick = { selectedTab = 2 }
//                )
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
                        if(eventsViewModel.isCurrentUserEventAttendee(event!!)){


                        Button(
                            onClick = { eventsViewModel.leaveEvent(event!!._id.toString()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Leave Event")
                        }
                        } else{
                            Button(
                                onClick = { eventsViewModel.joinEvent(event!!._id.toString()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Join Event")
                            }
                        }

                    }
                    1 -> {
                        var showAnnouncementDialog by remember { mutableStateOf(false) }
                        var announcement by remember {mutableStateOf("")}
                        val announcementList = event!!.announcement
                        Column(modifier = Modifier.fillMaxSize()){
                        LazyColumn(
                            modifier = Modifier.weight(1f),
//                            contentPadding = PaddingValues(16.dp)
                        ){
                            items(announcementList) {    announcement ->
                                AnnouncementCard(announcement = announcement)

                            }

                        }
                            if (eventsViewModel.isCurrentUserEventOwner(event!!)){
                            Button(
                                onClick = {showAnnouncementDialog = true},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )   {
                                Text("New Announcement")
                                }
                            }
                        }

                        if (showAnnouncementDialog) {
                            newAnnouncementDialog(
                                onCancel = {
                                    showAnnouncementDialog = false
                                },
                                onConfirmation = {newAnnouncement ->
                                    announcement = newAnnouncement
                                    eventsViewModel.addAnnouncement(eventId, announcement)
                                    showAnnouncementDialog = false

                                }

                            )
                        }
                        // Display announcement

                        // Display announcement content...
                    }
                    2 -> {
                        val attendeesList = eventsViewModel.getEventAttendeesList(eventId)
                        // Display attendees
                        Text(
                            text = "Attendees",
                            modifier = Modifier.padding(16.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(attendeesList) { userProfile ->
                                UserProfileCard(userProfile, onItemClick = { /*TODO*/ })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
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
@Composable
fun AnnouncementCard(
    announcement: String,
){
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = announcement,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun newAnnouncementDialog(
    onCancel:() -> Unit,
    onConfirmation:(String) -> Unit
){
    var announcement by remember{ mutableStateOf("")}
    AlertDialog(
        onDismissRequest = onCancel,
        title = {Text(text = "Enter Announcement")},
        text = {
            Column(
                modifier = Modifier.padding(horizontal =20.dp)
            ) {
                OutlinedTextField(value = announcement,
                    onValueChange = {announcement = it},
                    modifier  = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    label = {Text("Announcement")},
                    maxLines = 5)
            }
        },
        confirmButton = {
            Button(
                onClick = {onConfirmation(announcement)})
            {
                Text(text = "Confirm")
            }
                },
        dismissButton = {
            Button(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
        }
            )
}





//@Composable
//fun LocationInputDialog(
//    onSaveLocation: (String) -> Unit,
//    onCancel: () -> Unit
//) {
//    var address1 by remember { mutableStateOf("") }
//    var address2 by remember { mutableStateOf("") }
//    var city by remember { mutableStateOf("") }
//    var country by remember { mutableStateOf("") }
//    var zip by remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = onCancel,
//        title = {
//            Text(text = "Enter Location")
//        },
//        text = {
//            Column {
//                OutlinedTextField(
//                    value = address1,
//                    onValueChange = { address1 = it },
//                    label = { Text("Address Line 1") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                OutlinedTextField(
//                    value = address2,
//                    onValueChange = { address2 = it },
//                    label = { Text("Address Line 2") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                OutlinedTextField(
//                    value = city,
//                    onValueChange = { city = it },
//                    label = { Text("City") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                OutlinedTextField(
//                    value = country,
//                    onValueChange = { country = it },
//                    label = { Text("Country") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                OutlinedTextField(
//                    value = zip,
//                    onValueChange = { zip = it },
//                    label = { Text("ZIP Code") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    val fullAddress = "$address1, $address2, $city, $country, $zip"
//                    onSaveLocation(fullAddress)
//                }
//            ) {
//                Text(text = "Save")
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onCancel
//            ) {
//                Text(text = "Cancel")
//            }
//        }
//    )
//}


//@Composable
//@Preview
//fun EventDetailsScreenPreview(){
//    MyApplicationTheme {
//        EventDetailsScreen(eventId = "test", eventsViewModel = , navigateBack = { /*TODO*/ }) {
//
//        }
//    }
//}

