package com.notifyu.app.navigation.navgraph.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.ui.screens.auth.LoginScreen
import com.notifyu.app.ui.screens.auth.ResetPasswordScreen
import com.notifyu.app.ui.screens.auth.SignupScreen
import com.notifyu.app.ui.screens.auth.VerifyEmailScreen
import com.notifyu.app.viewmodel.MainViewModel

fun NavGraphBuilder.authNavGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    navigation(
        startDestination = AuthScreenRoutes.SignupScreen.route,
        route = AuthScreenRoutes.AuthScreenRoot.route
    ) {
        composable(AuthScreenRoutes.SignupScreen.route) {
            SignupScreen(navController,mainViewModel)
        }
        composable(AuthScreenRoutes.LoginScreen.route) {
            LoginScreen(navController,mainViewModel)
        }
        composable(AuthScreenRoutes.VerifyEmailScreen.route) {
            VerifyEmailScreen(navController,mainViewModel)
        }
        composable(AuthScreenRoutes.ResetPasswordScreen.route) {
            ResetPasswordScreen(navController=navController, mainViewModel = mainViewModel)
        }
    }
}