package com.mongodb.app.location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.app.R
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.Purple200
import com.mongodb.app.ui.theme.Purple500
import com.mongodb.app.ui.theme.Purple700
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle

// contribution: Marco Pacini
/**
 * A composable for displaying a slider that adjusts the proximity radius.
 */
@Composable
fun ProximityRadiusAdjuster(userProfileViewModel: UserProfileViewModel) {
    var sliderPosition by remember { mutableFloatStateOf(userProfileViewModel.proximityRadius.value.toFloat()) }
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
            steps = 4,
            valueRange = 0.05f..0.1f
        )
        Text(
            text = stringResource(R.string.proximity_radius_text, sliderPosition*1000),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
            fontStyle = FontStyle.Italic
        )
    }
}