package com.example.compoundingjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tradeNumber: Int,
    val date: String,
    val time: String,
    val timestamp: Long,
    val symbol: String,
    val direction: String,
    val timeframe: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val lotSize: Double,
    val riskAmount: Double,
    val rewardAmount: Double,
    val grossProfitLoss: Double,
    val commission: Double,
    val swap: Double,
    val netProfitLoss: Double,
    val withdrawalAmount: Double,
    val startingBalance: Double,
    val endingBalance: Double,
    val growthPercent: Double,
    val riskRewardRatio: Double,
    val rMultiple: Double,
    val status: String,
    val strategyName: String,
    val setupType: String,
    val emotionBefore: String,
    val emotionAfter: String,
    val mistakeMade: String,
    val lessonLearned: String,
    val notes: String,
    val beforeScreenshotPath: String?,
    val afterScreenshotPath: String?,
    val createdAt: Long,
    val updatedAt: Long
)
