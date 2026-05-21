package com.example.compoundingjournal.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.FilterPresetEntity
import com.example.compoundingjournal.data.repository.FilterPresetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FilterPresetUiState(
    val presets: List<FilterPresetEntity> = emptyList(),
    val isLoading: Boolean = false
)

class FilterPresetViewModel(
    private val repository: FilterPresetRepository
) : ViewModel() {

    val uiState: StateFlow<FilterPresetUiState> = repository.getAllPresets()
        .map { FilterPresetUiState(presets = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterPresetUiState(isLoading = true))

    fun deletePreset(preset: FilterPresetEntity) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }
}
