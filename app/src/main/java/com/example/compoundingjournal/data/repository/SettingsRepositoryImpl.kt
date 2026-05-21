package com.example.compoundingjournal.data.repository

import com.example.compoundingjournal.data.dao.SettingsDao
import com.example.compoundingjournal.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SettingsRepositoryImpl(private val settingsDao: SettingsDao) : SettingsRepository {
    override fun getSettings(): Flow<SettingsEntity?> = settingsDao.getSettings()

    override suspend fun insertSettings(settings: SettingsEntity) {
        settingsDao.insertSettings(settings)
    }

    override suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.updateSettings(settings)
    }

    override suspend fun ensureDefaultSettings() {
        val currentSettings = settingsDao.getSettings().firstOrNull()
        if (currentSettings == null) {
            val defaultSettings = SettingsEntity(
                initialBalance = 1000.0,
                currency = "USD",
                timezoneOffset = "UTC+0",
                dateFormat = "yyyy-MM-dd",
                timeFormat = "HH:mm:ss",
                defaultTimeframe = "H1",
                defaultSymbol = "EURUSD",
                defaultCommission = 0.0,
                defaultSwap = 0.0,
                themeMode = "SYSTEM",
                accentColor = "BLUE",
                autoTradeNumber = true,
                maxRiskPerTradePercent = 2.0,
                maxDailyLossPercent = 5.0,
                maxWeeklyLossPercent = 10.0,
                maxTradesPerDay = 5,
                maxLosingStreakWarning = 3,
                minimumRiskRewardRatio = 1.5,
                enableRiskWarning = true
            )
            settingsDao.insertSettings(defaultSettings)
        }
    }
}
