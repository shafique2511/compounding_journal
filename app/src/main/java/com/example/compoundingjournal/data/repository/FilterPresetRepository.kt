package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.entity.FilterPresetEntity
import kotlinx.coroutines.flow.Flow

interface FilterPresetRepository {
    fun getAllPresets(): Flow<List<FilterPresetEntity>>
    suspend fun getPresetById(id: Long): FilterPresetEntity?
    suspend fun insertPreset(preset: FilterPresetEntity): Long
    suspend fun updatePreset(preset: FilterPresetEntity)
    suspend fun deletePreset(preset: FilterPresetEntity)
}
