package com.example.compoundingjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.compoundingjournal.data.entity.TradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeEntity)

    @Query("SELECT * FROM trades ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getTradeById(id: Long): TradeEntity?

    @Query("SELECT * FROM trades WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getTradesByDateRange(startTime: Long, endTime: Long): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getTradesBySymbol(symbol: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE timeframe = :timeframe ORDER BY timestamp DESC")
    fun getTradesByTimeframe(timeframe: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE status = :status ORDER BY timestamp DESC")
    fun getTradesByStatus(status: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE strategyName = :strategy ORDER BY timestamp DESC")
    fun getTradesByStrategy(strategy: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTrade(): TradeEntity?

    @Query("DELETE FROM trades")
    suspend fun deleteAllTrades()
}
