package com.example.compoundingjournal.presentation.exportbackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.StrategyRepository
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
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
    private val tradeRepository: TradeRepository,
    private val strategyRepository: StrategyRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportBackupUiState())
    val uiState: StateFlow<ExportBackupUiState> = _uiState.asStateFlow()

    fun prepareAllTradesExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            if (trades.isEmpty()) {
                showMessage("No trades available for export")
                return@launch
            }
            val csv = ExportUtils.generateTradesCsv(trades)
            onReady(csv)
        }
    }

    fun prepareSummaryExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            if (trades.isEmpty()) {
                showMessage("No data available for summary")
                return@launch
            }
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

    fun prepareStrategyExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val strategies = strategyRepository.getAllStrategies().first()
            if (strategies.isEmpty()) {
                showMessage("No strategies available for export")
                return@launch
            }
            val csv = ExportUtils.generateStrategyCsv(strategies)
            onReady(csv)
        }
    }

    fun prepareReviewReportExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            if (trades.isEmpty()) {
                showMessage("No trades available for review report")
                return@launch
            }
            val csv = ExportUtils.generateReviewReportCsv(trades)
            onReady(csv)
        }
    }

    fun prepareMistakeAnalysisExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            if (trades.isEmpty()) {
                showMessage("No data available for mistake analysis")
                return@launch
            }
            
            val stats = mutableMapOf<String, Int>()
            val impact = mutableMapOf<String, Double>()
            
            trades.forEach { t ->
                if (t.mistakeTags.isNotBlank()) {
                    t.mistakeTags.split(",").forEach { tag ->
                        val clean = tag.trim()
                        if (clean.isNotBlank()) {
                            stats[clean] = stats.getOrDefault(clean, 0) + 1
                            impact[clean] = impact.getOrDefault(clean, 0.0) + t.netProfitLoss
                        }
                    }
                }
            }
            
            val csv = ExportUtils.generateMistakeAnalysisCsv(stats.toList(), impact)
            onReady(csv)
        }
    }

    fun prepareQualityAnalysisExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            if (trades.isEmpty()) {
                showMessage("No data available for quality analysis")
                return@launch
            }
            
            val gradeCounts = trades.groupBy { it.tradeQualityGrade }.mapValues { it.value.size }
            val winRates = trades.groupBy { it.tradeQualityGrade }.mapValues { entry ->
                val wins = entry.value.count { it.status.uppercase() == "WIN" }
                (wins.toDouble() / entry.value.size) * 100
            }
            
            val csv = ExportUtils.generateQualityAnalysisCsv(gradeCounts, winRates)
            onReady(csv)
        }
    }

    fun prepareRiskReportExport(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val trades = tradeRepository.getAllTrades().first()
            val settings = settingsRepository.getSettings().first()
            if (trades.isEmpty() || settings == null) {
                showMessage("Insufficient data for risk report")
                return@launch
            }

            var warnings = 0
            var followedProfit = 0.0
            var brokenProfit = 0.0

            trades.forEach { t ->
                val riskPercent = if (t.startingBalance > 0) (t.riskAmount / t.startingBalance) * 100 else 0.0
                val brokeAny = CalculationUtils.checkRiskPerTradeWarning(riskPercent, settings.maxRiskPerTradePercent) ||
                            CalculationUtils.checkMinimumRiskRewardWarning(t.riskRewardRatio, settings.minimumRiskRewardRatio)
                
                if (brokeAny) {
                    warnings++
                    brokenProfit += t.netProfitLoss
                } else {
                    followedProfit += t.netProfitLoss
                }
            }
            
            val csv = ExportUtils.generateRiskReportCsv(warnings, followedProfit, brokenProfit)
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
