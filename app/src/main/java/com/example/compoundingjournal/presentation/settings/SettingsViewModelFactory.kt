package com.example.compoundingjournal.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.TradeRepository

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val tradeRepository: TradeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository, tradeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
