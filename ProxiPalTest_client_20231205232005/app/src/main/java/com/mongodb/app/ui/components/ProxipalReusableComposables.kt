package com.mongodb.app.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.mongodb.app.R
import com.mongodb.app.app
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.tasks.ToolbarEvent
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This file will have reusable UI composables
 */

/**
 * Proxipal app bar to display a title of the screen and display the back navigation.
 * Not used for the three main screens (connect with others, messages, profile)
 * Can be used for screens where you need a back button, like settings for example
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxipalTopAppBarWithBackButton(
    navController: NavController,
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple200),
        navigationIcon = {
            // for navigating back
            IconButton(onClick = {navController.navigateUp()} ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back button"
                )
            }

        }
    )
}

/**
 * Bottom app bar for navigating between the three main screens of the app
 * TODO onClick will be for navigating to other pages
 */
@Composable
fun ProxiPalBottomAppBar(navController: NavHostController) {
    BottomNavigation(
        windowInsets = BottomNavigationDefaults.windowInsets,
        backgroundColor = Purple200
    ) {
        // Define navigation items directly within the BottomNavigation composable
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Navigate to profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { navController.navigate(Routes.UserProfileScreen.route) },
            selectedContentColor = Color.White
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Navigate to connect") },
            label = { Text("Connect") },
            selected = false,
            onClick = { navController.navigate(Routes.ConnectWithOthersScreen.route) },
            selectedContentColor = Color.White
        )
        BottomNavigationItem(
            icon={ Icon(Icons.Filled.Event, contentDescription = "Navigate to events")},
            label = {Text(text = stringResource(id = R.string.events))},
            selected = false,
            onClick= {navController.navigate((Routes.EventScreen.route))},
            selectedContentColor = Color.White
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Navigate to friends") },
            label = { Text("Friends") },
            selected = false,
            onClick = {    },
            selectedContentColor = Color.White
        )

    }
}