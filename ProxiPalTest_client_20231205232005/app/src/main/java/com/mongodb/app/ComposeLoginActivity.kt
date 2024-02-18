package com.mongodb.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.SHOULD_USE_TASKS_ITEMS
import com.mongodb.app.presentation.login.EventSeverity
import com.mongodb.app.presentation.login.LoginAction
import com.mongodb.app.presentation.login.LoginEvent
import com.mongodb.app.presentation.login.LoginViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.login.HomeDestination
import com.mongodb.app.ui.login.LoginScaffold
import com.mongodb.app.ui.login.RegisterScaffold
import com.mongodb.app.ui.login.RegisterScreen
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.userprofiles.UserProfileScreen
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.launch


/*
Contributions:
- Kevin Kubota (added switch cases for starting or referencing an activity, see below)

- Vichet Chim (added navigation between login page and sign up page)
 */


class ComposeLoginActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fast-track task list screen if we are logged in
        if (app.currentUser != null) {
            // Contributed by Kevin Kubota
            if (SHOULD_USE_TASKS_ITEMS)
                startActivity(Intent(this, ComposeItemActivity::class.java))
            else
                startActivity(Intent(this, UserProfileScreen::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            // Subscribe to navigation and message-logging events
            loginViewModel.event
                .collect { event ->
                    when (event) {
                        is LoginEvent.GoToTasks -> {
                            event.process()

                            // Contributed by Kevin Kubota
                            val intent = if (SHOULD_USE_TASKS_ITEMS) Intent(
                                this@ComposeLoginActivity,
                                ComposeItemActivity::class.java
                            )
                            else Intent(
                                this@ComposeLoginActivity,
                                UserProfileScreen::class.java
                            )
                            startActivity(intent)
                            finish()
                        }

                        is LoginEvent.ShowMessage -> event.process()
                    }
                }
        }

        setContent {
            MyApplicationTheme {
                // Vichet Chim - navigation between login and sign up
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = HomeDestination.route){
                    composable(route = HomeDestination.route){
                        LoginScaffold(loginViewModel,
                            navigateToRegister = {navController.navigate(RegisterScreen.route)}
                        )
                    }
                    composable(route = RegisterScreen.route){
                        RegisterScaffold(loginViewModel = loginViewModel, navigateBack = {
                            navController.navigate(HomeDestination.route)
                        })
                        }

                        }
                    }
                }
            }



    private fun LoginEvent.process() {
        when (severity) {
            EventSeverity.INFO -> Log.i(TAG(), message)
            EventSeverity.ERROR -> {
                Log.e(TAG(), message)
                Toast.makeText(this@ComposeLoginActivity, message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun LoginActivityPreview() {
//    MyApplicationTheme {
//        val viewModel = LoginViewModel().also {
//            it.switchToAction(LoginAction.LOGIN)
//            it.setEmail("test@test.com")
//            it.setPassword("123456")
//        }
//        LoginScaffold(viewModel)
//    }
//}
