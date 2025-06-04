package com.notifyu.app.presentation.navigation.navgraph.auth

sealed class AuthScreenRoutes(val route: String) {
    data object AuthScreenRoot : AuthScreenRoutes(route = "AuthRoot")
    data object SignupScreen : AuthScreenRoutes(route = "SignupScreen")
    data object LoginScreen : AuthScreenRoutes(route = "LoginScreen")
    data object VerifyEmailScreen : AuthScreenRoutes(route = "VerifyEmailScreen")
    data object ResetPasswordScreen : AuthScreenRoutes(route = "ResetPasswordScreen")
}