package com.notifyu.app.presentation.navigation.navgraph

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.auth.authNavGraph
import com.notifyu.app.presentation.navigation.navgraph.main.mainNavGraph
import com.notifyu.app.presentation.navigation.navgraph.setting.settingNavGraph
import com.notifyu.app.presentation.viewmodel.MainViewModel
import androidx.compose.runtime.*
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes

@Composable
fun RootNavHost(navHostController: NavHostController, mainViewModel: MainViewModel,startDestination: String) {
    //val currentUser by mainViewModel.currentUser.collectAsState()
   // val startDestination =  if (currentUser != null && currentUser!!.isEmailVerified) MainScreenRoutes.MainScreenRoot.route else AuthScreenRoutes.AuthScreenRoot.route

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        route = "Root",
        enterTransition = {
            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(160)
            ) + fadeIn(animationSpec = tween(100))
        },
        exitTransition = {
            scaleOut(
                targetScale = .95f,
                animationSpec = tween(160)
            ) + fadeOut(animationSpec = tween(150))
        },
        popEnterTransition = {
            scaleIn(initialScale = .95f, animationSpec = tween(160)) + fadeIn(
                animationSpec = tween(
                    100
                )
            )
        },
        popExitTransition = {
            scaleOut(
                targetScale = .95f,
                animationSpec = tween(160)
            ) + fadeOut(animationSpec = tween(150))
        }
    ) {
        authNavGraph(navController = navHostController, mainViewModel = mainViewModel)
        mainNavGraph(navController = navHostController, mainViewModel = mainViewModel)
        settingNavGraph(navController = navHostController, mainViewModel = mainViewModel)
    }
}