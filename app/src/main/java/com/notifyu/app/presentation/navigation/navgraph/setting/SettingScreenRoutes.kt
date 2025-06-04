package com.notifyu.app.presentation.navigation.navgraph.setting

sealed class SettingScreenRoutes(val route: String) {
    data object SettingScreenRoot : SettingScreenRoutes("SettingScreenRoot")
    data object SettingScreen : SettingScreenRoutes("SettingScreen")
    data object DataPrivacyScreen : SettingScreenRoutes("DataPrivacyScreen")
    data object AboutNotifyuScreen : SettingScreenRoutes("AboutNotifyuScreen")
}