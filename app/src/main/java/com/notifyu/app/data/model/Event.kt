package com.notifyu.app.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val type: String = "" // e.g., "Exam", "Seminar", "Notice"
)