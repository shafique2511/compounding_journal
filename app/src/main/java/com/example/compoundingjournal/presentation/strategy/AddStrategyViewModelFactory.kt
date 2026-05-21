package com.example.compoundingjournal.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.StrategyRepository

class AddStrategyViewModelFactory(
    private val strategyRepository: StrategyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddStrategyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddStrategyViewModel(strategyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
