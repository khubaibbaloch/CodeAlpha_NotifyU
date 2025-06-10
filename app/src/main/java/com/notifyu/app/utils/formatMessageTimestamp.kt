package com.notifyu.app.utils

import android.text.format.DateFormat
import java.util.Calendar

fun formatMessageTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        isSameDay(now, msgTime) -> {
            // Today: Show time
            DateFormat.format("h:mm a", msgTime).toString()
        }

        isYesterday(now, msgTime) -> {
            "Yesterday"
        }

        else -> {
            // Older: Show full date
            DateFormat.format("MMM d, yyyy", msgTime).toString()
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, msgTime: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, msgTime)
}
