package com.example.appmuabandocu.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRelativeTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (60 * 1000)
    val diffInHours = diffInMillis / (60 * 60 * 1000)
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

    return when {
        diffInMinutes < 60 -> "$diffInMinutes phút trước"
        diffInHours < 24 -> "$diffInHours giờ trước"
        diffInDays < 7 -> "$diffInDays ngày trước"
        else -> {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(date)
        }
    }
}
