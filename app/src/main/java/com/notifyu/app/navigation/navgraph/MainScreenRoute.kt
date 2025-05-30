package com.notifyu.app.navigation.navgraph

sealed class MainScreenRoute(val route: String) {
    data object MainScreenRoot : MainScreenRoute(route = "MainScreenRoot")
    data object MainScreen : MainScreenRoute(route = "MainScreen")
    data object OrganizationJoinedScreen : MainScreenRoute(route = "OrganizationJoinedScreen")
    data object OrganizationOwnedScreen : MainScreenRoute(route = "OrganizationOwnedScreen")
    data object EventChatScreen : MainScreenRoute(route = "EventChatScreen")
}