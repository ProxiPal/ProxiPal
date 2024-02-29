package com.mongodb.app.ui.tasks

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mongodb.app.R
import com.mongodb.app.app
import com.mongodb.app.location.LocationUpdatesScreen
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.tasks.ToolbarEvent
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.components.ProxiPalBottomAppBar
import com.mongodb.app.ui.components.ProxipalTopAppBarWithBackButton
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import com.mongodb.app.ui.theme.Purple500
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Contribution: Marco Pacini
/**
 * Connect with others screen which will have a switch to enable or disable connection
 * Will display a dynamically updating list of nearby users
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConnectWithOthersScreen (
    toolbarViewModel: ToolbarViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = { ConnectWithOthersToolbar(viewModel = toolbarViewModel, navController = navController) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            ProxiPalBottomAppBar(navController)
        }

    ) {
        LocationUpdatesScreen(userProfileViewModel = userProfileViewModel)
    }
}

// Toolbar for the connect screen, has location settings button
@ExperimentalMaterial3Api
@Composable
fun ConnectWithOthersToolbar(viewModel: ToolbarViewModel, navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                text = "Connect with Others",
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple200),
        actions = {

            // LOCATION SETTINGS
            IconButton(
                onClick = {
                    navController.navigate(Routes.LocationPermissionsScreen.route)
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.Place, contentDescription = null)
            }

            // App SETTINGS
            IconButton(
                onClick = {
                    navController.navigate(Routes.ScreenSettings.route)
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = null)
            }

            // Log out
            IconButton(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            app.currentUser?.logOut()
                        }.onSuccess {
                            viewModel.logOut()
                        }.onFailure {
                            viewModel.error(ToolbarEvent.Error("Log out failed", it))
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_logout_24_white),
                    contentDescription = null
                )
            }
        })
}
