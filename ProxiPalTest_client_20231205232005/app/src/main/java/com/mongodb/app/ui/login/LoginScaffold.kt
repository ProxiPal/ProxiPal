package com.mongodb.app.ui.login

//import com.mongodb.app.navigation.NavigationDestination
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.R
import com.mongodb.app.presentation.login.LoginViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme

import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.runBlocking

import io.realm.kotlin.mongodb.User
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


import org.mongodb.kbson.ObjectId

// navigation details
//object HomeDestination : NavigationDestination {
//    override val route = "home"
//    override val title = "ProxiPal"
//}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun AccountScaffold(loginViewModel: LoginViewModel) {
    var isRegistrationScreen by remember { mutableStateOf(false) }

    if (!isRegistrationScreen) {
        LoginScaffold(loginViewModel = loginViewModel) {
            isRegistrationScreen = true
        }
    } else {
        RegisterScaffold(loginViewModel=loginViewModel){
            isRegistrationScreen = false
        }
    }
}
// builds login scaffold with the bottom bar and login main


// Main content of login scaffold, takes user's email and password input and logs them in if they have an account
@SuppressLint("AuthLeak")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginMain(loginViewModel: LoginViewModel){
    var msg by remember {mutableStateOf(false)}
    var passwordVisibility by remember{ mutableStateOf(false)}
    Column(
        modifier = Modifier
            .padding(top = 60.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 60.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(500.dp)
        )
        Text(
            text = stringResource(id = R.string.login_subheading),
            fontSize = 20.sp,
            color = Color.Gray
        )
        OutlinedTextField(value = loginViewModel.state.value.email,
            onValueChange = { loginViewModel.setEmail(it) },
            label = { Text(stringResource(id = R.string.prompt_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = loginViewModel.state.value.password,
            onValueChange = { loginViewModel.setPassword(it) },
            label = { Text(stringResource(id = R.string.prompt_password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisibility)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisibility) "Hide password" else "Show password"

                // Toggle button to hide or display password
                IconButton(onClick = {passwordVisibility = !passwordVisibility}){
                    Icon(imageVector  = image, description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { loginViewModel.login(loginViewModel.state.value.email, loginViewModel.state.value.password) },
            colors = ButtonDefaults.buttonColors(Color(0xFFEF8524)),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Login")
        }
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(text = stringResource(id = R.string.prompt_email_reset_password))
            TextButton(onClick = {
                val app: App = App.create("proxipaltest-jyqla")
                runBlocking { try{app.emailPasswordAuth.sendResetPasswordEmail(loginViewModel.state.value.email)
                    msg = true
                }
                catch(e:Exception){
                    Log.e("ResetPasswordError", "failed to send reset password email:${e.message}")
                }}
            }) {
                Text(stringResource(id = R.string.reset_password))
            }

        }
        if (msg){
        Text(stringResource(id = R.string.password_reset_email))
    }
    }
}

// Bottom bar for login scaffold, directs user to register page
@Composable
fun LoginBottomBar(toggleRegistrationScreen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(text = stringResource(id = R.string.does_not_have_account))
            TextButton(
                onClick = { toggleRegistrationScreen() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            )
            {
                Text(stringResource(id = R.string.register_here))
            }

        }
    }
}
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    fun LoginScaffold(loginViewModel: LoginViewModel, toggleRegistrationScreen: () -> Unit) {
        Scaffold(
            bottomBar = { LoginBottomBar(toggleRegistrationScreen) },
            content = {
                LoginMain(loginViewModel = loginViewModel)
            }
        )
    }


 //preview of login scaffold
@Composable
@Preview
fun LoginPreview(){
    MyApplicationTheme {
        AccountScaffold(loginViewModel = LoginViewModel())


    }
}