package com.example.compoundingjournal.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
import com.example.compoundingjournal.utils.DateTimeUtils
import kotlinx.coroutines.flow.*

data class AnalyticsUiState(
    val trades: List<TradeEntity> = emptyList(),
    val summary: PerformanceSummary = PerformanceSummary(),
    val riskAnalysis: RiskAnalysis = RiskAnalysis(),
    val timeframeAnalysis: List<TimeframeStat> = emptyList(),
    val symbolAnalysis: List<SymbolStat> = emptyList(),
    val strategyAnalysis: List<StrategyStat> = emptyList(),
    val monthlyAnalysis: List<MonthlyStat> = emptyList(),
    val commonMistakes: List<Pair<String, Int>> = emptyList(),
    val mistakeAnalysis: List<MistakeTagStat> = emptyList(),
    val qualityAnalysis: List<QualityGradeStat> = emptyList(),
    val averageQualityScore: Double = 0.0,
    val isLoading: Boolean = false
)

data class PerformanceSummary(
    val totalTrades: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val breakevens: Int = 0,
    val running: Int = 0,
    val cancelled: Int = 0,
    val winRate: Double = 0.0,
    val lossRate: Double = 0.0,
    val netProfit: Double = 0.0,
    val averageProfit: Double = 0.0,
    val averageLoss: Double = 0.0,
    val profitFactor: Double = 0.0
)

data class RiskAnalysis(
    val averageRisk: Double = 0.0,
    val averageR: Double = 0.0,
    val bestR: Double = 0.0,
    val worstR: Double = 0.0,
    val maxDrawdown: Double = 0.0
)

data class TimeframeStat(
    val timeframe: String,
    val totalTrades: Int,
    val winRate: Double,
    val netProfit: Double,
    val averageR: Double
)

data class SymbolStat(
    val symbol: String,
    val totalTrades: Int,
    val winRate: Double,
    val netProfit: Double,
    val bestTrade: Double,
    val worstTrade: Double
)

data class StrategyStat(
    val strategy: String,
    val totalTrades: Int,
    val winRate: Double,
    val netProfit: Double,
    val averageR: Double,
    val profitFactor: Double,
    val maxDrawdown: Double,
    val bestTrade: Double,
    val worstTrade: Double
)

data class MonthlyStat(
    val month: String,
    val netProfit: Double,
    val totalTrades: Int,
    val withdrawals: Double,
    val endingBalance: Double
)

data class MistakeTagStat(
    val tag: String,
    val count: Int,
    val netProfit: Double,
    val winRate: Double
)

data class QualityGradeStat(
    val grade: String,
    val count: Int,
    val winRate: Double,
    val netProfit: Double
)

