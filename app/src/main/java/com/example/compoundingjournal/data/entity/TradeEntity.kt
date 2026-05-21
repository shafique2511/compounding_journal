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
    val updatedAt: Long,

    // Checklist fields
    val checklistTrendConfirmed: Boolean = false,
    val checklistKeyLevelConfirmed: Boolean = false,
    val checklistEntryReasonConfirmed: Boolean = false,
    val checklistStopLossPlanned: Boolean = false,
    val checklistTakeProfitPlanned: Boolean = false,
    val checklistRiskAccepted: Boolean = false,
    val checklistNoRevengeTrade: Boolean = false,
    val checklistNoOverlot: Boolean = false,
    val checklistNewsChecked: Boolean = false,
    val checklistEmotionStable: Boolean = false,
    val checklistScore: Double = 0.0,
    val checklistStatus: String = "",

    // Mistake / rule fields
    val mistakeTags: String = "",
    val ruleFollowed: String = "",
    val ruleBrokenNotes: String = "",

    // Quality score fields
    val tradeQualityScore: Double = 0.0,
    val tradeQualityGrade: String = "",

    // Review fields
    val reviewCompleted: Boolean = false,
    val reviewDate: String = "",
    val reviewNotes: String = ""
)
