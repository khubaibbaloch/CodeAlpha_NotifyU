package com.notifyu.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.navigation.navgraph.MainScreenRoute

@Composable
fun OrganizationJoinedScreen(navController: NavController) {
    LazyColumn {
        items(20) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable{navController.navigate(MainScreenRoute.EventChatScreen.route)}
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .background(color = Color.Green, shape = CircleShape)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column {
                    Text(text = "Name", fontSize = 16.sp)
                    Text(text = "Announcement", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "Set", fontSize = 14.sp)

            }
        }
    }
}