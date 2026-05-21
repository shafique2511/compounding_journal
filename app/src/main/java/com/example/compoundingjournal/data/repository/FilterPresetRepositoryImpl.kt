package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.dao.FilterPresetDao
import com.example.compoundingjournal.data.entity.FilterPresetEntity
import kotlinx.coroutines.flow.Flow

class FilterPresetRepositoryImpl(private val dao: FilterPresetDao) : FilterPresetRepository {
    override fun getAllPresets(): Flow<List<FilterPresetEntity>> = dao.getAllPresets()
    override suspend fun getPresetById(id: Long): FilterPresetEntity? = dao.getPresetById(id)
    override suspend fun insertPreset(preset: FilterPresetEntity): Long = dao.insertPreset(preset)
    override suspend fun updatePreset(preset: FilterPresetEntity) = dao.updatePreset(preset)
    override suspend fun deletePreset(preset: FilterPresetEntity) = dao.deletePreset(preset)
}
