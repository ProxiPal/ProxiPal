package com.mongodb.app.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mongodb.app.ui.theme.Purple200

/**
 * This file will have reusable UI composables
 */

/**
 * Proxipal app bar to display a title of the screen and conditionally display the back navigation.
 * Not used for the three main screens (connect with others, messages, profile)
 * Can be used for screens where you need a back button, like settings for example
 * Based on this android example app:
 * https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app/tree/main
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxipalTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple200),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back button"
                    )
                }
            }
        }
    )
}

/**
 * Selected item is 0 for Profile, 1, for Connect, or 2 for Friends
 * TODO onClick will be for navigating to other pages
 */
@Composable
fun ProxiPalBottomAppBar(selectedItem: Int){
    val items = listOf("Profile", "Connect", "Friends")
    val theMap = mapOf(
        0 to Icons.Filled.AccountCircle, 1 to Icons.Filled.PlayArrow,
        2 to Icons.Filled.Favorite
    )
    BottomNavigation(
        windowInsets = BottomNavigationDefaults.windowInsets,
        backgroundColor = Purple200
    ) {
        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = { theMap[index]?.let { Icon(it, contentDescription = null) } },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {},
                selectedContentColor = Color.White
            )
        }
    }
}

