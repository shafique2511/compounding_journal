package com.example.compoundingjournal.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JournalUiState(
    val trades: List<TradeEntity> = emptyList(),
    val searchQuery: String = "",
    val filterSymbol: String? = null,
    val filterTimeframe: String? = null,
    val filterStatus: String? = null,
    val filterStrategy: String? = null,
    val sortBy: SortOption = SortOption.NEWEST,
    val isLoading: Boolean = false
)

enum class SortOption {
    NEWEST, OLDEST, HIGHEST_PROFIT, BIGGEST_LOSS
}

class JournalViewModel(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterSymbol = MutableStateFlow<String?>(null)
    private val _filterTimeframe = MutableStateFlow<String?>(null)
    private val _filterStatus = MutableStateFlow<String?>(null)
    private val _filterStrategy = MutableStateFlow<String?>(null)
    private val _sortBy = MutableStateFlow(SortOption.NEWEST)

    val uiState: StateFlow<JournalUiState> = combine(
        tradeRepository.getAllTrades(),
        _searchQuery,
        _filterSymbol,
        _filterTimeframe,
        _filterStatus,
        _filterStrategy,
        _sortBy
    ) { args ->
        val trades = args[0] as List<TradeEntity>
        val query = args[1] as String
        val symbol = args[2] as String?
        val tf = args[3] as String?
        val status = args[4] as String?
        val strategy = args[5] as String?
        val sort = args[6] as SortOption

        val filtered = trades.filter { trade ->
            (query.isEmpty() || trade.symbol.contains(query, ignoreCase = true) || 
             trade.strategyName.contains(query, ignoreCase = true) || 
             trade.notes.contains(query, ignoreCase = true)) &&
            (symbol == null || trade.symbol == symbol) &&
            (tf == null || trade.timeframe == tf) &&
            (status == null || trade.status == status) &&
            (strategy == null || trade.strategyName == strategy)
        }

        val sorted = when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
            SortOption.OLDEST -> filtered.sortedBy { it.timestamp }
            SortOption.HIGHEST_PROFIT -> filtered.sortedByDescending { it.netProfitLoss }
            SortOption.BIGGEST_LOSS -> filtered.sortedBy { it.netProfitLoss }
        }

        JournalUiState(
            trades = sorted,
            searchQuery = query,
            filterSymbol = symbol,
            filterTimeframe = tf,
            filterStatus = status,
            filterStrategy = strategy,
            sortBy = sort
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JournalUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSymbolChange(symbol: String?) {
        _filterSymbol.value = symbol
    }

    fun onFilterTimeframeChange(tf: String?) {
        _filterTimeframe.value = tf
    }

    fun onFilterStatusChange(status: String?) {
        _filterStatus.value = status
    }

    fun onFilterStrategyChange(strategy: String?) {
        _filterStrategy.value = strategy
    }

    fun onSortByChange(sort: SortOption) {
        _sortBy.value = sort
    }

    fun deleteTrade(trade: TradeEntity) {
        viewModelScope.launch {
            tradeRepository.deleteTrade(trade)
            recalculateSubsequentTrades()
        }
    }

    private suspend fun recalculateSubsequentTrades() {
        val allTrades = tradeRepository.getAllTrades().first().sortedBy { it.tradeNumber }
        var currentBalance: Double? = null
        
        for (trade in allTrades) {
            if (currentBalance != null) {
                val updatedTrade = trade.copy(
                    startingBalance = currentBalance,
                    endingBalance = CalculationUtils.calculateEndingBalance(currentBalance, trade.netProfitLoss, trade.withdrawalAmount),
                    growthPercent = CalculationUtils.calculateGrowthPercent(trade.netProfitLoss, currentBalance)
                )
                tradeRepository.updateTrade(updatedTrade)
                currentBalance = updatedTrade.endingBalance
            } else {
                currentBalance = trade.endingBalance
            }
        }
    }
}
