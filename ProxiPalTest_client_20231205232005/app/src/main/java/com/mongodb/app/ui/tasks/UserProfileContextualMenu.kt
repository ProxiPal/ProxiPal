package com.mongodb.app.ui.tasks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview
import com.mongodb.app.data.MockRepository
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.presentation.tasks.UserProfileContextualMenuViewModel
import com.mongodb.app.presentation.tasks.UserProfileViewModel
import com.mongodb.app.ui.components.ContextualMenu
import com.mongodb.app.ui.theme.MyApplicationTheme

@Composable
fun UserProfileContextualMenu(
    viewModel: UserProfileContextualMenuViewModel,
    userProfile: UserProfile
) {
    ContextualMenu(
        contextualMenuViewModel = viewModel,
        onClickContextualMenuViewModelOpen = { viewModel.open() },
        onClickContextualMenuViewModelClose = { viewModel.close() },
        onClickContextualMenuViewModelDeleteEntry = { viewModel.deleteUserProfile(userProfile) }
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun UserProfileContextualMenuPreview() {
    MyApplicationTheme {
        MyApplicationTheme {
            val repository = MockRepository()
            UserProfileContextualMenu(
                UserProfileContextualMenuViewModel(
                    repository,
                    UserProfileViewModel(repository, mutableStateListOf())
                ),
                UserProfile()
            )
        }
    }
}
