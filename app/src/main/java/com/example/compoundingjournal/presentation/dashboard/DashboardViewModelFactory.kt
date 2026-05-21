package com.example.compoundingjournal.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.TradeRepository

class DashboardViewModelFactory(
    private val tradeRepository: TradeRepository,
    private val settingsRepository: SettingsRepository,
    private val presetRepository: com.example.compoundingjournal.data.repository.FilterPresetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(tradeRepository, settingsRepository, presetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
