package com.mongodb.app.location

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.mongodb.app.R
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.Purple200
import com.mongodb.app.ui.theme.Purple500
import com.mongodb.app.ui.theme.Purple700

@Composable
fun ProximityRadiusAdjuster(userProfileViewModel: UserProfileViewModel) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Column {
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                userProfileViewModel.updateProximityRadius(it.toDouble()) },
            colors = SliderDefaults.colors(
                thumbColor = Purple200,
                activeTrackColor = Purple500,
                inactiveTrackColor = Purple700,
            ),
            steps = 10,
            valueRange = 0.1f..1f
        )
        Text(text = stringResource(R.string.proximity_radius_text, sliderPosition))
    }
}