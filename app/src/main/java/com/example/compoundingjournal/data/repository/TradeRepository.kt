package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

interface TradeRepository {
    fun getAllTrades(): Flow<List<TradeEntity>>
    suspend fun getTradeById(id: Long): TradeEntity?
    suspend fun insertTrade(trade: TradeEntity)
    suspend fun updateTrade(trade: TradeEntity)
    suspend fun deleteTrade(trade: TradeEntity)
    fun getTradesByDateRange(startTime: Long, endTime: Long): Flow<List<TradeEntity>>
    fun getTradesBySymbol(symbol: String): Flow<List<TradeEntity>>
    fun getTradesByTimeframe(timeframe: String): Flow<List<TradeEntity>>
    fun getTradesByStatus(status: String): Flow<List<TradeEntity>>
    fun getTradesByStrategy(strategy: String): Flow<List<TradeEntity>>
    suspend fun getLastTrade(): TradeEntity?
    suspend fun deleteAllTrades()
    suspend fun recalculateSubsequentTrades()
}
