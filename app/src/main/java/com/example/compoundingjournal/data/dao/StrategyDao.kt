package com.example.compoundingjournal.data.dao

import androidx.room.*
import com.example.compoundingjournal.data.entity.StrategyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrategyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: StrategyEntity): Long

    @Update
    suspend fun updateStrategy(strategy: StrategyEntity)

    @Delete
    suspend fun deleteStrategy(strategy: StrategyEntity)

    @Query("SELECT * FROM strategies ORDER BY strategyName ASC")
    fun getAllStrategies(): Flow<List<StrategyEntity>>

    @Query("SELECT * FROM strategies WHERE isActive = 1 ORDER BY strategyName ASC")
    fun getActiveStrategies(): Flow<List<StrategyEntity>>

    @Query("SELECT * FROM strategies WHERE id = :id")
    suspend fun getStrategyById(id: Long): StrategyEntity?
}
