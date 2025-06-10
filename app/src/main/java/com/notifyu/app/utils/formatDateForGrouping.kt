package com.notifyu.app.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


fun formatDateForGrouping(timestamp: Long): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val messageDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        when (messageDate) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> messageDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        }
    } else {
        // Fallback for older versions
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
