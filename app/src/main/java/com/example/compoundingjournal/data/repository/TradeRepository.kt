package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

interface TradeRepository {
    fun getAllTrades(): Flow<List<TradeEntity>>
    suspend fun insertTrade(trade: TradeEntity)
    suspend fun getTradeById(id: Long): TradeEntity?
}
