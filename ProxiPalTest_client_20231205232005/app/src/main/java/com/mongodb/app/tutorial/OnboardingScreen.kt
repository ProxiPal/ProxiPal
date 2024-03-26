package com.mongodb.app.tutorial

import android.graphics.Paint.Align
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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.R
import com.mongodb.app.data.MockRepository
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.location.UserProfileDisplayList
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.ui.theme.Blue
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
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
    var circleOffset by remember { mutableIntStateOf(-132) }
    val steps = listOf(
        stringResource(R.string.welcome_to_appname),
        stringResource(R.string.this_is_the_profile_screen_here_you_can_edit_your_profile_and_add_photos),
        stringResource(R.string.this_is_the_connect_screen_here_you_can_connect_with_nearby_users_and_start_looking_for_friends),
        stringResource(R.string.this_is_the_friends_screen_here_you_can_exchange_messages_with_friends),
        stringResource(R.string.great_you_re_all_set)
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Text(if (currentStep < progressSteps) stringResource(R.string.next) else stringResource(R.string.got_it))
                }
            },
            backgroundColor = Purple200,
            modifier = Modifier.padding(16.dp)
        )

        if (currentStep == 1){
            Box {
                UserProfileLayout(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    navController = navController,
                    homeViewModel = homeViewModel
                )
                CircleToBottomAppBar(circleOffset)
            }
        }
        else if (currentStep == 2){
            circleOffset = 0
            Box {
                ConnectWithOthersScreen(
                    toolbarViewModel = toolbarViewModel,
                    navController = navController,
                    userProfileViewModel = userProfileViewModel
                )
                CircleToBottomAppBar(circleOffset)
            }
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
            color = Color.Black
        )
    }
}

@Composable
fun CircleToBottomAppBar(circleOffset: Int) {
    Box (modifier = Modifier.fillMaxSize()){
        androidx.compose.material3.Icon(
            Icons.Outlined.Circle,
            contentDescription = "Circle",
            tint = Color.White,
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .offset(x = circleOffset.dp, y = 25.dp)
                .size(100.dp)
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

@Preview
@Composable
fun UserProfileLayoutWithCircle() {
    MyApplicationTheme {
        val repository = MockRepository()
        val userProfiles = (1..30).map { index ->
            MockRepository.getMockUserProfile(index)
        }.toMutableStateList()
        Box {
            UserProfileLayout(
                userProfileViewModel = UserProfileViewModel(
                    repository = repository,
                    userProfileListState = userProfiles
                ),
                toolbarViewModel = ToolbarViewModel(repository),
                navController = rememberNavController(),
                homeViewModel = HomeViewModel(repository)
            )
            CircleToBottomAppBar(0)
        }
    }
}

