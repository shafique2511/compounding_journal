package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.dao.TradeDao
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.utils.CalculationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TradeRepositoryImpl(private val tradeDao: TradeDao) : TradeRepository {
    override fun getAllTrades(): Flow<List<TradeEntity>> = tradeDao.getAllTrades()

    override suspend fun getTradeById(id: Long): TradeEntity? = tradeDao.getTradeById(id)

    override suspend fun insertTrade(trade: TradeEntity) {
        tradeDao.insertTrade(trade)
    }

    override suspend fun updateTrade(trade: TradeEntity) {
        tradeDao.updateTrade(trade)
    }

    override suspend fun deleteTrade(trade: TradeEntity) {
        tradeDao.deleteTrade(trade)
        recalculateSubsequentTrades()
    }

    override fun getTradesByDateRange(startTime: Long, endTime: Long): Flow<List<TradeEntity>> =
        tradeDao.getTradesByDateRange(startTime, endTime)

    override fun getTradesBySymbol(symbol: String): Flow<List<TradeEntity>> =
        tradeDao.getTradesBySymbol(symbol)

    override fun getTradesByTimeframe(timeframe: String): Flow<List<TradeEntity>> =
        tradeDao.getTradesByTimeframe(timeframe)

    override fun getTradesByStatus(status: String): Flow<List<TradeEntity>> =
        tradeDao.getTradesByStatus(status)

    override fun getTradesByStrategy(strategy: String): Flow<List<TradeEntity>> =
        tradeDao.getTradesByStrategy(strategy)

    override suspend fun getLastTrade(): TradeEntity? = tradeDao.getLastTrade()

    override suspend fun deleteAllTrades() {
        tradeDao.deleteAllTrades()
    }

    override suspend fun recalculateSubsequentTrades() {
        val allTrades = tradeDao.getAllTrades().first().sortedBy { it.tradeNumber }
        var currentBalance: Double? = null
        
        for (trade in allTrades) {
            if (currentBalance != null) {
                val updatedTrade = trade.copy(
                    startingBalance = currentBalance,
                    endingBalance = CalculationUtils.calculateEndingBalance(currentBalance, trade.netProfitLoss, trade.withdrawalAmount),
                    growthPercent = CalculationUtils.calculateGrowthPercent(trade.netProfitLoss, currentBalance),
                    updatedAt = System.currentTimeMillis()
                )
                tradeDao.updateTrade(updatedTrade)
                currentBalance = updatedTrade.endingBalance
            } else {
                currentBalance = trade.endingBalance
            }
        }
    }
}
