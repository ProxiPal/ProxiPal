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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.R
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
    var confirmPassword by remember {mutableStateOf("")}
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(top = 60.dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 70.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(500.dp),
            color = Color.Black
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
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = confirmPassword,
            onValueChange = { confirmPassword =it },
            label = { Text(stringResource(id = R.string.prompt_password_comfirm)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisibility)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (confirmPasswordVisibility) "Hide password" else "Show password"

                // Toggle button to hide or display password
                IconButton(onClick = {confirmPasswordVisibility = !confirmPasswordVisibility}){
                    Icon(imageVector  = image, description)
                }
            },
            modifier = Modifier.fillMaxWidth())
        // check if passwords match
        if (confirmPassword != loginViewModel.state.value.password){
            Text(stringResource(id = R.string.matching_password), color = Color.Red, fontSize = 10.sp)
        }
        // shows register button
        if (isRegistrationButtonEnabled) {
            Button(
                onClick = { if (confirmPassword == loginViewModel.state.value.password){
                    loginViewModel.createAccount(
                        loginViewModel.state.value.email,
                        loginViewModel.state.value.password
                    )};
                    isRegistrationButtonEnabled = false
                },
                enabled = isRegistrationButtonEnabled,

                colors = ButtonDefaults.buttonColors(Color(0xFFEF8524)),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(id = R.string.create_account))
            }
        } else{ // shows login button
            Button(
                onClick = {loginViewModel.login(loginViewModel.state.value.email,loginViewModel.state.value.password)},
                colors = ButtonDefaults.buttonColors(Color(0xFFEF8524)),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.fillMaxWidth(),
            ){
                Text(text = stringResource(id = R.string.log_in))
            }
        }
        // prompts a message to confirm account creation through email
        if (!isRegistrationButtonEnabled){
            Text(
                text = stringResource(id=R.string.email_confirmation),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

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
@Composable
@Preview
fun registerpreview(){
    MyApplicationTheme {
        RegisterScaffold(loginViewModel = LoginViewModel(), toggleRegistrationScreen = {})


    }
}