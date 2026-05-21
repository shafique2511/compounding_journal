package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.dao.StrategyDao
import com.example.compoundingjournal.data.entity.StrategyEntity
import kotlinx.coroutines.flow.Flow

class StrategyRepositoryImpl(private val strategyDao: StrategyDao) : StrategyRepository {
    override fun getAllStrategies(): Flow<List<StrategyEntity>> = strategyDao.getAllStrategies()
    override fun getActiveStrategies(): Flow<List<StrategyEntity>> = strategyDao.getActiveStrategies()
    override suspend fun getStrategyById(id: Long): StrategyEntity? = strategyDao.getStrategyById(id)
    override suspend fun insertStrategy(strategy: StrategyEntity): Long = strategyDao.insertStrategy(strategy)
    override suspend fun updateStrategy(strategy: StrategyEntity) = strategyDao.updateStrategy(strategy)
    override suspend fun deleteStrategy(strategy: StrategyEntity) = strategyDao.deleteStrategy(strategy)
}
