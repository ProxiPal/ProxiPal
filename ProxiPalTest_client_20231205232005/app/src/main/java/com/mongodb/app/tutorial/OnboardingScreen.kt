package com.mongodb.app.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mongodb.app.R
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.ui.theme.Blue
import com.mongodb.app.ui.theme.Shapes
import com.mongodb.app.ui.userprofiles.UserProfileLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
@Composable
fun OnboardingScreen() {
    var showDialog by remember { mutableStateOf(true) }
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Welcome to Your App!") },
            text = {
                Column(
                    modifier = Modifier.size(100.dp, 100.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Here's how to use the app:")
                    Text(text = "- Step 1: Do something")
                    Text(text = "- Step 2: Do something else")
                    // Add more instructions as needed
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                ) {
                    Text("Got it!")
                }
            }
        )
    }
}
 */

@Composable
fun AppOnboarding(
    userProfileViewModel: UserProfileViewModel,
    toolbarViewModel: ToolbarViewModel,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
)
{
    var currentStep by remember { mutableIntStateOf(0) }
    var alertDialogOffset by remember { mutableIntStateOf(80) }
    val steps = listOf(
        "Welcome to Your App!",
        "This is the home screen, here you can edit your profile and add photos!",
        "This is the connect screen, here you can connect with nearby users and start looking for friends!",
        "Great! You're all set!"
    )

    val progressSteps = steps.size - 1
    var progress by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        LinearDeterminateIndicator(progress) // Display LinearDeterminateIndicator throughout all steps
        AlertDialog(
            onDismissRequest = { /* Do nothing - dialog can only be dismissed by user interaction */ },
            title = { Text(text = steps[currentStep]) },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentStep < progressSteps) {
                            currentStep++
                            progress = (currentStep.toFloat() / progressSteps)
                        } else {
                            navController.navigate(Routes.UserProfileScreen.route)
                        }
                    },
                ) {
                    Text(if (currentStep < progressSteps) "Next" else "Got it!")
                }
            },
            modifier = Modifier.padding(16.dp)
        )

        if (currentStep == 1){
            UserProfileLayout(
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                homeViewModel = homeViewModel)
        }
        else if (currentStep == 2){
            ConnectWithOthersScreen(
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                userProfileViewModel = userProfileViewModel
            )
        }
    }
}

@Composable
fun LinearDeterminateIndicator(progress: Float) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            color = Color.Yellow
        )
    }
}

@Composable
fun ArrowToBottomAppBar(modifier: Modifier = Modifier) {
    Box {
        androidx.compose.material3.Icon(
            Icons.Filled.ArrowDownward,
            contentDescription = "Arrow Downward",
            modifier = Modifier.align(alignment = Alignment.BottomCenter)
        )
    }

}


@Composable
fun OnboardingScreen(
    userProfileViewModel: UserProfileViewModel,
    toolbarViewModel: ToolbarViewModel,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Surface(color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppOnboarding(
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                homeViewModel = homeViewModel
            )
        }
    }
}

