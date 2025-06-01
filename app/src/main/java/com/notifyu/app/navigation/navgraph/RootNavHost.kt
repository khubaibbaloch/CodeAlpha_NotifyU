package com.notifyu.app.navigation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.notifyu.app.ui.screens.main.MainScreen
import com.notifyu.app.viewmodel.MainViewModel

@Composable
fun RootNavHost(navHostController: NavHostController,mainViewModel: MainViewModel) {
    NavHost(
        navController = navHostController,
        startDestination = MainScreenRoute.MainScreenRoot.route,
        route = "Root"
    ) {
        authNavHost(navController = navHostController,mainViewModel = mainViewModel)
        mainNavHost(navController = navHostController, mainViewModel = mainViewModel)
    }
}