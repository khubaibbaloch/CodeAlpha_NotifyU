package com.notifyu.app.presentation.screens.chat.components.state

import com.notifyu.app.data.model.Message

sealed class DisplayItem {
    data class DateHeader(val date: String) : DisplayItem()
    data class ChatMessage(val message: Message) : DisplayItem()
}
