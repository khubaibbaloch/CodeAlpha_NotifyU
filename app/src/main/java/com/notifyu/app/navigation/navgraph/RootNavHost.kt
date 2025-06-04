package com.notifyu.app.navigation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.notifyu.app.navigation.navgraph.auth.authNavGraph
import com.notifyu.app.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.navigation.navgraph.main.mainNavGraph
import com.notifyu.app.viewmodel.MainViewModel

@Composable
fun RootNavHost(navHostController: NavHostController,mainViewModel: MainViewModel) {
    NavHost(
        navController = navHostController,
        startDestination = MainScreenRoutes.MainScreenRoot.route,
        route = "Root"
    ) {
        authNavGraph(navController = navHostController,mainViewModel = mainViewModel)
        mainNavGraph(navController = navHostController, mainViewModel = mainViewModel)
    }
}