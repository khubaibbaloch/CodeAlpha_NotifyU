package com.notifyu.app.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.notifyu.app.R
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import com.notifyu.app.presentation.screens.main.components.getAvatarList
import com.notifyu.app.presentation.theme.PrimaryColor


@Composable
fun HomeScreen(navController: NavController, mainViewModel: MainViewModel) {
//    val navEvent by mainViewModel.navigation.collectAsState()
//
//    // Initial fetch
//    LaunchedEffect(Unit) {
//        mainViewModel.authFetchSelectedScreenForCurrentUser()
//    }
//
//    // React to navigation event
//    LaunchedEffect(navEvent) {
//        when (navEvent) {
//            is AuthNavEvent.ToOrganizationOwned -> {
//                navController.navigate("dummy") { popUpTo(0) }
//            }
//
//            is AuthNavEvent.ToOrganizationJoined -> {
//                navController.navigate("dummy") { popUpTo(0) }
//            }
//
//            else -> {}
//        }
//    }


    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Owned", "Joined")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PrimaryColor
                )
            },
            divider = { HorizontalDivider(color = Color.Gray.copy(0.2f)) }) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(title)
                        Text(
                            title,
                            color = if (selectedTabIndex == index) PrimaryColor
                            else Color.Black.copy(alpha = 1f)
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> OwnedOrganizationsTab(navController, mainViewModel)
            1 -> JoinedOrganizationsTab(navController, mainViewModel)
        }
    }

}

@Composable
fun OwnedOrganizationsTab(navController: NavController, mainViewModel: MainViewModel) {
    val organizationsOwned by mainViewModel.organizationsOwned.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.authFetchOwnedOrganizations()
    }

    val avatarList = getAvatarList()

    LazyColumn {
        items(organizationsOwned) { org ->
            OrganizationRow(
                name = org.name,
                avatarIndex = org.avatarIndex,
                avatarList = avatarList,
                onClick = {
                    mainViewModel.updateOnOrganizationClick(org.id)
                    navController.navigate(MainScreenRoutes.ChatScreen.route)
                }
            )
        }
    }
}

@Composable
fun JoinedOrganizationsTab(navController: NavController, mainViewModel: MainViewModel) {
    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.authFetchMemberOrganizations()
    }

    val avatarList = getAvatarList()

    LazyColumn {
        items(organizationsMemberOf) { org ->
            OrganizationRow(
                name = org.name,
                avatarIndex = org.avatarIndex,
                avatarList = avatarList,
                onClick = {
                    mainViewModel.updateOnOrganizationClick(org.id)
                    navController.navigate(MainScreenRoutes.ChatScreen.route)
                }
            )
        }
    }
}

@Composable
fun OrganizationRow(
    name: String,
    avatarIndex: Int,
    avatarList: List<Int>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(avatarList[avatarIndex]),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(0.5f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = name, fontSize = 16.sp)
            Text(
                text = "Announcement",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Set", fontSize = 14.sp)
    }
}

