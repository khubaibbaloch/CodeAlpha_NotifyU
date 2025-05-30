package com.notifyu.app.navigation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun RootNavHost(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = AuthScreenRoute.AuthScreenRoot.route,
        route = "Root"
    ) {
        authNavHost(navController = navHostController)
        mainNavHost(navController = navHostController)
    }
}