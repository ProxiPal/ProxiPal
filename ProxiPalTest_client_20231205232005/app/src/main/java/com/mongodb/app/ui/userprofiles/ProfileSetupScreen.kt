package com.mongodb.app.ui.userprofiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetup(userProfileViewModel: UserProfileViewModel){
    Column {
        TextField(value = userProfileViewModel.userProfileFirstName.value, onValueChange = {userProfileViewModel.setUserProfileFirstName(it)})
        TextField(value = userProfileViewModel.userProfileLastName.value, onValueChange ={userProfileViewModel.setUserProfileLastName(it)} )
        TextField(value = userProfileViewModel.userProfileBiography.value, onValueChange ={userProfileViewModel.setUserProfileBiography(it)} )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScaffold(userProfileViewModel: UserProfileViewModel, onPreviousClicked: ()-> Unit, onNextClicked:()->Unit){
    MyApplicationTheme {
        Scaffold(
            bottomBar = { PreviousNextBottomAppBar(
                onPreviousClicked = onPreviousClicked,
                onNextClicked = onNextClicked,
                currentPage = 1,
                totalPages = 3
            )}
        ){
                innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                ProfileSetup(userProfileViewModel = userProfileViewModel)
            }
        }
    }
}