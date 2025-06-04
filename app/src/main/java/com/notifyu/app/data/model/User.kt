package com.notifyu.app.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val fcmToken: String = "",
    val avatarIndex: Int = 0,
    val selectedScreen: String = SelectedScreen.None.value
)
sealed class SelectedScreen(val value: String) {
    data object Owned : SelectedScreen("owned")
    data object Joined : SelectedScreen("joined")
    data object None : SelectedScreen("none")
}
