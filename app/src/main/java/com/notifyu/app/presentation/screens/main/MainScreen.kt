package com.notifyu.app.presentation.screens.main

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.NavController
import com.notifyu.app.data.model.Organization
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.setting.SettingScreenRoutes
import com.notifyu.app.presentation.screens.components.ConfirmationDialog
import com.notifyu.app.presentation.screens.main.components.JoinCreateOrgBottomSheet
import com.notifyu.app.presentation.screens.main.components.SettingScreenTopBar
import com.notifyu.app.presentation.screens.main.components.getAvatarList
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    startDestination: String,
) {

    // AFTER MVVM
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isMenuExpanded = remember { mutableStateOf(false) }
    var showLogoutDialog = remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }


    val currentUser by mainViewModel.currentUser.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()
    val organizations by mainViewModel.organizationsOwned.collectAsState()
    val organizationId by mainViewModel.onOrganizationsClick.collectAsState()
    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    val selectedOrganization = remember(organizations, organizationsMemberOf, organizationId) {
        organizations.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }
    }
    val isOwner = remember(selectedOrganization, currentUser) {
        selectedOrganization?.owner == currentUser?.uid
    }


    val avatarList = getAvatarList()


    ModalNavigationDrawer(
        modifier = Modifier
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars),
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
                        text = "${currentUser?.email}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(
                                    SettingScreenRoutes.SettingScreen.route
                                )
                            }
                        },
                        modifier = Modifier
                            .size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_setting),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)

                        )
                    }

                }
                Spacer(modifier = Modifier.padding(vertical = 24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clickable { showBottomSheet = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Add organization",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 24.dp))

                DrawerOrganizationList(
                    title = "Organization Owned",
                    organizations = organizations,
                    avatarList = avatarList,
                    onOrganizationClick = { organization ->
                        scope.launch {
                            drawerState.close()
                            mainViewModel.updateOnOrganizationClick(organization.id)
                            navController.navigate(MainScreenRoutes.ChatScreen.route)
                        }
                    }
                )

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                DrawerOrganizationList(
                    title = "Organization Joined",
                    organizations = organizationsMemberOf,
                    avatarList = avatarList,
                    onOrganizationClick = { organization ->
                        scope.launch {
                            drawerState.close()
                            mainViewModel.updateOnOrganizationClick(organization.id)
                            navController.navigate(MainScreenRoutes.ChatScreen.route)
                        }
                    }
                )


            }

//            if (showBottomSheet) {
                JoinCreateOrgBottomSheet(
                    onCreateClick = {
                        scope.launch {
                            showBottomSheet = false
                            drawerState.close()
                            mainViewModel.updateAddOrg(true)
                            if (currentRoute != MainScreenRoutes.CreateJoinOrgScreen.route) {
                                navController.navigate(MainScreenRoutes.CreateJoinOrgScreen.route)
                            }
                        }

                    },
                    onJoinClick = {
                        scope.launch {
                            showBottomSheet = false
                            drawerState.close()
                            mainViewModel.updateAddOrg(false)
                            if (currentRoute != MainScreenRoutes.CreateJoinOrgScreen.route) {
                                navController.navigate(MainScreenRoutes.CreateJoinOrgScreen.route)
                            }
                        }

                    },
                    showSheet = showBottomSheet,
                    onDismissRequest = { showBottomSheet = false }
                )
//            }


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
                            onLeaveClick = {
                                isMenuExpanded.value = false
                                showLogoutDialog.value = true
//
                            },
                            showLogoutDialog = showLogoutDialog,
                            onConfirm = {
                                if (!isOwner) {
                                    mainViewModel.authRemoveMemberFromOrganization(
                                        currentUser?.uid ?: ""
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

                    MainScreenRoutes.HomeScreen.route -> {
                        MainScreenTopBar(
                            title = "Notifyu",
                            onNavigationClick = { scope.launch { drawerState.open() } },
                            hasElevation = false,
                            isDropDownMenuClicked = isMenuExpanded.value,
                            onMoreVertClick = { isMenuExpanded.value = true },
                            onDismissRequest = { isMenuExpanded.value = false },
                            onLeaveClick = {
                                isMenuExpanded.value = false
                                navController.navigate(SettingScreenRoutes.SettingScreen.route)
                            }
                        )
                    }

                    MainScreenRoutes.CreateJoinOrgScreen.route -> {
                        MainScreenTopBar(
                            title = "Notifyu",
                            onNavigationClick = { scope.launch { drawerState.open() } },
                            hasElevation = true,
                            isDropDownMenuClicked = isMenuExpanded.value,
                            onMoreVertClick = { isMenuExpanded.value = true },
                            onDismissRequest = { isMenuExpanded.value = false },
                            onLeaveClick = {
                                isMenuExpanded.value = false
                                navController.navigate(SettingScreenRoutes.SettingScreen.route)
                            }
                        )
                    }

                    SettingScreenRoutes.SettingScreen.route -> {
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

                    AuthScreenRoutes.LoginScreen.route,
                    AuthScreenRoutes.SignupScreen.route,
                    AuthScreenRoutes.ResetPasswordScreen.route,
                    AuthScreenRoutes.VerifyEmailScreen.route,
                        -> {
                    }

                    else -> {
                        MainScreenTopBar(
                            title = "Notifyu",
                            onNavigationClick = { scope.launch { drawerState.open() } },
                            hasElevation = false,
                            isDropDownMenuClicked = isMenuExpanded.value,
                            onMoreVertClick = { isMenuExpanded.value = true },
                            onDismissRequest = { isMenuExpanded.value = false },
                            onLeaveClick = {
                                isMenuExpanded.value = false
                                navController.navigate(SettingScreenRoutes.SettingScreen.route)
                            })
                    }
                }
            },
            containerColor = Color.White,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                RootNavHost(
                    navHostController = navController,
                    mainViewModel = mainViewModel,
                    startDestination = startDestination
                )
            }
        }
    }
}


@Composable
fun DrawerOrganizationList(
    title: String,
    organizations: List<Organization>,
    avatarList: List<Int>,
    onOrganizationClick: (Organization) -> Unit,
) {
    // Step 1: Sort and Limit to 5
    val sortedOrgs = organizations
        .sortedByDescending { it.lastMessage?.timestamp ?: 0L }
        .take(5)

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Column(
                modifier = Modifier
                    .height(30.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }

        item {
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }

        items(sortedOrgs) { organization ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOrganizationClick(organization) }
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
