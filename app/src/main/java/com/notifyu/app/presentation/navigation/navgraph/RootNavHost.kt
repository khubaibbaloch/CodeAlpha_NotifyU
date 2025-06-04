package com.notifyu.app.presentation.navigation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.auth.authNavGraph
import com.notifyu.app.presentation.navigation.navgraph.main.mainNavGraph
import com.notifyu.app.presentation.navigation.navgraph.setting.settingNavGraph
import com.notifyu.app.presentation.viewmodel.MainViewModel

@Composable
fun RootNavHost(navHostController: NavHostController, mainViewModel: MainViewModel) {
    NavHost(
        navController = navHostController,
        startDestination = AuthScreenRoutes.AuthScreenRoot.route,
        route = "Root"
    ) {
        authNavGraph(navController = navHostController, mainViewModel = mainViewModel)
        mainNavGraph(navController = navHostController, mainViewModel = mainViewModel)
        settingNavGraph(navController = navHostController, mainViewModel = mainViewModel)
    }
}