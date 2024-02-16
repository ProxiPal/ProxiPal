package com.mongodb.app.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build.VERSION_CODES.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.mongodb.app.ComposeLoginActivity

import com.mongodb.app.navigation.NavigationDestination
import com.mongodb.app.presentation.login.LoginAction
import com.mongodb.app.presentation.login.LoginViewModel
import com.mongodb.app.ui.theme.Blue
import com.mongodb.app.ui.theme.Purple200

private const val USABLE_WIDTH = 0.8F

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val title = "ProxiPal"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun LoginScaffold(loginViewModel: LoginViewModel, navigateToRegister: ()->Unit) {



    Scaffold(
        content = {
            Column(
                modifier = Modifier.padding(top = 60.dp).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = "ProxiPal",
                    fontSize = 60.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(500.dp)
                )
                Text(
                    text = "Login to access your bookmarks and personal preferences.",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
                TextField(value = loginViewModel.state.value.email, onValueChange = { loginViewModel.setEmail(it) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                TextField(value = loginViewModel.state.value.password, onValueChange = { loginViewModel.setPassword(it) }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(),modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = { loginViewModel.login(loginViewModel.state.value.email, loginViewModel.state.value.password) },
                    colors = ButtonDefaults.buttonColors(Color(0xFFEF8524)),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Login")
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxSize().padding(60.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "No account yet?")
                    TextButton(
                        onClick = navigateToRegister,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Register here")
                    }
                }
            }
        }
    )
}