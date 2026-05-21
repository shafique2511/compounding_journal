package com.example.compoundingjournal.presentation.addtrade

data class TradeFormState(
    val symbol: String = "",
    val direction: String = "BUY",
    val timeframe: String = "H1",
    val date: String = "",
    val time: String = "",
    val status: String = "RUNNING",
    val entryPrice: String = "",
    val stopLoss: String = "",
    val takeProfit: String = "",
    val lotSize: String = "",
    val startingBalance: String = "",
    val grossProfitLoss: String = "",
    val commission: String = "",
    val swap: String = "",
    val withdrawalAmount: String = "",
    val strategyName: String = "",
    val setupType: String = "",
    val emotionBefore: String = "",
    val emotionAfter: String = "",
    val mistakeMade: String = "",
    val lessonLearned: String = "",
    val notes: String = "",
    val beforeScreenshotPath: String? = null,
    val afterScreenshotPath: String? = null,
    
    // Phase 3 Checklist fields
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
    val checklistStatus: String = "Plan Warning",
    
    // Phase 3 Rule tracking
    val ruleFollowed: String = "YES",
    val ruleBrokenNotes: String = "",
    
    // Other Phase 1/2 fields to preserve
    val mistakeTags: String = "",
    val tradeQualityScore: Double = 0.0,
    val tradeQualityGrade: String = "",
    val reviewCompleted: Boolean = false,
    val reviewDate: String = "",
    val reviewNotes: String = "",
    
    // Calculated values (as strings for display)
    val netProfitLoss: String = "0.00",
    val endingBalance: String = "0.00",
    val growthPercent: String = "0.00",
    val riskRewardRatio: String = "0.00",
    val rMultiple: String = "0.00",
    
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val showChecklistWarning: Boolean = false
)
