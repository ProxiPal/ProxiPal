package com.mongodb.app.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.ui.login.RegisterMain
import com.mongodb.app.ui.login.RegisterTopBar
import com.mongodb.app.ui.theme.MyApplicationTheme

@Composable
fun CreateEventBody(){
    var eventName by remember { mutableStateOf("")}
    var eventDescription by remember {mutableStateOf("") }
    Column(
        modifier = Modifier
            .padding(top = 60.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Create Event", fontSize = 30.sp)
        OutlinedTextField(value = eventName,
            onValueChange = {eventName= it},
            label = {Text(text = "Event Name")})
        OutlinedTextField(value = eventDescription,
            onValueChange = {eventDescription= it},
            label = {Text(text = "Event Description")})
        Text(text = "Location")
        Text(text = "Date & Time")

        Row(
            verticalAlignment = Alignment.CenterVertically){
            TextButton(onClick = { /*TODO*/ }) { Text(text = "Create Event")

        }
            TextButton(onClick = { /*TODO*/ }) {Text(text = "Cancel")
                
            }
        }

    }

}
@Composable
@Preview(showBackground = true)
fun CreateEventPreview(){
    MyApplicationTheme {
        CreateEventBody()
    }
}
