package com.mongodb.app.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.mongodb.app.R
import com.mongodb.app.ui.theme.MyApplicationTheme
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun CreateEventBody(navigateBack: () -> Unit, eventsViewModel: EventsViewModel, onConfirm:() -> Unit) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showLocationDialog by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }

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
            Text(text = stringResource(id = R.string.create_event), fontSize = 30.sp)
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text(text = stringResource(id = R.string.event_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = eventDescription,
                onValueChange = { eventDescription = it },
                label = { Text(text = stringResource(id = R.string.event_description)) },
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
                    text = stringResource(id = R.string.location),
                    fontSize = 18.sp,
                )
                TextButton(
                    onClick = {
                        showLocationDialog = true
                    },
                ) {
                    Text(
                        text = if (location.isBlank()) stringResource(id = R.string.select_location) else location,
                        fontSize = 18.sp
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                date = showDatePicker(context)
                time = showTimePicker(context)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.duration),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = durationHours,
                    onValueChange = { durationHours = it },
//                    label = { Text("Hours") },
                    modifier = Modifier.width(60.dp).height(50.dp)
                )
                Text(
                    text = stringResource(id = R.string.hour),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
//                    label = { Text("Minutes") },
                    modifier = Modifier.width(60.dp).height(50.dp)
                )
                Text(
                    text = stringResource(id = R.string.minute),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            TextButton(onClick = navigateBack) {
                Text(text = stringResource(id = R.string.cancel))
            }
            val duration = "$durationHours h $durationMinutes m"
            TextButton(onClick = { eventsViewModel.addEvent(eventName, eventDescription, date, time, duration , location); onConfirm()}) {
                Text(text = stringResource(id = R.string.confirm))
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

@Composable
fun LocationInputDialog(
    onSaveLocation: (String) -> Unit,
    onCancel: () -> Unit
) {
    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(id = R.string.enter_location))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = address1,
                    onValueChange = { address1 = it },
                    label = { Text(text = stringResource(id = R.string.address1)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address2,
                    onValueChange = { address2 = it },
                    label = { Text(text = stringResource(id = R.string.address2)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(text = stringResource(id = R.string.city)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text(text = stringResource(id = R.string.state)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = zip,
                    onValueChange = { zip = it },
                    label = { Text(text = stringResource(id = R.string.zip)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fullAddress = "$address1, $address2, $city, $state, $zip"
                    onSaveLocation(fullAddress)
                }
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}


@Composable
fun showDatePicker(context: Context, initialDate: String=""): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()
    var date by remember { mutableStateOf("") }

    if (initialDate.isEmpty()) {
        date = "${month + 1}/$day/$year"
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            date = "${month + 1}/$dayOfMonth/$year"
        },
        year,
        month,
        day
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.Date),
            fontSize = 18.sp,
        )
        TextButton(
            onClick = {
                datePickerDialog.show()
            },
        ) {
            Text(
                text = date,
                fontSize = 18.sp
            )
        }
    }
    return date
}
@Composable
fun showTimePicker(context: Context, initialTime:String = ""): String {
    val calendar = Calendar.getInstance()
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    var time by remember{ mutableStateOf(initialTime)}
    var amPm by remember { mutableStateOf("") }


//    if (time.isEmpty()) {
//        time = "%02d:%02d".format(hour, minute)
//    }
    if (time.isEmpty()) {
        val hr = if (hour > 12) hour - 12 else hour
        val min = if (minute < 10) "0$minute" else minute.toString()
        amPm = if (hour >= 12) "PM" else "AM"
        time = "$hr:$min $amPm"
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour: Int, selectedMinute: Int ->
            //time = "%02d:%02d".format(selectedHour, selectedMinute)
            val hr = if (selectedHour > 12) selectedHour - 12 else selectedHour
            val min = if (selectedMinute < 10) "0$selectedMinute" else selectedMinute.toString()
            amPm = if (selectedHour >= 12) "PM" else "AM"
            time = "$hr:$min $amPm"
        },
        hour,
        minute,
        false
    )



    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = stringResource(id = R.string.time),
            fontSize = 18.sp,
        )
        TextButton(
            onClick = {
                timePickerDialog.show()
            },
        ) {
            Text(
                text = time,
                fontSize = 18.sp
            )
        }
    }
    return time
}



@Composable
@Preview
fun CreateEventPreview() {
    MyApplicationTheme {
        //CreateEventBody(navigateBack = {}, eventsViewModel = EventsViewModel())
    }
}