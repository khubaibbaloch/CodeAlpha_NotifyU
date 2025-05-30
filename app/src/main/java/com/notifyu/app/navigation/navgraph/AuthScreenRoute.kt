package com.notifyu.app.navigation.navgraph

sealed class AuthScreenRoute(val route: String) {
    data object AuthScreenRoot : AuthScreenRoute(route = "AuthRoot")
    data object SignupScreen : AuthScreenRoute(route = "SignupScreen")
    data object LoginScreen : AuthScreenRoute(route = "LoginScreen")
    data object VerifyEmailScreen : AuthScreenRoute(route = "VerifyEmailScreen")
    data object ResetPasswordScreen : AuthScreenRoute(route = "ResetPasswordScreen")
}