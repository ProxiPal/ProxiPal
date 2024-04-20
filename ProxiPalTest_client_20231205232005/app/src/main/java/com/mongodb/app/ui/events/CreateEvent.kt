package com.mongodb.app.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.ui.theme.MyApplicationTheme
import java.util.Calendar
import java.util.Date

@Composable
fun CreateEventBody() {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Event", fontSize = 30.sp)
        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text(text = "Event Name") },
            modifier=Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            label = { Text(text = "Event Description") },
            modifier=Modifier.fillMaxWidth()
        )
        Text(text = "Location")
        Text(text = "Date & Time")
        Row(verticalAlignment = Alignment.CenterVertically) {
            showDatePicker(context)
        }
        showTimePicker(context = context)
        TextButton(onClick = { /*TODO*/ }) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun showDatePicker(context: Context) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()
    var date by remember { mutableStateOf("") }

    if (date.isEmpty()) {
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
            text = "Date",
            fontSize = 24.sp,
        )
        TextButton(
            onClick = {
                datePickerDialog.show()
            },
        ) {
            Text(
                text = date,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun showTimePicker(context: Context) {
    val calendar = Calendar.getInstance()
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    var time by remember { mutableStateOf("") }

    if (time.isEmpty()) {
        time = "%02d:%02d".format(hour, minute)
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour: Int, selectedMinute: Int ->
            time = "%02d:%02d".format(selectedHour, selectedMinute)
        },
        hour,
        minute,
        false
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = "Time",
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
}

@Composable
@Preview(showBackground = true)
fun CreateEventPreview() {
    MyApplicationTheme {
        CreateEventBody()
    }
}