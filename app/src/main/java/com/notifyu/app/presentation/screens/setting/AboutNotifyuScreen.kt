package com.notifyu.app.presentation.screens.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Composable function to show the "About" screen for the NotifyU app
@Composable
fun AboutNotifyuScreen() {
    // Column arranges children vertically
    Column(
        modifier = Modifier
            .fillMaxSize()            // Makes the column fill the entire screen size
            .padding(16.dp)           // Adds padding around the column content
    ) {

        // Display a descriptive text about what the NotifyU app does
        Text(
            text = "NotifyU is a simple and secure app designed to help organizations or groups send reminders and event notifications easily.",
            fontSize = 14.sp          // Sets the font size to 14sp
        )

        // Adds vertical space between the description and the bullet list
        Spacer(modifier = Modifier.height(16.dp))

        // Define a list of features or points about the app
        val points = listOf(
            "Admins can send alerts via SMS and notifications.",         // Feature 1
            "Only the admin can see who is in the organization.",        // Feature 2
            "No member details are visible to others.",                  // Feature 3
            "You can leave the organization at any time.",               // Feature 4
            "Your data remains hidden after leaving.",                   // Feature 5
            "Stay informed with complete privacy and control."           // Feature 6
        )

        // Loop through each point in the list and display it with a bullet
        points.forEach { point ->
            // Row arranges bullet and text horizontally
            Row(
                verticalAlignment = Alignment.Top,                      // Align text to top
                modifier = Modifier.padding(bottom = 8.dp)             // Adds space after each point
            ) {
                Text(text = "•", fontSize = 16.sp)                      // Bullet symbol
                Spacer(modifier = Modifier.width(8.dp))                // Space between bullet and text
                Text(text = point, fontSize = 14.sp)                   // The point text itself
            }
        }
    }
}
