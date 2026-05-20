package com.example.compoundingjournal.presentation.exportbackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.compoundingjournal.data.repository.TradeRepository

class ExportBackupViewModelFactory(
    private val tradeRepository: TradeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportBackupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportBackupViewModel(tradeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
