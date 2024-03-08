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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.SHOULD_USE_TASKS_ITEMS
import com.mongodb.app.presentation.login.EventSeverity
import com.mongodb.app.presentation.login.LoginAction
import com.mongodb.app.presentation.login.LoginEvent
import com.mongodb.app.presentation.login.LoginViewModel
import com.mongodb.app.ui.compassscreen.CompassPermissions
import com.mongodb.app.ui.compassscreen.CompassScreen
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.login.AccountScaffold

import com.mongodb.app.ui.login.LoginScaffold
import com.mongodb.app.ui.login.RegisterScaffold

import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.userprofiles.UserProfileScreen
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.ConnectionException
import io.realm.kotlin.mongodb.exceptions.InvalidCredentialsException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/*
Contributions:
- Kevin Kubota (added switch cases for starting or referencing an activity, see below)

- Vichet Chim (added navigation between login page and sign up page)
 */


class ComposeLoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var client: GoogleSignInClient

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
                    //CompassScreen::class.java
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
                                //CompassScreen::class.java
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
                Button(onClick = { signIn() }) { Text(text = "google login")

                }

                    }
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestServerAuthCode("06277155842-cvp1gfmpl2euecgn1bsaaf4c1il2nnpa.apps.googleusercontent.com")
                .requestServerAuthCode(getString(R.string.server_client_id))
                .build()

            client = GoogleSignIn.getClient(this, googleSignInOptions)

                }
            }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            handleSignInResult(data)
        }
    }

    private fun handleSignInResult(data: Intent?) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            val token: String = account?.idToken!!

            val app: App = App.create("proxipaltest-jyqla") // Replace this with your App ID

            runBlocking {
                val user = app.login(Credentials.google(token, GoogleAuthType.ID_TOKEN))
                // Handle login success, navigate to appropriate screen
                // For now, let's just log success

                Log.i("AUTH", "Successfully logged in with Google")
            }
        } catch (e: ApiException) {
            Log.e("AUTH", "Failed to authenticate using Google OAuth: ${e.message}")
        }
    }
    private fun signIn() {
        val signIntent = client.signInIntent
        startActivityForResult(signIntent, 100)
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == 100){
//            val task: Task<GoogleSignInAccount> =
//                GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//            startActivity(Intent(
//                this,
//                //CompassScreen::class.java
//                UserProfileScreen::class.java
//            ))
//            finish()
//
//        }
//    }
//
//
//        private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            if (completedTask.isSuccessful) {
//                val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
//                val token: String = account?.idToken!!
//                val app: App = App.create("proxipaltest-jyqla") // Replace this with your App ID
//
//                runBlocking { val user =app.login(Credentials.google(token, GoogleAuthType.ID_TOKEN)
//                )
//                }
//            } else {
//                Log.e("AUTH", "Google Auth failed: ${completedTask.exception}")
//            }
//        } catch (e: ApiException) {
//            Log.e("AUTH", "Failed to authenticate using Google OAuth: " + e.message);
//        }
//    }
    private suspend fun handleSignInResult2(account: GoogleSignInAccount?) {
        try{
            Log.d("MainActivity", "${account?.serverAuthCode}")
            //1
            val idToken = account?.serverAuthCode

            //signed in successfully, forward credentials to MongoDB realm
            //2
            //3
            idToken?.let { Credentials.google(it,GoogleAuthType.ID_TOKEN) }?.let {
                app.login(it)
            }
        } catch(exception: ApiException){
            Log.d("MainActivity",  exception.printStackTrace().toString())
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

//    private fun signIn() {
//        val signIntent = client.signInIntent
//        startActivityForResult(signIntent, 100)
//    }
//    fun loginWithGoogle() {
//        val gso = GoogleSignInOptions
//            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken("606277155842-cvp1gfmpl2euecgn1bsaaf4c1il2nnpa.apps.googleusercontent.com")
//            .build()
//        val googleSignInClient = GoogleSignIn.getClient(this, gso)
//        val signInIntent: Intent = googleSignInClient.signInIntent
//        val resultLauncher: ActivityResultLauncher<Intent> =
//            // Note: this activity MUST inherit from ComponentActivity or AppCompatActivity to use this API
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
//            { result ->
//                val task: Task<GoogleSignInAccount> =
//                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                handleSignInResult(task)
//            }
//        resultLauncher.launch(signInIntent)
//    }



//    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            if (completedTask.isSuccessful) {
//                val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
//                val token: String = account?.idToken!!
//                val app: App = App.create("proxipaltest-jyqla") // Replace this with your App ID
//                runBlocking {
//                    val user = app.login(Credentials.google(token, GoogleAuthType.ID_TOKEN))
//                }
//            } else {
//                Log.e("AUTH", "Google Auth failed: ${completedTask.exception}")
//            }
//        } catch (e: ApiException) {
//            Log.e("AUTH", "Failed to authenticate using Google OAuth: " + e.message);
//        }
//    }
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
