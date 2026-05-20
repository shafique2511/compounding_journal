package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<SettingsEntity?>
    suspend fun insertSettings(settings: SettingsEntity)
    suspend fun updateSettings(settings: SettingsEntity)
    suspend fun ensureDefaultSettings()
}
