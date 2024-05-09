package com.mongodb.app.home
import android.annotation.SuppressLint
import android.content.Intent
import android.service.autofill.OnClickAction
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.mongodb.app.R
import com.mongodb.app.AllowNotificationsActivity
import com.mongodb.app.LanguageActivity
import com.mongodb.app.PrivacyPolicyActivity
import com.mongodb.app.navigation.Routes

//This is the code to edit the settings screen.
@Composable
fun ScreenSettings(navController: NavHostController) {
    val context = LocalContext.current
    val gradientColors = listOf(Color(0xFFFFAA33), Color(0xFFFFD700))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(gradientColors))
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()) // Add vertical scrolling
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back Arrow",
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f, fill = false))
            }
            SettingsItem("Profile Info") {
                navController.navigate(Routes.UserProfileScreen.route)
            }
            SettingsItem("Language") {
                val intent = Intent(context, LanguageActivity::class.java)
                context.startActivity(intent)
            }
            SettingsItem("Allow Notifications") {
                val intent = Intent(context, AllowNotificationsActivity::class.java)
                context.startActivity(intent)
            }
            SettingsItem("User Filters") {
                navController.navigate(Routes.FilterScreen.route)
            }
            SettingsItem("Advanced Settings") {
                navController.navigate(Routes.AdvancedScreenSettings.route)
            }
            SettingsItem("Privacy Policy") {
                val intent = Intent(context, PrivacyPolicyActivity::class.java)
                context.startActivity(intent)
            }
        }
    }
}

@Composable
private fun SettingsItem(settingName: String, onClick: () -> Unit) {
    Divider(
        color = Color.White,
        thickness = 2.dp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Row(
        modifier = Modifier.clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = settingName,
            color = Color.White,
            fontSize = 40.sp,
        )
    }
}
