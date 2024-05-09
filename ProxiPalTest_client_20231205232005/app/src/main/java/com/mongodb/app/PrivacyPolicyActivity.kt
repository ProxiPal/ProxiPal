/*
Programmer: Brian Poon
 */
package com.mongodb.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Defining the class `LanguageActivity` that extends `AppCompatActivity`
class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3366FF), Color(0xFF33CC99))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    BackButton(onClick = { onBackPressed() }) // Added back button
                    Text(
                        text = "This mobile application is designed as a social networking platform to connect students with similar interests, academic backgrounds, or geographic proximity. Its purpose is to facilitate making new friends.\n" +
                                "                \n" +
                                "To ensure user privacy and data protection, the app collects only the information necessary for basic functionality and user experience enhancement. This includes usernames, profile pictures, interests, and location data. User data is securely stored and managed, accessible only to the user and not shared with any third-party entities.\n" +
                                "                \n" +
                                "The app may collect analytics data, such as user interactions and usage patterns, solely for the purpose of improving the app's performance and user experience.\n" +
                                "                \n" +
                                "Users have full control over their data and can delete their accounts and personal information at any time.\n" +
                                "                \n" +
                                "The app complies with applicable privacy laws and standards, such as the General Data Protection Regulation (GDPR) and the California Consumer Privacy Act (CCPA), and periodically updates its privacy policy in accordance with any relevant changes or developments.",
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
        )
    }
}
