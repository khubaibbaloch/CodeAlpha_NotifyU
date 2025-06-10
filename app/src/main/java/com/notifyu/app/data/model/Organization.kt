package com.notifyu.app.data.model



data class Organization(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val owner: String = "",
    val avatarIndex :Int = 0,
    val members: List<String> = emptyList(),
    val messages: List<Message> = emptyList(),
    val lastMessage: LastMessage? = null
)
data class Message(
    val content: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
data class LastMessage(
    val content: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val seenBy: List<String> = emptyList()
)




