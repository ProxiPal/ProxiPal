package com.mongodb.app.ui.tasks

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.ui.components.ProxiPalBottomAppBar
import com.mongodb.app.ui.components.ProxipalTopAppBar
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import com.mongodb.app.ui.theme.Purple500

// TODO location services, find out how to connect nearby users

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConnectWithOthersScreen (
    navigateToProfile: () -> Unit,
    navigateToMessages: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            ProxiPalBottomAppBar(selectedItem = 0, onClick = {})
        }

    ) { innerPadding ->
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectWithOthersScreenPreview() {
    MyApplicationTheme {
        ConnectWithOthersScreen(
            navigateToProfile = {},
            navigateToMessages = {},
            navigateToSettings = {}
        )
    }
}