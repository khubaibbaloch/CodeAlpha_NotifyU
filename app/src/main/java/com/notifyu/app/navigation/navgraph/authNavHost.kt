package com.notifyu.app.navigation.navgraph

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.ui.screens.auth.LoginScreen
import com.notifyu.app.ui.screens.auth.ResetPasswordScreen
import com.notifyu.app.ui.screens.auth.SignupScreen
import com.notifyu.app.ui.screens.auth.VerifyEmailScreen

fun NavGraphBuilder.authNavHost(navController: NavController) {
    navigation(
        startDestination = AuthScreenRoute.SignupScreen.route,
        route = AuthScreenRoute.AuthScreenRoot.route
    ) {
        composable(AuthScreenRoute.SignupScreen.route) {
            SignupScreen(navController)
        }
        composable(AuthScreenRoute.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(AuthScreenRoute.VerifyEmailScreen.route) {
            VerifyEmailScreen()
        }
        composable(AuthScreenRoute.ResetPasswordScreen.route) {
            ResetPasswordScreen()
        }
    }
}