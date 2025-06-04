package com.notifyu.app.ui.screens.setting

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun AboutNotifyuScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "NotifyU is a simple and secure app designed to help organizations or groups send reminders and event notifications easily.",   fontSize = 14.sp

        )

        Spacer(modifier = Modifier.height(16.dp))

        val points = listOf(
            "Admins can send alerts via SMS and notifications.",
            "Only the admin can see who is in the organization.",
            "No member details are visible to others.",
            "You can leave the organization at any time.",
            "Your data remains hidden after leaving.",
            "Stay informed with complete privacy and control."
        )

        points.forEach { point ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(text = "•", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = point, fontSize = 14.sp)
            }
        }
    }
}

