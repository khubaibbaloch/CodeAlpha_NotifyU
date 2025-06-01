package com.notifyu.app.navigation.navgraph

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.notifyu.app.ui.screens.auth.LoginScreen
import com.notifyu.app.ui.screens.auth.ResetPasswordScreen
import com.notifyu.app.ui.screens.auth.SignupScreen
import com.notifyu.app.ui.screens.auth.VerifyEmailScreen
import com.notifyu.app.viewmodel.MainViewModel

fun NavGraphBuilder.authNavHost(navController: NavHostController,mainViewModel: MainViewModel) {
    navigation(
        startDestination = AuthScreenRoute.SignupScreen.route,
        route = AuthScreenRoute.AuthScreenRoot.route
    ) {
        composable(AuthScreenRoute.SignupScreen.route) {
            SignupScreen(navController,mainViewModel)
        }
        composable(AuthScreenRoute.LoginScreen.route) {
            LoginScreen(navController,mainViewModel)
        }
        composable(AuthScreenRoute.VerifyEmailScreen.route) {
            VerifyEmailScreen(navController,mainViewModel)
        }
        composable(AuthScreenRoute.ResetPasswordScreen.route) {
            ResetPasswordScreen(navController=navController, mainViewModel = mainViewModel)
        }
    }
}