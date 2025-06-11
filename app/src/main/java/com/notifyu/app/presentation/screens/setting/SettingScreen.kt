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
import com.notifyu.app.presentation.screens.components.ConfirmationDialog

@Composable
fun SettingScreen(navController: NavController, mainViewModel: MainViewModel) {
    // State to control the visibility of the logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize() // Makes the column take the full available height
            .padding(vertical = 16.dp) // Adds vertical padding to the entire column
    ) {

        // --- Section: Data & Privacy ---
        Row(
            modifier = Modifier
                .fillMaxWidth() // Makes the row take full width
                .clickable {
                    // Navigates to the DataPrivacyScreen when clicked
                    navController.navigate(SettingScreenRoutes.DataPrivacyScreen.route)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp), // Adds padding inside the row
            verticalAlignment = Alignment.CenterVertically // Vertically aligns children to center
        ) {
            // Icon container with a circular background
            Box(
                modifier = Modifier
                    .size(40.dp) // Fixed size box
                    .background(SurfaceColor.copy(0.3f), CircleShape) // Circular background with some transparency
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_privacy), // Loads privacy icon
                    contentDescription = null, // No accessibility description
                    tint = Color.Unspecified, // No tint is applied to icon
                    modifier = Modifier
                        .size(22.dp) // Icon size
                        .align(Alignment.Center) // Centered inside the box
                )
            }
            Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text
            Text("Data and Privacy") // Label for this option
        }

        // Divider under the "Data and Privacy" row, starts after icon
        HorizontalDivider(
            modifier = Modifier.padding(start = 75.dp), // Leaves space to align with text
            color = SurfaceColor.copy(0.5f) // Uses semi-transparent surface color
        )
        Spacer(modifier = Modifier.height(16.dp)) // Space between sections

        // --- Section: About NotifyU ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Navigates to the AboutNotifyuScreen when clicked
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
                    painter = painterResource(R.drawable.ic_aboutus), // Loads About Us icon
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("About NotifyU") // Label for this option
        }

        // Divider under the "About NotifyU" row
        HorizontalDivider(
            modifier = Modifier.padding(start = 75.dp),
            color = SurfaceColor.copy(0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Section: Logout ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Sets state to true to show logout confirmation dialog
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
                    painter = painterResource(R.drawable.ic_logout), // Loads logout icon
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Logout") // Label for logout option
        }

        // Divider under the "Logout" row
        HorizontalDivider(
            modifier = Modifier.padding(start = 75.dp),
            color = SurfaceColor.copy(0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    // --- Logout Confirmation Dialog ---
    ConfirmationDialog(
        title = "Logout", // Dialog title
        text = "Are you sure you want to logout?", // Dialog message
        showDialog = showLogoutDialog, // Dialog visibility based on state
        onDismiss = { showLogoutDialog = false }, // Hides dialog on dismiss
        onConfirm = {
            // Handles logout confirmation
            showLogoutDialog = false // Close the dialog
            mainViewModel.signOut() // Call sign out method from ViewModel
            navController.navigate(AuthScreenRoutes.SignupScreen.route) {
                // Navigate to SignupScreen and clear backstack
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true // Removes all previous destinations
                }
                launchSingleTop = true // Avoid duplicate destination on back stack
            }

            mainViewModel.resetNavigation() // Reset any navigation-related state in ViewModel
        }
    )
}
