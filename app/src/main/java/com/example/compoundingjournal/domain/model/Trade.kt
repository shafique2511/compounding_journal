package com.example.compoundingjournal.domain.model

data class Trade(
    val id: Long,
    val symbol: String,
    val entryPrice: Double,
    val exitPrice: Double?,
    val quantity: Double,
    val type: String,
    val timestamp: Long
)
