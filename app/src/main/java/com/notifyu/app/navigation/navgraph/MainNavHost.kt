package com.notifyu.app.navigation.navgraph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.ui.screens.events.EventChatScreen
import com.notifyu.app.ui.screens.main.CreateJoinOrgScreen
import com.notifyu.app.ui.screens.main.HomeScreen
import com.notifyu.app.ui.screens.main.OrganizationJoinedScreen
import com.notifyu.app.ui.screens.main.OrganizationOwnedScreen
import com.notifyu.app.viewmodel.MainViewModel


fun NavGraphBuilder.mainNavHost(navController: NavHostController,mainViewModel: MainViewModel) {
    navigation(
        startDestination = MainScreenRoute.HomeScreen.route,
        route = MainScreenRoute.MainScreenRoot.route
    ) {
        composable(MainScreenRoute.HomeScreen.route) {
            HomeScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(MainScreenRoute.OrganizationJoinedScreen.route) {
            OrganizationJoinedScreen(navController = navController)
        }
        composable(MainScreenRoute.OrganizationOwnedScreen.route) {
            OrganizationOwnedScreen(navController = navController,mainViewModel = mainViewModel)
        }
        composable(MainScreenRoute.EventChatScreen.route) {
            EventChatScreen(navController,mainViewModel)
        }
        composable(MainScreenRoute.CreateJoinOrgScreen.route) {
            CreateJoinOrgScreen(navController,mainViewModel)
        }
    }
}