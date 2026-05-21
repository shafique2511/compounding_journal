package com.example.compoundingjournal.utils

import androidx.compose.ui.graphics.Color

object ColorUtils {
    fun getStatusColor(status: String): Color {
        return when (status.uppercase()) {
            "WIN" -> Color(0xFF4CAF50)
            "LOSS" -> Color(0xFFF44336)
            "BREAKEVEN" -> Color.Gray
            "RUNNING" -> Color(0xFF2196F3)
            "CANCELLED" -> Color.LightGray
            else -> Color.Black
        }
    }

    fun getGradeColor(grade: String): Color {
        return when (grade) {
            "A+", "A" -> Color(0xFF4CAF50)
            "B" -> Color(0xFF8BC34A)
            "C" -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
    }
}
