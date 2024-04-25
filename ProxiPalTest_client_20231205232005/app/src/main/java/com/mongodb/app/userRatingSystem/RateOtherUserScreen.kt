package com.mongodb.app.userRatingSystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mongodb.app.data.MockRepository
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.Purple200

@Composable
fun RateUserPopup(
    otherUserOwnerId: String,
    userProfileViewModel: UserProfileViewModel,
    onClosePopup: () -> Unit
) {
    val (ratingGiven, setRatingGiven) = remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!ratingGiven) {
                onClosePopup()
            }
        }
    ) {
        Surface(
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Rate this user")

                Row {
                    Button(
                        onClick = {
                            userProfileViewModel.rateOtherUser(otherUserOwnerId, ratingGiven = true)
                            setRatingGiven(true)
                            onClosePopup()
                        },
                        enabled = !ratingGiven,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Purple200)
                    ) {
                        Text("Like")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            userProfileViewModel.rateOtherUser(otherUserOwnerId, ratingGiven = false)
                            setRatingGiven(true)
                            onClosePopup()
                        },
                        enabled = !ratingGiven,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Purple200)
                    ) {
                        Text("Dislike")
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun RateUserPopupPreview() {
    val repository = MockRepository()
    var isPopupVisible by remember { mutableStateOf(true) }
    if (isPopupVisible) {
        RateUserPopup(otherUserOwnerId = "otherUserId", userProfileViewModel = UserProfileViewModel(repository)) {
            isPopupVisible = false
        }
    }
}

