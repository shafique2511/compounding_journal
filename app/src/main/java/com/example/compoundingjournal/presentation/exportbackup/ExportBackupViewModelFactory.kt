package com.example.compoundingjournal.presentation.exportbackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.StrategyRepository
import com.example.compoundingjournal.data.repository.TradeRepository

class ExportBackupViewModelFactory(
    private val tradeRepository: TradeRepository,
    private val strategyRepository: StrategyRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportBackupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportBackupViewModel(tradeRepository, strategyRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
