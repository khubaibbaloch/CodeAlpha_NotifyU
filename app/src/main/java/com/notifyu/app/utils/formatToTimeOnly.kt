package com.notifyu.app.utils

import android.text.format.DateFormat
import java.util.Calendar

fun formatToTimeOnly(timestamp: Long): String {
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    return DateFormat.format("h:mm a", msgTime).toString()
}
