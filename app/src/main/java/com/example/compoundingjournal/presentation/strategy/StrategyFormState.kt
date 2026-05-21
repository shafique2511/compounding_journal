package com.example.compoundingjournal.presentation.strategy

data class StrategyFormState(
    val strategyName: String = "",
    val marketType: String = "",
    val timeframe: String = "",
    val entryRules: String = "",
    val exitRules: String = "",
    val stopLossRules: String = "",
    val takeProfitRules: String = "",
    val riskRules: String = "",
    val exampleScreenshotPath: String? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
