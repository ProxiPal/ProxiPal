package com.mongodb.app.ui.events

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.domain.Event

@Composable
fun EditEventBody(
    navigateBack: () -> Unit,
    eventsViewModel: EventsViewModel,
    eventId: String?
) {

    var event by remember { mutableStateOf<Event?>(null) }
    LaunchedEffect(eventId) {
        eventId?.let {
            eventsViewModel.getEventById(it).collect { event = it }
        }
    }

    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showLocationDialog by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var initialTime by remember { mutableStateOf("") }
    var initialDate by remember { mutableStateOf("") }

    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }

    LaunchedEffect(event) {
        event?.let {
            eventName = it.name
            eventDescription = it.description
            location = it.location
            initialTime = it.time
            initialDate = it.date
            val durationParts = it.duration.split(" ")
            durationHours = durationParts[0].replace("h", "")
            durationMinutes = durationParts[2].replace("m", "")
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Edit Event", fontSize = 30.sp)
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text(text = "Event Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = eventDescription,
                onValueChange = { eventDescription = it },
                label = { Text(text = "Event Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                maxLines = 5,
                singleLine = false
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Location:",
                    fontSize = 18.sp,
                )
                TextButton(
                    onClick = {
                        showLocationDialog = true
                    },
                ) {
                    Text(
                        text = location,
                        fontSize = 18.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                date = showDatePicker(context, initialDate = initialDate)
                time = showTimePicker(context, initialTime = initialTime)
                Log.d("initialTime", initialTime)
                Log.d("initialDate", initialDate)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Duration:",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = durationHours,
                    onValueChange = { durationHours = it },
                    modifier = Modifier.width(60.dp).height(50.dp)
                )
                Text(
                    text = "h",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    modifier = Modifier.width(60.dp).height(50.dp)
                )
                Text(
                    text = "m",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            TextButton(onClick = navigateBack) {
                Text(text = "Cancel")
            }
            val duration = "$durationHours h $durationMinutes m"
            TextButton(
                onClick = {
//                    eventsViewModel.updateEvent(
//                        eventToEdit.id,
//                        eventName,
//                        eventDescription,
//                        date,
//                        time,
//                        duration,
//                        location
//                    )
                }
            ) {
                Text(text = "Save")
            }
        }
    }

    if (showLocationDialog) {
        LocationInputDialog(
            onSaveLocation = { address ->
                location = address
                showLocationDialog = false
            },
            onCancel = {
                showLocationDialog = false
            }
        )
    }
}