class AnalyticsViewModel(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = tradeRepository.getAllTrades()
        .map { trades ->
            if (trades.isEmpty()) {
                AnalyticsUiState()
            } else {
                calculateAnalytics(trades)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState(isLoading = true))

    private fun calculateAnalytics(trades: List<TradeEntity>): AnalyticsUiState {
        val summary = calculateSummary(trades)
        val risk = calculateRisk(trades)
        val timeframeStats = calculateTimeframeStats(trades)
        val symbolStats = calculateSymbolStats(trades)
        val strategyStats = calculateStrategyStats(trades)
        val monthlyStats = calculateMonthlyStats(trades)
        val mistakeTags = calculateMistakeTagStats(trades)
        val qualityStats = calculateQualityStats(trades)

        return AnalyticsUiState(
            trades = trades,
            summary = summary,
            riskAnalysis = risk,
            timeframeAnalysis = timeframeStats,
            symbolAnalysis = symbolStats,
            strategyAnalysis = strategyStats,
            monthlyAnalysis = monthlyStats,
            mistakeAnalysis = mistakeTags,
            qualityAnalysis = qualityStats,
            averageQualityScore = if (trades.isNotEmpty()) trades.sumOf { it.tradeQualityScore } / trades.size else 0.0,
            isLoading = false
        )
    }

    private fun calculateSummary(trades: List<TradeEntity>): PerformanceSummary {
        val total = trades.size
        val wins = trades.count { it.status.uppercase() == "WIN" }
        val losses = trades.count { it.status.uppercase() == "LOSS" }
        val be = trades.count { it.status.uppercase() == "BREAKEVEN" }
        val running = trades.count { it.status.uppercase() == "RUNNING" }
        val cancelled = trades.count { it.status.uppercase() == "CANCELLED" }
        
        val winTrades = trades.filter { it.netProfitLoss > 0 }
        val lossTrades = trades.filter { it.netProfitLoss < 0 }
        
        return PerformanceSummary(
            totalTrades = total,
            wins = wins,
            losses = losses,
            breakevens = be,
            running = running,
            cancelled = cancelled,
            winRate = CalculationUtils.calculateWinRate(trades),
            lossRate = CalculationUtils.calculateLossRate(trades),
            netProfit = trades.sumOf { it.netProfitLoss },
            averageProfit = if (winTrades.isNotEmpty()) winTrades.averageOf { it.netProfitLoss } else 0.0,
            averageLoss = if (lossTrades.isNotEmpty()) lossTrades.averageOf { it.netProfitLoss } else 0.0,
            profitFactor = CalculationUtils.calculateProfitFactor(trades)
        )
    }

    private fun calculateRisk(trades: List<TradeEntity>): RiskAnalysis {
        return RiskAnalysis(
            averageRisk = trades.averageOf { it.riskAmount },
            averageR = CalculationUtils.calculateAverageRMultiple(trades),
            bestR = trades.maxOfOrNull { it.rMultiple } ?: 0.0,
            worstR = trades.minOfOrNull { it.rMultiple } ?: 0.0,
            maxDrawdown = CalculationUtils.calculateMaxDrawdown(trades)
        )
    }

    private fun calculateTimeframeStats(trades: List<TradeEntity>): List<TimeframeStat> {
        return trades.groupBy { it.timeframe }.map { (tf, tfTrades) ->
            TimeframeStat(
                timeframe = tf,
                totalTrades = tfTrades.size,
                winRate = CalculationUtils.calculateWinRate(tfTrades),
                netProfit = tfTrades.sumOf { it.netProfitLoss },
                averageR = CalculationUtils.calculateAverageRMultiple(tfTrades)
            )
        }.sortedByDescending { it.netProfit }
    }

    private fun calculateSymbolStats(trades: List<TradeEntity>): List<SymbolStat> {
        return trades.groupBy { it.symbol }.map { (symbol, symTrades) ->
            SymbolStat(
                symbol = symbol,
                totalTrades = symTrades.size,
                winRate = CalculationUtils.calculateWinRate(symTrades),
                netProfit = symTrades.sumOf { it.netProfitLoss },
                bestTrade = CalculationUtils.calculateBestTrade(symTrades),
                worstTrade = CalculationUtils.calculateWorstTrade(symTrades)
            )
        }.sortedByDescending { it.netProfit }
    }

    private fun calculateStrategyStats(trades: List<TradeEntity>): List<StrategyStat> {
        return trades.groupBy { it.strategyName }.map { (strategy, stratTrades) ->
            StrategyStat(
                strategy = if (strategy.isEmpty()) "Unknown" else strategy,
                totalTrades = stratTrades.size,
                winRate = CalculationUtils.calculateWinRate(stratTrades),
                netProfit = stratTrades.sumOf { it.netProfitLoss },
                averageR = CalculationUtils.calculateAverageRMultiple(stratTrades),
                profitFactor = CalculationUtils.calculateProfitFactor(stratTrades),
                maxDrawdown = CalculationUtils.calculateMaxDrawdown(stratTrades),
                bestTrade = CalculationUtils.calculateBestTrade(stratTrades),
                worstTrade = CalculationUtils.calculateWorstTrade(stratTrades)
            )
        }.sortedByDescending { it.netProfit }
    }

    private fun calculateMonthlyStats(trades: List<TradeEntity>): List<MonthlyStat> {
        return trades.groupBy { DateTimeUtils.formatDate(it.timestamp, "MMM yyyy") }.map { (month, monthTrades) ->
            val sorted = monthTrades.sortedBy { it.timestamp }
            MonthlyStat(
                month = month,
                netProfit = monthTrades.sumOf { it.netProfitLoss },
                totalTrades = monthTrades.size,
                withdrawals = monthTrades.sumOf { it.withdrawalAmount },
                endingBalance = sorted.last().endingBalance
            )
        }
    }

    private fun calculateMistakeTagStats(trades: List<TradeEntity>): List<MistakeTagStat> {
        val tagMap = mutableMapOf<String, MutableList<TradeEntity>>()
        trades.forEach { trade ->
            if (trade.mistakeTags.isNotBlank()) {
                trade.mistakeTags.split(",").forEach { tag ->
                    val cleanTag = tag.trim()
                    if (cleanTag.isNotEmpty()) {
                        tagMap.getOrPut(cleanTag) { mutableListOf() }.add(trade)
                    }
                }
            }
        }
        
        return tagMap.map { (tag, tagTrades) ->
            MistakeTagStat(
                tag = tag,
                count = tagTrades.size,
                netProfit = tagTrades.sumOf { it.netProfitLoss },
                winRate = CalculationUtils.calculateWinRate(tagTrades)
            )
        }.sortedByDescending { it.count }
    }

    private fun calculateQualityStats(trades: List<TradeEntity>): List<QualityGradeStat> {
        return trades.groupBy { it.tradeQualityGrade }.map { (grade, gradeTrades) ->
            QualityGradeStat(
                grade = if (grade.isEmpty()) "N/A" else grade,
                count = gradeTrades.size,
                winRate = CalculationUtils.calculateWinRate(gradeTrades),
                netProfit = gradeTrades.sumOf { it.netProfitLoss }
            )
        }.sortedBy { it.grade }
    }

    private fun <T> Iterable<T>.averageOf(selector: (T) -> Double): Double {
        var count = 0
        var sum = 0.0
        for (element in this) {
            sum += selector(element)
            count++
        }
        return if (count == 0) 0.0 else sum / count
    }
}
