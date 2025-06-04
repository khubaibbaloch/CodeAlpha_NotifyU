package com.notifyu.app.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.ui.theme.SurfaceColor
import com.notifyu.app.viewmodel.MainViewModel

@Composable
fun OrganizationJoinedScreen(navController: NavController,mainViewModel: MainViewModel) {

    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()

    val avatarList = listOf(
        com.notifyu.app.R.drawable.avatar_index_0,
        com.notifyu.app.R.drawable.avatar_index_1,
        com.notifyu.app.R.drawable.avatar_index_2,
        com.notifyu.app.R.drawable.avatar_index_3,
        com.notifyu.app.R.drawable.avatar_index_4,
        com.notifyu.app.R.drawable.avatar_index_5,
        com.notifyu.app.R.drawable.avatar_index_6,
        com.notifyu.app.R.drawable.avatar_index_7,
        com.notifyu.app.R.drawable.avatar_index_8,
        com.notifyu.app.R.drawable.avatar_index_9,
    )


    LaunchedEffect(Unit) {
        mainViewModel.fetchMemberOrganizations()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(organizationsMemberOf) { organizations ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable {
                            mainViewModel.updateOnOrganizationClick(organizations.id)
                            navController.navigate(MainScreenRoutes.ChatScreen.route)
                        }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(avatarList[organizations.avatarIndex]),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceColor.copy(0.5f))

                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Column {
                        Text(text = organizations.name, fontSize = 16.sp)
                        Text(text = "Announcement", fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "Set", fontSize = 14.sp)

                }
            }
        }
    }

}