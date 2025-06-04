package com.notifyu.app.navigation.navgraph.main

sealed class MainScreenRoutes(val route: String) {
    data object MainScreenRoot : MainScreenRoutes(route = "MainScreenRoot")
    data object HomeScreen : MainScreenRoutes(route = "HomeScreen")
    data object OrganizationJoinedScreen : MainScreenRoutes(route = "OrganizationJoinedScreen")
    data object OrganizationOwnedScreen : MainScreenRoutes(route = "OrganizationOwnedScreen")
    data object ChatScreen : MainScreenRoutes(route = "ChatScreen")
    data object CreateJoinOrgScreen : MainScreenRoutes(route = "CreateJoinOrgScreen")
    data object SettingScreen : MainScreenRoutes(route = "SettingScreen")
    data object DataPrivacyScreen : MainScreenRoutes(route = "DataPrivacyScreen")
    data object AboutNotifyuScreen : MainScreenRoutes(route = "AboutNotifyuScreen")
}