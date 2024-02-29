package com.mongodb.app.location
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mongodb.app.R
import com.mongodb.app.data.MockRepository
import com.mongodb.app.domain.UserProfile

// Contribution: Marco Pacini
/**
 * Composable function for displaying a dynamically updating list of user profiles
 * as they are queried from a specified radius around the current device
 */
@Composable
fun UserProfileDisplayList(userProfiles: List<UserProfile>, isLookingForUsers: Boolean) {
    if (isLookingForUsers){
        if (userProfiles.isEmpty()){
            LazyColumn (
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    EmptyListCard()
                }
            }
        }
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp)
            ) {
                items(userProfiles) { userProfile ->
                    UserProfileCard(userProfile)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// A card to display a single user profile in a clean UI
@Composable
fun UserProfileCard(userProfile: UserProfile) {
    Card(
        modifier = Modifier.size(width = 240.dp, height = 80.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .horizontalScroll(rememberScrollState())
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

@Composable
fun EmptyListCard() {
    Card(
        modifier = Modifier.size(width = 360.dp, height = 80.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.searching_for_nearby_users),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview
@Composable
fun PreviewUserProfileDisplayList() {
    val sampleUserProfiles = (1..10).map { index ->
        MockRepository.getMockUserProfile(index)
    }.toList()

    UserProfileDisplayList(userProfiles = sampleUserProfiles, true)
}
@Preview

@Composable
fun PreviewEmptyUserProfileDisplayList() {
    val sampleUserProfiles = listOf<UserProfile>()

    UserProfileDisplayList(userProfiles = sampleUserProfiles, true)
}