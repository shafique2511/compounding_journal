package com.example.compoundingjournal.presentation.addtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.StrategyRepository
import com.example.compoundingjournal.data.repository.TradeRepository

class TradeViewModelFactory(
    private val tradeRepository: TradeRepository,
    private val settingsRepository: SettingsRepository,
    private val strategyRepository: StrategyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradeViewModel(tradeRepository, settingsRepository, strategyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
