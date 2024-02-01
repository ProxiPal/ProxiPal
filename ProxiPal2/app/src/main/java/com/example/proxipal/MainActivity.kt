package com.example.proxipal

import android.os.Bundle
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement


import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authentication3.ui.theme.ProxiPalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProxiPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var userName by remember { mutableStateOf("")}
    var passWord by remember {mutableStateOf("")}
    val checkedState = remember { mutableStateOf(true)}
    Column(modifier = modifier.padding(60.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text( text = "ProxiPal", fontSize = 40.sp, textAlign = TextAlign.Center, modifier = Modifier.width(500.dp))
        Text( text = "Login to access your bookmarks and personal preferences.", fontSize = 15.sp, color = Color.Gray)
        TextField(value = userName, onValueChange = {/*todo*/}, label = {Text("username")} )
        TextField(value = passWord, onValueChange= { /*todo*/ }, label = {Text("password")})
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, ){
            Checkbox(checked = checkedState.value, onCheckedChange = {/*todo*/})
            Text( text = "Keep me logged in")
        }
        Button(onClick = { /*TODO*/ }, colors = buttonColors(Color(0xFFEF8524)), shape = RoundedCornerShape(5.dp)) { Text("Login") }
    }
    Column(modifier= Modifier
        .fillMaxSize()
        .padding(60.dp), verticalArrangement = Arrangement.Bottom ){
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically ){
            Text(text= "No account yet?")
            TextButton(onClick = { /*TODO*/ }, colors = ButtonDefaults.textButtonColors( contentColor = Color.Gray)) { Text("Register here")

            }
        }}

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProxiPalTheme {
        Greeting("Android")
    }
}