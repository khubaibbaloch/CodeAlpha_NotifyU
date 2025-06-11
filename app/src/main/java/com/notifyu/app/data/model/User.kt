package com.notifyu.app.data.model

// This is a Kotlin data class named 'User'
data class User(
    // 'uid' stores the unique ID of the user, default is an empty string
    val uid: String = "",

    // 'email' stores the user's email address, default is an empty string
    val email: String = "",

    // 'fcmToken' is the Firebase Cloud Messaging token used for push notifications, default is an empty string
    val fcmToken: String = "",
)

