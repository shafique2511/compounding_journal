package com.example.compoundingjournal.presentation.exportbackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.ExportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportBackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val tradesToExport: List<TradeEntity> = emptyList()
)

class ExportBackupViewModel(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportBackupUiState())
    val uiState: StateFlow<ExportBackupUiState> = _uiState.asStateFlow()

    fun prepareAllTradesExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            val csv = ExportUtils.generateTradesCsv(trades)
            onReady(csv)
        }
    }

    fun prepareSummaryExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            val totalTrades = trades.size
            val netProfit = trades.sumOf { it.netProfitLoss }
            val wins = trades.count { it.status.uppercase() == "WIN" }
            
            val summaryMap = mapOf(
                "Total Trades" to totalTrades.toString(),
                "Net Profit" to String.format("%.2f", netProfit),
                "Wins" to wins.toString(),
                "Win Rate" to if (totalTrades > 0) "${(wins.toDouble() / totalTrades * 100).toInt()}%" else "0%"
            )
            
            val csv = ExportUtils.generateSummaryCsv(summaryMap)
            onReady(csv)
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(message = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
