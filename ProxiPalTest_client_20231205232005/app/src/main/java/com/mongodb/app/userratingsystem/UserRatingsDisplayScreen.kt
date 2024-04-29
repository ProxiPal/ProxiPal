package com.mongodb.app.userratingsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel

@Composable
fun UserRatingsDisplayScreen(userProfileViewModel: UserProfileViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        UserRatingCard(
            title = "Likes",
            count = userProfileViewModel.userRatings.getOrElse(0) { 0 },
            backgroundColor = Color.Green
        )
        Spacer(modifier = Modifier.width(4.dp))
        UserRatingCard(
            title = "Dislikes",
            count = userProfileViewModel.userRatings.getOrElse(1) { 0 },
            backgroundColor = Color.Red
        )
    }
}

@Composable
fun UserRatingCard(title: String, count: Int, backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(4.dp),
        backgroundColor = backgroundColor,
        modifier = Modifier.widthIn(max = 80.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

