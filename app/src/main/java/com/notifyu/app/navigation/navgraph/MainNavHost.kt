package com.notifyu.app.navigation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.ui.screens.events.EventChatScreen
import com.notifyu.app.ui.screens.main.MainScreen
import com.notifyu.app.ui.screens.main.OrganizationJoinedScreen
import com.notifyu.app.ui.screens.main.OrganizationOwnedScreen


fun NavGraphBuilder.mainNavHost(navController: NavController) {
    navigation(
        startDestination = MainScreenRoute.OrganizationOwnedScreen.route,
        route = MainScreenRoute.MainScreenRoot.route
    ) {
//        composable(MainScreenRoute.MainScreen.route) {
//            MainScreen(navController = navController)
//        }
        composable(MainScreenRoute.OrganizationJoinedScreen.route) {
            OrganizationJoinedScreen(navController = navController)
        }
        composable(MainScreenRoute.OrganizationOwnedScreen.route) {
            OrganizationOwnedScreen(navController = navController)
        }
        composable(MainScreenRoute.EventChatScreen.route) {
            EventChatScreen()
        }
    }
}