package com.example.compoundingjournal.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.FilterPresetRepository

class FilterPresetViewModelFactory(
    private val repository: FilterPresetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilterPresetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FilterPresetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
