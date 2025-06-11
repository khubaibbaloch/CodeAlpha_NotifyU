package com.notifyu.app.presentation.screens.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DataPrivacyScreen() {
    // Creates a scroll state object to enable vertical scrolling
    val scrollState = rememberScrollState()

    // A vertical column layout with modifiers applied
    Column(
        modifier = Modifier
            .fillMaxSize() // Makes the column take full available size of the screen
            .verticalScroll(scrollState) // Enables vertical scrolling using the scroll state
            .padding(16.dp) // Adds 16dp padding around the content
    ) {

        // Section Title: Data Collection
        Text(
            text = "1. Data Collection",
            fontSize = 16.sp, // Font size of the text
            fontWeight = FontWeight.SemiBold // Semi-bold font weight for emphasis
        )

        // Description for Data Collection
        Text(
            text = "We only collect the following user data:\n" +
                    "- Email address (used for login and identification)\n" +
                    "- Organization-related data (stored in Firebase Firestore)",
            fontSize = 14.sp // Slightly smaller font for descriptive text
        )

        // Spacer adds vertical space between sections (12dp here)
        Spacer(modifier = Modifier.height(12.dp))

        // Section Title: Data Usage
        Text(
            text = "2. Data Usage",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Description for Data Usage
        Text(
            text = "Your data is used solely to enable login and manage your organizations. We do not share your data with any third parties.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section Title: Data Storage
        Text(
            text = "3. Data Storage",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Description for Data Storage
        Text(
            text = "All data is stored securely in Firebase Firestore. No data is stored locally on your device. There is no offline data storage.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section Title: Authentication
        Text(
            text = "4. Authentication",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Description for Authentication
        Text(
            text = "We use Firebase Authentication for secure user login via email and password. We do not access or store your password.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section Title: Your Control
        Text(
            text = "5. Your Control",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Description for User Control
        Text(
            text = "You can request to delete your account and all your data by contacting us or using account management features within the app.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section Title: Third-Party Services
        Text(
            text = "6. Third-Party Services",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Description for Third-Party Services
        Text(
            text = "We only use Firebase services (Authentication and Firestore). No third-party analytics or tracking tools are integrated.",
            fontSize = 14.sp
        )
    }
}
