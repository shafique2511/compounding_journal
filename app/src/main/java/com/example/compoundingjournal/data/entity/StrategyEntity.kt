package com.example.compoundingjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strategies")
data class StrategyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strategyName: String,
    val marketType: String,
    val timeframe: String,
    val entryRules: String,
    val exitRules: String,
    val stopLossRules: String,
    val takeProfitRules: String,
    val riskRules: String,
    val exampleScreenshotPath: String?,
    val notes: String,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
