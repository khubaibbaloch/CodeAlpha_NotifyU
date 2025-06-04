package com.notifyu.app.navigation.navgraph.main

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.ui.screens.chat.EventChatScreen
import com.notifyu.app.ui.screens.main.CreateJoinOrgScreen
import com.notifyu.app.ui.screens.main.HomeScreen
import com.notifyu.app.ui.screens.main.OrganizationJoinedScreen
import com.notifyu.app.ui.screens.main.OrganizationOwnedScreen
import com.notifyu.app.ui.screens.setting.AboutNotifyuScreen
import com.notifyu.app.ui.screens.setting.DataPrivacyScreen
import com.notifyu.app.ui.screens.setting.SettingScreen
import com.notifyu.app.viewmodel.MainViewModel


fun NavGraphBuilder.mainNavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    navigation(
        startDestination = MainScreenRoutes.HomeScreen.route,
        route = MainScreenRoutes.MainScreenRoot.route
    ) {
        composable(MainScreenRoutes.HomeScreen.route) {
            HomeScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(MainScreenRoutes.OrganizationJoinedScreen.route) {
            OrganizationJoinedScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(MainScreenRoutes.OrganizationOwnedScreen.route) {
            OrganizationOwnedScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(MainScreenRoutes.ChatScreen.route) {
            EventChatScreen(navController, mainViewModel)
        }
        composable(MainScreenRoutes.CreateJoinOrgScreen.route) {
            CreateJoinOrgScreen(navController, mainViewModel)
        }
        composable(MainScreenRoutes.SettingScreen.route) {
            SettingScreen(navController,mainViewModel)
        }
        composable(MainScreenRoutes.DataPrivacyScreen.route) {
            DataPrivacyScreen()
        }
        composable(MainScreenRoutes.AboutNotifyuScreen.route) {
            AboutNotifyuScreen()
        }
    }
}