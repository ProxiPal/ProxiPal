package com.mongodb.app.ui.login

import android.annotation.SuppressLint
import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.mongodb.app.navigation.NavigationDestination
import com.mongodb.app.presentation.login.LoginViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme

// navigation details
//object RegisterScreen : NavigationDestination {
//    override val route = "register_scaffold"
//    override val title = "Create Account"
//}

// builds the registration scaffold with topbar and registermain
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun RegisterScaffold(loginViewModel: LoginViewModel, toggleRegistrationScreen: () -> Unit) {
    Scaffold(
        topBar = { RegisterTopBar(toggleRegistrationScreen = toggleRegistrationScreen) },
        content = { RegisterMain(loginViewModel) },
        )
}

// main contain of register scaffold, takes user's email and password to create an account
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMain(loginViewModel: LoginViewModel){
    var isRegistrationButtonEnabled by remember {mutableStateOf(true)}
    Column(
        modifier = Modifier
            .padding(top = 60.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "ProxiPal",
            fontSize = 70.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(500.dp),
            color = Color.Black
        )
        TextField(value = loginViewModel.state.value.email, onValueChange = { loginViewModel.setEmail(it) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        TextField(value = loginViewModel.state.value.password, onValueChange = { loginViewModel.setPassword(it) }, label = { Text("Password") },visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        // when clicked, disables register button
        Button(
            onClick = {
                loginViewModel.createAccount(loginViewModel.state.value.email, loginViewModel.state.value.password);
                isRegistrationButtonEnabled = false},
            enabled = isRegistrationButtonEnabled,

            colors = ButtonDefaults.buttonColors(Color(0xFFEF8524)),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create Account")
        }
        // enables login button and prompts a message to confirm account creation through email
        if (!isRegistrationButtonEnabled){
            Text(
                text = "An email has been sent for account confirmation. Click the link finish account creation.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {loginViewModel.login(loginViewModel.state.value.email,loginViewModel.state.value.password, fromCreation = true)},
                colors = ButtonDefaults.buttonColors(Color(0xFFEF8524)),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.fillMaxWidth(),
            ){
                Text(text = "Login")
            }
        }
    }
}

// top bar for register to navigate back to login screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTopBar(toggleRegistrationScreen:  ()->Unit){
    TopAppBar(
        title = {Text(text = "Create Account", color = Color.White)},
        navigationIcon = {
            IconButton(onClick = toggleRegistrationScreen) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription ="back", tint = Color.White )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor =Color(0xFFEF8524) )
    )
}

// preview of registerscaffold
//@Composable
//@Preview
//fun registerpreview(){
//    MyApplicationTheme {
//        RegisterScaffold(loginViewModel = LoginViewModel(), navigateBack = {})
//
//
//    }
//}