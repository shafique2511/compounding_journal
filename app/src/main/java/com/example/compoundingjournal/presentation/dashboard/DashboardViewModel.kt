package com.example.compoundingjournal.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
import com.example.compoundingjournal.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.floor

data class DashboardUiState(
    val kpis: DashboardKpis = DashboardKpis(),
    val chartData: DashboardChartData = DashboardChartData(),
    val filters: DashboardFilters = DashboardFilters(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

data class DashboardKpis(
    val currentBalance: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val totalTrades: Int = 0,
    val winRate: Double = 0.0,
    val lossRate: Double = 0.0,
    val averageRMultiple: Double = 0.0,
    val bestTrade: Double = 0.0,
    val worstTrade: Double = 0.0,
    val profitFactor: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val currentStreak: Int = 0,
    val longestWinStreak: Int = 0,
    val longestLossStreak: Int = 0,
    val averageQualityScore: Double = 0.0,
    val dailyLossUsed: Double = 0.0,
    val weeklyLossUsed: Double = 0.0,
    val riskWarningCount: Int = 0
)

data class DashboardChartData(
    val equityCurve: List<Double> = emptyList(),
    val drawdownSeries: List<Double> = emptyList(),
    val cumulativeProfit: List<Double> = emptyList(),
    val profitLossHistory: List<Double> = emptyList(),
    val winLossCount: Map<String, Int> = emptyMap(),
    val timeframePerformance: Map<String, Double> = emptyMap(),
    val symbolPerformance: Map<String, Double> = emptyMap(),
    val strategyPerformance: Map<String, Double> = emptyMap(),
    val monthlyProfit: Map<String, Double> = emptyMap(),
    val rMultipleDistribution: Map<String, Int> = emptyMap(),
    val withdrawalHistory: List<Double> = emptyList()
)

data class DashboardFilters(
    val dateRange: DateRangeOption = DateRangeOption.ALL_TIME,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val symbol: String? = null,
    val timeframe: String? = null,
    val strategy: String? = null,
    val status: String? = null
)

enum class DateRangeOption {
    ALL_TIME, TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, CUSTOM
}

class DashboardViewModel(
    private val tradeRepository: TradeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(DashboardFilters())
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        tradeRepository.getAllTrades(),
        settingsRepository.getSettings(),
        _filters,
        _isRefreshing
    ) { args ->
        val trades = args[0] as List<TradeEntity>
        val settings = args[1] as com.example.compoundingjournal.data.entity.SettingsEntity?
        val filters = args[2] as DashboardFilters
        val refreshing = args[3] as Boolean
        
        val filteredTrades = filterTrades(trades, filters)
        val kpis = calculateKpis(filteredTrades, trades, settings)
        val chartData = prepareChartData(filteredTrades, settings?.initialBalance ?: 1000.0)

        DashboardUiState(
            kpis = kpis,
            chartData = chartData,
            filters = filters,
            isRefreshing = refreshing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState(isLoading = true))

    fun onFilterDateRangeChange(option: DateRangeOption) {
        _filters.update { it.copy(dateRange = option) }
    }

    fun onFilterSymbolChange(symbol: String?) {
        _filters.update { it.copy(symbol = symbol) }
    }

    fun onFilterStatusChange(status: String?) {
        _filters.update { it.copy(status = status) }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(500)
            _isRefreshing.value = false
        }
    }

    private fun filterTrades(trades: List<TradeEntity>, filters: DashboardFilters): List<TradeEntity> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        return trades.filter { trade ->
            val dateMatch = when (filters.dateRange) {
                DateRangeOption.ALL_TIME -> true
                DateRangeOption.TODAY -> {
                    DateTimeUtils.formatDate(trade.timestamp) == DateTimeUtils.formatDate(now)
                }
                DateRangeOption.THIS_WEEK -> {
                    calendar.timeInMillis = now
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    trade.timestamp >= calendar.timeInMillis
                }
                DateRangeOption.THIS_MONTH -> {
                    calendar.timeInMillis = now
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    trade.timestamp >= calendar.timeInMillis
                }
                DateRangeOption.THIS_YEAR -> {
                    calendar.timeInMillis = now
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    trade.timestamp >= calendar.timeInMillis
                }
                DateRangeOption.CUSTOM -> {
                    (filters.customStartDate == null || trade.timestamp >= filters.customStartDate) &&
                    (filters.customEndDate == null || trade.timestamp <= filters.customEndDate)
                }
            }

            dateMatch &&
            (filters.symbol == null || trade.symbol == filters.symbol) &&
            (filters.timeframe == null || trade.timeframe == filters.timeframe) &&
            (filters.strategy == null || trade.strategyName == filters.strategy) &&
            (filters.status == null || trade.status == filters.status)
        }
    }

    private fun calculateKpis(filteredTrades: List<TradeEntity>, allTrades: List<TradeEntity>, settings: com.example.compoundingjournal.data.entity.SettingsEntity?): DashboardKpis {
        val initialBalance = settings?.initialBalance ?: 1000.0
        if (filteredTrades.isEmpty()) return DashboardKpis(currentBalance = initialBalance)

        val sortedTrades = filteredTrades.sortedBy { it.timestamp }
        val avgScore = filteredTrades.sumOf { it.tradeQualityScore } / filteredTrades.size

        val today = DateTimeUtils.getCurrentDeviceDate()
        val calendar = Calendar.getInstance()
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        
        val baseBalance = sortedTrades.last().startingBalance
        val dailyLossUsed = CalculationUtils.calculateDailyLossUsed(allTrades, today, baseBalance)
        val weeklyLossUsed = CalculationUtils.calculateWeeklyLossUsed(allTrades, week, year, baseBalance)
        
        val tradesTodayCount = allTrades.count { it.date == today }
        val currentLossStreak = CalculationUtils.calculateCurrentLossStreak(allTrades)

        val riskWarningCount = if (settings != null) {
            CalculationUtils.calculateRiskWarningCount(
                dailyLossUsed = dailyLossUsed,
                maxDaily = settings.maxDailyLossPercent,
                weeklyLossUsed = weeklyLossUsed,
                maxWeekly = settings.maxWeeklyLossPercent,
                tradesToday = tradesTodayCount,
                maxTrades = settings.maxTradesPerDay,
                lossStreak = currentLossStreak,
                maxStreak = settings.maxLosingStreakWarning
            )
        } else 0
        
        return DashboardKpis(
            currentBalance = sortedTrades.last().endingBalance,
            totalNetProfit = filteredTrades.sumOf { it.netProfitLoss },
            totalWithdrawals = filteredTrades.sumOf { it.withdrawalAmount },
            totalTrades = filteredTrades.size,
            winRate = CalculationUtils.calculateWinRate(filteredTrades),
            lossRate = CalculationUtils.calculateLossRate(filteredTrades),
            averageRMultiple = CalculationUtils.calculateAverageRMultiple(filteredTrades),
            bestTrade = CalculationUtils.calculateBestTrade(filteredTrades),
            worstTrade = CalculationUtils.calculateWorstTrade(filteredTrades),
            profitFactor = CalculationUtils.calculateProfitFactor(filteredTrades),
            maxDrawdown = CalculationUtils.calculateMaxDrawdown(filteredTrades),
            currentStreak = CalculationUtils.calculateCurrentStreak(filteredTrades),
            longestWinStreak = CalculationUtils.calculateLongestWinStreak(filteredTrades),
            longestLossStreak = CalculationUtils.calculateLongestLossStreak(filteredTrades),
            averageQualityScore = avgScore,
            dailyLossUsed = dailyLossUsed,
            weeklyLossUsed = weeklyLossUsed,
            riskWarningCount = riskWarningCount
        )
    }

    private fun prepareChartData(trades: List<TradeEntity>, initialBalance: Double): DashboardChartData {
        if (trades.isEmpty()) return DashboardChartData()

        val sortedByTime = trades.sortedBy { it.timestamp }
        
        // Series Data
        val equityCurve = CalculationUtils.calculateEquityCurve(trades, initialBalance).map { it.second }
        val drawdownSeries = CalculationUtils.calculateDrawdownSeries(trades, initialBalance).map { it.second }
        val cumulativeProfit = CalculationUtils.calculateCumulativeProfit(trades).map { it.second }
        
        val winCount = trades.count { it.status.uppercase() == "WIN" }
        val lossCount = trades.count { it.status.uppercase() == "LOSS" }
        val beCount = trades.count { it.status.uppercase() == "BREAKEVEN" }

        val stratPerf = trades.groupBy { it.strategyName }
            .mapValues { entry -> entry.value.sumOf { it.netProfitLoss } }

        val tfPerf = trades.groupBy { it.timeframe }
            .mapValues { entry -> entry.value.sumOf { it.netProfitLoss } }

        val symPerf = trades.groupBy { it.symbol }
            .mapValues { entry -> entry.value.sumOf { it.netProfitLoss } }

        val monthlyPerf = trades.groupBy { DateTimeUtils.formatDate(it.timestamp, "MMM yyyy") }
            .mapValues { entry -> entry.value.sumOf { it.netProfitLoss } }

        // R Multiple Distribution
        val rBuckets = mutableMapOf<String, Int>()
        trades.forEach {
            val bucket = floor(it.rMultiple).toInt()
            val label = "${bucket}R to ${bucket + 1}R"
            rBuckets[label] = rBuckets.getOrDefault(label, 0) + 1
        }

        val withdrawals = sortedByTime.filter { it.withdrawalAmount > 0 }.map { it.withdrawalAmount }

        return DashboardChartData(
            equityCurve = equityCurve,
            drawdownSeries = drawdownSeries,
            cumulativeProfit = cumulativeProfit,
            profitLossHistory = sortedByTime.map { it.netProfitLoss },
            winLossCount = mapOf("Win" to winCount, "Loss" to lossCount, "BE" to beCount),
            strategyPerformance = stratPerf,
            timeframePerformance = tfPerf,
            symbolPerformance = symPerf,
            monthlyProfit = monthlyPerf,
            rMultipleDistribution = rBuckets.toSortedMap(),
            withdrawalHistory = withdrawals
        )
    }
}
