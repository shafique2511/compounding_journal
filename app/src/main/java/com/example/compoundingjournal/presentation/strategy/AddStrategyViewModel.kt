package com.example.compoundingjournal.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.StrategyEntity
import com.example.compoundingjournal.data.repository.StrategyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddStrategyViewModel(
    private val strategyRepository: StrategyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrategyFormState())
    val uiState: StateFlow<StrategyFormState> = _uiState.asStateFlow()

    private var editingStrategyId: Long? = null
    private var originalCreatedAt: Long = 0

    fun initForEdit(id: Long) {
        editingStrategyId = id
        viewModelScope.launch {
            val strategy = strategyRepository.getStrategyById(id)
            strategy?.let { s ->
                originalCreatedAt = s.createdAt
                _uiState.update { 
                    it.copy(
                        strategyName = s.strategyName,
                        marketType = s.marketType,
                        timeframe = s.timeframe,
                        entryRules = s.entryRules,
                        exitRules = s.exitRules,
                        stopLossRules = s.stopLossRules,
                        takeProfitRules = s.takeProfitRules,
                        riskRules = s.riskRules,
                        exampleScreenshotPath = s.exampleScreenshotPath,
                        notes = s.notes,
                        isActive = s.isActive
                    )
                }
            }
        }
    }

    fun onEvent(event: StrategyFormEvent) {
        when (event) {
            is StrategyFormEvent.NameChanged -> _uiState.update { it.copy(strategyName = event.value) }
            is StrategyFormEvent.MarketTypeChanged -> _uiState.update { it.copy(marketType = event.value) }
            is StrategyFormEvent.TimeframeChanged -> _uiState.update { it.copy(timeframe = event.value) }
            is StrategyFormEvent.EntryRulesChanged -> _uiState.update { it.copy(entryRules = event.value) }
            is StrategyFormEvent.ExitRulesChanged -> _uiState.update { it.copy(exitRules = event.value) }
            is StrategyFormEvent.StopLossRulesChanged -> _uiState.update { it.copy(stopLossRules = event.value) }
            is StrategyFormEvent.TakeProfitRulesChanged -> _uiState.update { it.copy(takeProfitRules = event.value) }
            is StrategyFormEvent.RiskRulesChanged -> _uiState.update { it.copy(riskRules = event.value) }
            is StrategyFormEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value) }
            is StrategyFormEvent.IsActiveChanged -> _uiState.update { it.copy(isActive = event.value) }
            is StrategyFormEvent.ScreenshotPicked -> _uiState.update { it.copy(exampleScreenshotPath = event.uri) }
            is StrategyFormEvent.Save -> save(event.onSuccess)
        }
    }

    private fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.strategyName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Strategy name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val strategy = StrategyEntity(
                id = editingStrategyId ?: 0,
                strategyName = state.strategyName,
                marketType = state.marketType,
                timeframe = state.timeframe,
                entryRules = state.entryRules,
                exitRules = state.exitRules,
                stopLossRules = state.stopLossRules,
                takeProfitRules = state.takeProfitRules,
                riskRules = state.riskRules,
                exampleScreenshotPath = state.exampleScreenshotPath,
                notes = state.notes,
                isActive = state.isActive,
                createdAt = if (editingStrategyId == null) now else originalCreatedAt,
                updatedAt = now
            )

            if (editingStrategyId == null) strategyRepository.insertStrategy(strategy)
            else strategyRepository.updateStrategy(strategy)
            
            onSuccess()
        }
    }
}

sealed class StrategyFormEvent {
    data class NameChanged(val value: String) : StrategyFormEvent()
    data class MarketTypeChanged(val value: String) : StrategyFormEvent()
    data class TimeframeChanged(val value: String) : StrategyFormEvent()
    data class EntryRulesChanged(val value: String) : StrategyFormEvent()
    data class ExitRulesChanged(val value: String) : StrategyFormEvent()
    data class StopLossRulesChanged(val value: String) : StrategyFormEvent()
    data class TakeProfitRulesChanged(val value: String) : StrategyFormEvent()
    data class RiskRulesChanged(val value: String) : StrategyFormEvent()
    data class NotesChanged(val value: String) : StrategyFormEvent()
    data class IsActiveChanged(val value: Boolean) : StrategyFormEvent()
    data class ScreenshotPicked(val uri: String?) : StrategyFormEvent()
    data class Save(val onSuccess: () -> Unit) : StrategyFormEvent()
}
