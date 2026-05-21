package com.example.compoundingjournal.presentation.exportbackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.TradeRepository

class ExportBackupViewModelFactory(
    private val tradeRepository: TradeRepository,
    private val strategyRepository: com.example.compoundingjournal.data.repository.StrategyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportBackupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportBackupViewModel(tradeRepository, strategyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
