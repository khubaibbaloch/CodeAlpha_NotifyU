package com.notifyu.app.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.notifyu.app.R
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.RootNavHost
import com.notifyu.app.presentation.screens.chat.components.EventChatScreenTopBar
import com.notifyu.app.presentation.screens.main.components.MainScreenTopBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.notifyu.app.data.model.Organization
import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.presentation.navigation.navgraph.setting.SettingScreenRoutes
import com.notifyu.app.presentation.screens.main.components.JoinCreateOrgBottomSheet
import com.notifyu.app.presentation.screens.main.components.SettingScreenTopBar
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isOrganizationOwned = remember { mutableStateOf(false) }
    val isOrganizationJoined = remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var showBottomSheet by remember { mutableStateOf(false) }

    val organizations by mainViewModel.organizationsOwned.collectAsState()
    val organizationId by mainViewModel.onOrganizationsClick.collectAsState()

    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()
    val currentUser = mainViewModel.auth.currentUser
    val selectedOrganization = remember(organizations, organizationsMemberOf, organizationId) {
        organizations.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }
    }
    val isOwner = remember(selectedOrganization, currentUser) {
        selectedOrganization?.owner == currentUser?.uid
    }
    var isMenuExpanded by remember { mutableStateOf(false) }

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
//        mainViewModel.fetchSelectedScreenForCurrentUser { selectedScreen ->
//            when (selectedScreen) {
//                "owned" -> {
//                    isOrganizationOwned.value = true
//                    isOrganizationJoined.value = false
//                }
//
//                "joined" -> {
//                    isOrganizationJoined.value = true
//                    isOrganizationOwned.value = false
//                }
//
//                "none" -> {
//
//                }
//            }
//        }

        mainViewModel.authFetchSelectedScreenForCurrentUser { selectedScreen ->
            when (selectedScreen) {
                "owned" -> {
                    isOrganizationOwned.value = true
                    isOrganizationJoined.value = false
                }

                "joined" -> {
                    isOrganizationJoined.value = true
                    isOrganizationOwned.value = false
                }

                "none" -> {

                }
            }
        }
    }

