package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.dao.TradeDao
import com.example.compoundingjournal.data.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

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
}
