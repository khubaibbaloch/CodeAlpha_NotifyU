package com.notifyu.app.presentation.screens.main

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.notifyu.app.presentation.screens.main.components.RequestPermissions
import com.notifyu.app.presentation.screens.main.components.getAvatarList
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.utils.formatMessageTimestamp


@Composable
fun HomeScreen(navController: NavController, mainViewModel: MainViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        mainViewModel.resetNavigation()
        // hideKeyboard(context)
    }


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RequestPermissions(
            permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
            onAllGranted = {
                // All permissions granted
            },
            onDenied = { denied, permanentlyDenied ->
//                if (permanentlyDenied.isNotEmpty()) {
//                    // Show dialog directing user to app settings
//                } else {
//                    // Show retry rationale dialog
//                }
            }
        )
    }


    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf("Owned", "Joined")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
            )
    ) {
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
    val avatarList = getAvatarList()
    val sortedOrgs =organizationsOwned
        .sortedByDescending { it.lastMessage?.timestamp ?: 0L }


            LazyColumn {
        items(sortedOrgs) { org ->
            val time =
                org.lastMessage?.timestamp?.takeIf { it > 0L }?.let { formatMessageTimestamp(it) }
                    ?: ""

            OrganizationRow(
                name = org.name,
                lastMessage = org.lastMessage?.content ?: "",
                time = time,
                avatarIndex = org.avatarIndex,
                avatarList = avatarList,
                isSeen = true,
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
    val currentUser by mainViewModel.currentUser.collectAsState()
//    LaunchedEffect(Unit) {
//        mainViewModel.authFetchMemberOrganizations()
//    }

    val avatarList = getAvatarList()
    val sortedOrgs = organizationsMemberOf
        .sortedByDescending { it.lastMessage?.timestamp ?: 0L }


    LazyColumn {
        items(sortedOrgs) { org ->
            val time = org.lastMessage?.timestamp?.takeIf { it > 0L }
                ?.let { formatMessageTimestamp(it) } ?: ""

            val isSeen = currentUser?.uid?.let { uid ->
                org.lastMessage?.seenBy?.contains(uid)
            } ?: false

            OrganizationRow(
                name = org.name,
                lastMessage = org.lastMessage?.content ?: "",
                time = time,
                avatarIndex = org.avatarIndex,
                avatarList = avatarList,
                isSeen = isSeen,
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
    lastMessage: String,
    time: String,
    avatarIndex: Int,
    avatarList: List<Int>,
    isSeen: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (isSeen) {
        Color.Black
    } else {
        PrimaryColor
    }

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
                .background(SurfaceColor.copy(0.5f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                Text(text = time, fontSize = 14.sp,color = textColor,)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Announcement: $lastMessage",
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                if (!isSeen){
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(PrimaryColor, CircleShape)
                    )
                }

            }



        }

    }

}

