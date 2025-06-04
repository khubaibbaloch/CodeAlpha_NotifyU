package com.notifyu.app.presentation.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import androidx.compose.runtime.*
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.setting.SettingScreenRoutes

@Composable
fun SettingScreen(navController: NavController, mainViewModel: MainViewModel) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    mainViewModel.auth.signOut()
                    navController.navigate(AuthScreenRoutes.SignupScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // Data & Privacy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(SettingScreenRoutes.DataPrivacyScreen.route)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceColor.copy(0.3f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_privacy),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Data and Privacy")
        }

        HorizontalDivider(modifier = Modifier.padding(start = 75.dp), color = SurfaceColor.copy(0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // About NotifyU
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(SettingScreenRoutes.AboutNotifyuScreen.route)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceColor.copy(0.3f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_aboutus),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("About NotifyU")
        }

        HorizontalDivider(modifier = Modifier.padding(start = 75.dp), color = SurfaceColor.copy(0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showLogoutDialog = true
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceColor.copy(0.3f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Logout")
        }

        HorizontalDivider(modifier = Modifier.padding(start = 75.dp), color = SurfaceColor.copy(0.5f))
        Spacer(modifier = Modifier.height(16.dp))
    }
}
