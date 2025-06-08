package com.notifyu.app.presentation.navigation.navgraph.main

sealed class MainScreenRoutes(val route: String) {
    data object MainScreenRoot : MainScreenRoutes(route = "MainScreenRoot")
    data object HomeScreen : MainScreenRoutes(route = "HomeScreen")
//    data object OrganizationJoinedScreen : MainScreenRoutes(route = "OrganizationJoinedScreen")
//    data object OrganizationOwnedScreen : MainScreenRoutes(route = "OrganizationOwnedScreen")
    data object ChatScreen : MainScreenRoutes(route = "ChatScreen")
    data object CreateJoinOrgScreen : MainScreenRoutes(route = "CreateJoinOrgScreen")
}