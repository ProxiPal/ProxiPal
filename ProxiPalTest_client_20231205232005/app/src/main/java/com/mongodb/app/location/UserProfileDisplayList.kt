package com.mongodb.app.location
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mongodb.app.R
import com.mongodb.app.data.MockRepository
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.report.ReportDropDownMenu
import com.mongodb.app.ui.report.ReportViewModel

// Contribution: Marco Pacini
/**
 * Composable function for displaying a dynamically updating list of user profiles
 * as they are queried from a specified radius around the current device
 */
@Composable
fun UserProfileDisplayList(userProfiles: List<UserProfile>, isLookingForUsers: Boolean, reportViewModel: ReportViewModel) {
    if (isLookingForUsers){
        if (userProfiles.isEmpty()){
            Box(Modifier.fillMaxSize()){
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
        }
        else {
            Box(Modifier.fillMaxSize()){
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(userProfiles) { userProfile ->
                        UserProfileCard(userProfile, onItemClick = { /*TODO*/ }, reportViewModel = reportViewModel )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// A card to display a single user profile in a clean UI
@Composable
fun UserProfileCard(userProfile: UserProfile, onItemClick: (UserProfile) -> Unit, reportViewModel: ReportViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(userProfile) }, // Make the card clickable
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
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
            ReportDropDownMenu(reportedUser = userProfile._id.toString(),reportViewModel = reportViewModel)
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
        ) {
            Text(
                text = stringResource(R.string.searching_for_nearby_users),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

//@Preview
@Composable
fun PreviewUserProfileDisplayList(reportViewModel:ReportViewModel) {
    val sampleUserProfiles = (1..10).map { index ->
        MockRepository.getMockUserProfile(index)
    }.toList()

    UserProfileDisplayList(userProfiles = sampleUserProfiles, true, reportViewModel = reportViewModel)
}

//@Preview
@Composable
fun PreviewEmptyUserProfileDisplayList(reportViewModel:ReportViewModel) {
    val sampleUserProfiles = listOf<UserProfile>()

    UserProfileDisplayList(userProfiles = sampleUserProfiles, true, reportViewModel = reportViewModel)
}