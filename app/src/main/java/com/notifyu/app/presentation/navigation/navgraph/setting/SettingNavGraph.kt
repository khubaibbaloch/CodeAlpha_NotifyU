package com.notifyu.app.presentation.navigation.navgraph.setting

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.presentation.screens.setting.AboutNotifyuScreen
import com.notifyu.app.presentation.screens.setting.DataPrivacyScreen
import com.notifyu.app.presentation.screens.setting.SettingScreen
import com.notifyu.app.presentation.viewmodel.MainViewModel


fun NavGraphBuilder.settingNavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    navigation(
        startDestination = SettingScreenRoutes.SettingScreen.route  ,
        route = SettingScreenRoutes.SettingScreenRoot.route
    ){
        composable(SettingScreenRoutes.SettingScreen.route) {
            SettingScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(SettingScreenRoutes.DataPrivacyScreen.route) {
            DataPrivacyScreen()
        }
        composable(SettingScreenRoutes.AboutNotifyuScreen.route) {
            AboutNotifyuScreen()
        }
    }
}