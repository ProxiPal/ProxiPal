package com.mongodb.app.userRatingSystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.data.MockRepository
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel

@Composable
fun UserRatingsDisplayScreen(userProfileViewModel: UserProfileViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserRatingCard(
            title = "Likes",
            count = userProfileViewModel.userRatings[0],
            backgroundColor = Color.Green
        )
        UserRatingCard(
            title = "Dislikes",
            count = userProfileViewModel.userRatings[1],
            backgroundColor = Color.Red
        )
    }
}

@Composable
fun UserRatingCard(title: String, count: Int, backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}

/*
@Preview
@Composable
fun UserRatingsDisplayScreenPreview() {
    val userProfileViewModel = UserProfileViewModel(MockRepository())
    UserRatingsDisplayScreen(userProfileViewModel = userProfileViewModel)
}
*/
