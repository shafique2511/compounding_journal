package com.example.compoundingjournal.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.StrategyEntity
import com.example.compoundingjournal.data.repository.StrategyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StrategyUiState(
    val strategies: List<StrategyEntity> = emptyList(),
    val isLoading: Boolean = false,
    val showOnlyActive: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class StrategyViewModel(
    private val strategyRepository: StrategyRepository
) : ViewModel() {

    private val _showOnlyActive = MutableStateFlow(true)
    
    val uiState: StateFlow<StrategyUiState> = combine(
        _showOnlyActive.flatMapLatest { onlyActive ->
            if (onlyActive) strategyRepository.getActiveStrategies()
            else strategyRepository.getAllStrategies()
        },
        _showOnlyActive
    ) { strategies, onlyActive ->
        StrategyUiState(
            strategies = strategies,
            showOnlyActive = onlyActive,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StrategyUiState(isLoading = true))

    fun toggleFilter() {
        _showOnlyActive.update { !it }
    }

    fun deleteStrategy(strategy: StrategyEntity) {
        viewModelScope.launch {
            strategyRepository.deleteStrategy(strategy)
        }
    }
}
