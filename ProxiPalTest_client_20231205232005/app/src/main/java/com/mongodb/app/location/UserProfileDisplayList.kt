package com.mongodb.app.location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mongodb.app.domain.UserProfile

// This will be for displaying a dynamically updating list of user profiles
// as they are queried from a specified radius around the current device
@Composable
fun UserProfileList(userProfiles: MutableList<UserProfile>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(userProfiles) { userProfile ->
            UserProfileCard(userProfile)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// A card to display a single user profile in a clean UI
@Composable
fun UserProfileCard(userProfile: UserProfile) {
    Card(
        modifier = Modifier.size(width = 240.dp, height = 100.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = userProfile.firstName,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userProfile.biography,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}