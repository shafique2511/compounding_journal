package com.example.compoundingjournal.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun getCurrentDeviceDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDeviceTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long, pattern: String = "HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
