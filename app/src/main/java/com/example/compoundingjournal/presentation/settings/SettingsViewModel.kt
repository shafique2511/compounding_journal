package com.example.compoundingjournal.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.SettingsEntity
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.TradeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: SettingsEntity? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            settingsRepository.ensureDefaultSettings()
            val settings = settingsRepository.getSettings().first()
            _uiState.update { it.copy(settings = settings, isLoading = false) }
        }
    }

    fun updateSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
            _uiState.update { it.copy(settings = newSettings, isSaved = true, message = "Settings saved successfully") }
        }
    }

    fun deleteAllTrades() {
        viewModelScope.launch {
            tradeRepository.deleteAllTrades()
            _uiState.update { it.copy(message = "All trades deleted") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, isSaved = false) }
    }
}
