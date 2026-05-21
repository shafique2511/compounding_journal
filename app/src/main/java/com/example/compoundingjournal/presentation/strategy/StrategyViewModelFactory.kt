package com.example.compoundingjournal.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.StrategyRepository

class StrategyViewModelFactory(
    private val strategyRepository: StrategyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StrategyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StrategyViewModel(strategyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
