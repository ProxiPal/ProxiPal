package com.mongodb.app.userratingsystem

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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

/**
 * Popup allowing the current user to rate another user by taking in the other user's owner id as input
 */
@Composable
fun RateUserPopup(
    otherUserOwnerId: String,
    userProfileViewModel: UserProfileViewModel,
    onClose: () -> Unit = {} // Callback function to be invoked when popup is closed
) {
    val (ratingGiven, setRatingGiven) = remember { mutableStateOf(false) }
    val (popupVisible, setPopupVisible) = remember { mutableStateOf(true) }

    if (!popupVisible) {
        // If popup is not visible, return empty composable
        return
    }

    Dialog(
        onDismissRequest = {
            if (!ratingGiven) {
                // Close the popup if no rating is given
                setPopupVisible(false)
                onClose()
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
                            // Close the popup after rating
                            setPopupVisible(false)
                            onClose()
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
                            // Close the popup after rating
                            setPopupVisible(false)
                            onClose()
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
    RateUserPopup(otherUserOwnerId = "otherUserId", userProfileViewModel = UserProfileViewModel(repository))
}

