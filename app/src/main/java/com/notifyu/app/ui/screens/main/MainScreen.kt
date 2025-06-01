package com.notifyu.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.notifyu.app.R
import com.notifyu.app.navigation.navgraph.MainScreenRoute
import com.notifyu.app.navigation.navgraph.RootNavHost
import com.notifyu.app.ui.screens.components.EventChatScreenTopBar
import com.notifyu.app.ui.screens.components.MainScreenTopBar
import com.notifyu.app.ui.theme.BackgroundColor
import com.notifyu.app.ui.theme.PrimaryColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.notifyu.app.ui.theme.SurfaceColor
import com.notifyu.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isOrganizationOwned = remember { mutableStateOf(false) }
    val isOrganizationJoined = remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var showBottomSheet by remember { mutableStateOf(false) }



    ModalNavigationDrawer(
        modifier = Modifier.statusBarsPadding(),
        drawerState = drawerState,
        gesturesEnabled = true,
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
                        text = "${mainViewModel.auth.currentUser!!.email}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_setting),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
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
                    isOrganizationOwned = isOrganizationOwned.value,
                    onOwnedClick = {
                        navController.navigate(MainScreenRoute.OrganizationOwnedScreen.route)
                        isOrganizationOwned.value = true
                        isOrganizationJoined.value = false
                    }, onOrganizationOwnedClick = {
                        navController.navigate(MainScreenRoute.EventChatScreen.route)
                    })
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                OrganizationJoined(
                    isOrganizationJoined = isOrganizationJoined.value,
                    onJoined = {
                        navController.navigate(MainScreenRoute.OrganizationJoinedScreen.route)
                        isOrganizationJoined.value = true
                        isOrganizationOwned.value = false
                    },
                    onOrganizationJoinedClicked = {
                        navController.navigate(MainScreenRoute.EventChatScreen.route)
                    })

            }

            if (showBottomSheet) {
                BottomSheet(
                    showSheet = showBottomSheet,
                    onDismissRequest = { showBottomSheet = false }
                )
            }


        }) {

        Scaffold(
            topBar = {
                when (currentRoute) {
                    MainScreenRoute.EventChatScreen.route -> {
                        EventChatScreenTopBar()
                    }

                    MainScreenRoute.OrganizationOwnedScreen.route -> {
                        MainScreenTopBar(title = if (isOrganizationJoined.value) "Joined" else "Owned")
                    }

                    MainScreenRoute.HomeScreen.route -> {
                        MainScreenTopBar(title = "Notifyu")
                    }
                }
            },
            bottomBar = {
                BottomSheet(
                    showSheet = false,
                    onDismissRequest = { showBottomSheet = false }
                )
            },
            containerColor = Color.Gray.copy(0.1f)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                RootNavHost(navHostController = navController, mainViewModel = mainViewModel)
            }
        }
    }
}


@Composable
fun OrganizationOwned(
    onOwnedClick: () -> Unit,
    isOrganizationOwned: Boolean,
    onOrganizationOwnedClick: () -> Unit,
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
        items(5) {
            Row(
                modifier = Modifier
                    .padding(start = 0.dp)
                    .clickable { onOrganizationOwnedClick() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Text(text = "Add organization", fontSize = 14.sp)
            }
        }
    }

}

@Composable
fun OrganizationJoined(
    isOrganizationJoined: Boolean,
    onJoined: () -> Unit,
    onOrganizationJoinedClicked: () -> Unit,
) {
    LazyColumn {
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
        items(5) {
            Row(
                modifier = Modifier
                    .padding(start = 0.dp)
                    .clickable { onOrganizationJoinedClicked() }) {

                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Text(text = "Add organization", fontSize = 14.sp)
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )


    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RectangleShape,
            dragHandle = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add organization", fontSize = 18.sp)
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(30.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = SurfaceColor.copy(
                                0.3f
                            )
                        ),
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {},
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = SurfaceColor.copy(
                                0.3f
                            )
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create new organization", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(start = 55.dp))
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {},
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = SurfaceColor.copy(
                                0.3f
                            )
                        )
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_left_arrow), contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join existing organization", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(start = 55.dp))
                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = {}) {
                    Text(text = "Cancel", color = PrimaryColor)
                }
            }
        }
    }
}
