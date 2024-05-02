package com.mongodb.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.mongodb.app.data.userprofiles.SHOULD_USE_TASKS_ITEMS
import com.mongodb.app.presentation.login.EventSeverity
import com.mongodb.app.presentation.login.LoginEvent
import com.mongodb.app.presentation.login.LoginViewModel
import com.mongodb.app.ui.login.AccountScaffold
import com.mongodb.app.ui.messages.MessagesScreen
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.userprofiles.UserProfileScreen
import io.realm.kotlin.Realm
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


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
                startActivity(Intent(
                    this,
//                    CompassScreen::class.java
                    UserProfileScreen::class.java
                ))
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
//                                CompassScreen::class.java
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
                    AccountScaffold(loginViewModel = loginViewModel)
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




