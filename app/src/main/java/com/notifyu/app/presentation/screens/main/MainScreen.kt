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


@OptIn(ExperimentalMaterial3Api::class) // Opt-in to experimental Material3 API
@Composable
fun MainScreen(
    navController: NavHostController,        // Navigation controller to handle app screen navigation
    mainViewModel: MainViewModel,            // ViewModel holding app logic and state
    startDestination: String,                // Initial screen route when navigation starts
) {
    // MVVM-related context
    val context = LocalContext.current                        // Get the current context
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Drawer state (opened or closed)
    val scope = rememberCoroutineScope()                      // Scope for launching coroutines

    var isMenuExpanded = remember { mutableStateOf(false) }   // Whether the dropdown menu is expanded
    var showLogoutDialog = remember { mutableStateOf(false) } // Whether to show the logout confirmation dialog
    var showBottomSheet by remember { mutableStateOf(false) } // Whether to show the join/create org bottom sheet

    // Collecting UI state from the ViewModel
    val currentUser by mainViewModel.currentUser.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()
    val organizations by mainViewModel.organizationsOwned.collectAsState()
    val organizationId by mainViewModel.onOrganizationsClick.collectAsState()
    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route // Current screen route

    // Get the selected organization based on its ID
    val selectedOrganization = remember(organizations, organizationsMemberOf, organizationId) {
        organizations.find { it.id == organizationId } ?: organizationsMemberOf.find { it.id == organizationId }
    }

    // Check if current user is the owner of the selected organization
    val isOwner = remember(selectedOrganization, currentUser) {
        selectedOrganization?.owner == currentUser?.uid
    }

    // Get list of avatars (not shown in this code, assumed to be defined elsewhere)
    val avatarList = getAvatarList()

    // Navigation drawer layout
    ModalNavigationDrawer(
        modifier = Modifier
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars), // Adjust for system bars
        drawerState = drawerState,               // Attach state
        gesturesEnabled = drawerState.isOpen,    // Enable gestures only when open
        drawerContent = {
            // Drawer UI
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                // Header row showing user email and settings icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${currentUser?.email}", // Show current user's email
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
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_setting),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(vertical = 24.dp))

                // Option to add organization
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

                // List of organizations owned by user
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

                // List of organizations the user is a member of
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

            // Bottom sheet to join or create org
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
        }
    ) {
        // Main screen content inside Scaffold
        Scaffold(
            topBar = {
                // Conditionally set top bar based on current screen
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

                    MainScreenRoutes.HomeScreen.route,
                    MainScreenRoutes.CreateJoinOrgScreen.route -> {
                        MainScreenTopBar(
                            title = "Notifyu",
                            onNavigationClick = { scope.launch { drawerState.open() } },
                            hasElevation = currentRoute == MainScreenRoutes.CreateJoinOrgScreen.route,
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
                    AuthScreenRoutes.VerifyEmailScreen.route -> {
                        // Do nothing – no top bar for these routes
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
                    .padding(top = innerPadding.calculateTopPadding()) // Adjust for top padding
                    .windowInsetsPadding(WindowInsets.navigationBars) // Adjust for navigation bar
            ) {
                RootNavHost( // Root navigator that handles all routes
                    navHostController = navController,
                    mainViewModel = mainViewModel,
                    startDestination = startDestination
                )
            }
        }
    }
}


// Used in MainScreen to display list of organizations
@Composable
fun DrawerOrganizationList(
    title: String,
    organizations: List<Organization>,
    avatarList: List<Int>,
    onOrganizationClick: (Organization) -> Unit,
) {
    // Sort organizations by the most recent message timestamp and limit to 5 entries
    val sortedOrgs = organizations
        .sortedByDescending { it.lastMessage?.timestamp ?: 0L }
        .take(5)

    // LazyColumn to display the list
    LazyColumn(modifier = Modifier.fillMaxWidth()) {

        // Header title
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

        // Spacer between title and list items
        item {
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }

        // List items for each organization
        items(sortedOrgs) { organization ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOrganizationClick(organization) } // Handle click event
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display organization avatar
                Image(
                    painter = painterResource(avatarList[organization.avatarIndex]),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(0.5f))
                )

                // Spacer between image and text
                Spacer(modifier = Modifier.width(8.dp))

                // Display organization name
                Text(text = organization.name, fontSize = 14.sp)
            }
        }
    }
}
