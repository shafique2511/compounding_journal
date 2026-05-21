package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.entity.StrategyEntity
import kotlinx.coroutines.flow.Flow

interface StrategyRepository {
    fun getAllStrategies(): Flow<List<StrategyEntity>>
    fun getActiveStrategies(): Flow<List<StrategyEntity>>
    suspend fun getStrategyById(id: Long): StrategyEntity?
    suspend fun insertStrategy(strategy: StrategyEntity): Long
    suspend fun updateStrategy(strategy: StrategyEntity)
    suspend fun deleteStrategy(strategy: StrategyEntity)
}
