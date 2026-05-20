package com.example.compoundingjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val initialBalance: Double,
    val currency: String,
    val timezoneOffset: String,
    val dateFormat: String,
    val timeFormat: String,
    val defaultTimeframe: String,
    val defaultSymbol: String,
    val defaultCommission: Double,
    val defaultSwap: Double,
    val themeMode: String,
    val accentColor: String,
    val autoTradeNumber: Boolean = true
)