//    BackHandler(enabled = drawerState.isOpen) {
//        scope.launch {
//            drawerState.close()
//        }
//    }


    ModalNavigationDrawer(
        modifier = Modifier.statusBarsPadding(),
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${mainViewModel.auth.currentUser?.email}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_setting),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                navController.navigate(
                                    SettingScreenRoutes.SettingScreen.route

                                )
                            }
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBottomSheet = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = "Add organization", fontSize = 14.sp, fontWeight = FontWeight.W400)
                }
                Spacer(modifier = Modifier.padding(vertical = 24.dp))

                OrganizationOwned(
                    organizations = organizations,
                    isOrganizationOwned = isOrganizationOwned.value,
                    avatarList = avatarList,
                    onOwnedClick = {
                        scope.launch {
                            drawerState.close()
//                            mainViewModel.updateSelectedScreen(
//                                mainViewModel.auth.currentUser?.uid ?: "", SelectedScreen.Owned
//                            )
                            mainViewModel.authUpdateSelectedScreen(
                                mainViewModel.auth.currentUser?.uid ?: "", SelectedScreen.Owned
                            )
                            navController.navigate(MainScreenRoutes.OrganizationOwnedScreen.route)
                        }

                    },
                    onOrganizationOwnedClick = { organization ->
                        scope.launch {
                            drawerState.close()
                            mainViewModel.updateOnOrganizationClick(organization.id)
                            navController.navigate(MainScreenRoutes.ChatScreen.route)
                        }

                    })
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                OrganizationJoined(
                    avatarList = avatarList,
                    organizationsMemberOf = organizationsMemberOf,
                    isOrganizationJoined = isOrganizationJoined.value,
                    onJoined = {
                        scope.launch {
                            drawerState.close()
//                            mainViewModel.updateSelectedScreen(
//                                mainViewModel.auth.currentUser?.uid ?: "", SelectedScreen.Joined
//                            )
                            mainViewModel.authUpdateSelectedScreen(
                                mainViewModel.auth.currentUser?.uid ?: "", SelectedScreen.Joined
                            )
                            navController.navigate(MainScreenRoutes.OrganizationJoinedScreen.route)
                        }

                    },
                    onOrganizationJoinedClicked = { organization ->
                        scope.launch {
                            drawerState.close()
                            mainViewModel.updateOnOrganizationClick(organization.id)
                            navController.navigate(MainScreenRoutes.ChatScreen.route)
                        }
                    })

            }

            if (showBottomSheet) {
                JoinCreateOrgBottomSheet(
                    onCreateClick = {
                        scope.launch {
                            showBottomSheet = false
                            drawerState.close()
                            mainViewModel.updateAddOrg(true)
                            navController.navigate(MainScreenRoutes.CreateJoinOrgScreen.route)
                        }

                    },
                    onJoinClick = {
                        scope.launch {
                            showBottomSheet = false
                            drawerState.close()
                            mainViewModel.updateAddOrg(false)
                            navController.navigate(MainScreenRoutes.CreateJoinOrgScreen.route)
                        }

                    },
                    showSheet = showBottomSheet,
                    onDismissRequest = { showBottomSheet = false }
                )
            }


        }) {

        Scaffold(
            topBar = {
                when (currentRoute) {
                    MainScreenRoutes.ChatScreen.route -> {
                        EventChatScreenTopBar(
                            title = selectedOrganization?.name ?: "",
                            onNavigationClick = { navController.popBackStack() },
                            isOwner = isOwner,
                            isDropDownMenuClicked = isMenuExpanded,
                            onMoreVertClick = { isMenuExpanded = true },
                            onDismissRequest = { isMenuExpanded = false },
                            onLeaveClick = {
                                isMenuExpanded = false
                                if (!isOwner) {
//                                    mainViewModel.removeMemberFromOrganization(
//                                        mainViewModel.auth.currentUser?.uid ?: ""
//                                    ) { success, message ->
//                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                                        if (success) {
//                                            navController.popBackStack()
//                                        }
//                                    }
                                    mainViewModel.authRemoveMemberFromOrganization(
                                        mainViewModel.auth.currentUser?.uid ?: ""
                                    ) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            navController.popBackStack()
                                        }
                                    }
                                }

                            }
                        )
                    }

                    MainScreenRoutes.OrganizationOwnedScreen.route -> {
                        MainScreenTopBar(
                            title = if (isOrganizationJoined.value) "Joined" else "Owned",
                            onNavigationClick = {
                                scope.launch { drawerState.open() }
                            })
                    }

                    MainScreenRoutes.OrganizationJoinedScreen.route -> {
                        MainScreenTopBar(
                            title = if (isOrganizationOwned.value) "Owned" else "Joined",
                            onNavigationClick = { scope.launch { drawerState.open() } })
                    }

                    MainScreenRoutes.HomeScreen.route, MainScreenRoutes.CreateJoinOrgScreen.route -> {
                        MainScreenTopBar(
                            title = "Notifyu",
                            onNavigationClick = { scope.launch { drawerState.open() } })
                    }

                    SettingScreenRoutes.SettingScreen.route-> {
                        SettingScreenTopBar(
                            title = "Setting",
                            onNavigationClick = { navController.popBackStack() })
                    }

                    SettingScreenRoutes.DataPrivacyScreen.route -> {
                        SettingScreenTopBar(
                            title = "Data and Privacy",
                            onNavigationClick = { navController.popBackStack() })
                    }

                    SettingScreenRoutes.AboutNotifyuScreen.route -> {
                        SettingScreenTopBar(
                            title = "About Notifyu",
                            onNavigationClick = { navController.popBackStack() })
                    }
                }
            },
            containerColor = Color.White
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                RootNavHost(navHostController = navController, mainViewModel = mainViewModel)
            }
        }
    }
}


@Composable
fun OrganizationOwned(
    organizations: List<Organization>,
    avatarList: List<Int>,
    isOrganizationOwned: Boolean,
    onOwnedClick: () -> Unit,
    onOrganizationOwnedClick: (Organization) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Column(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
                    .background(if (isOrganizationOwned) Color.Gray.copy(0.2f) else Color.Transparent)
                    .clickable { onOwnedClick() },
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Organization Owned",
                    fontSize = 12.sp,
                    color = Color.Black
                )

            }
        }
        item {
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }

        items(organizations) { organization ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOrganizationOwnedClick(organization) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(avatarList[organization.avatarIndex]),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(0.5f))

                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = organization.name, fontSize = 14.sp)
            }
        }
    }

}

@Composable
fun OrganizationJoined(
    avatarList: List<Int>,
    organizationsMemberOf: List<Organization>,
    isOrganizationJoined: Boolean,
    onJoined: () -> Unit,
    onOrganizationJoinedClicked: (Organization) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Column(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth()
                    .background(if (isOrganizationJoined) Color.Gray.copy(0.2f) else Color.Transparent)
                    .clickable { onJoined() },
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Organization Joined",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
        item {
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }
        items(organizationsMemberOf) { organizations ->
            Row(
                modifier = Modifier
                    .padding(start = 0.dp)
                    .clickable { onOrganizationJoinedClicked(organizations) }) {

                Image(
                    painter = painterResource(avatarList[organizations.avatarIndex]),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(0.5f))

                )
                Text(text = organizations.name, fontSize = 14.sp)
            }
        }
    }

}


