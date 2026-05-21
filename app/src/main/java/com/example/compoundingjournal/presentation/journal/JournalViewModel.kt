package com.example.compoundingjournal.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.FilterPresetEntity
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.FilterPresetRepository
import com.example.compoundingjournal.data.repository.TradeRepository
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
    private val tradeRepository: TradeRepository,
    private val presetRepository: FilterPresetRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterSymbol = MutableStateFlow<String?>(null)
    private val _filterTimeframe = MutableStateFlow<String?>(null)
    private val _filterStatus = MutableStateFlow<String?>(null)
    private val _filterStrategy = MutableStateFlow<String?>(null)
    private val _filterGrade = MutableStateFlow<String?>(null)
    private val _filterRule = MutableStateFlow<String?>(null)
    private val _sortBy = MutableStateFlow(SortOption.NEWEST)

    val presets = presetRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<JournalUiState> = combine(
        tradeRepository.getAllTrades(),
        _searchQuery,
        _filterSymbol,
        _filterTimeframe,
        _filterStatus,
        _filterStrategy,
        _filterGrade,
        _filterRule,
        _sortBy
    ) { args ->
        val trades = args[0] as List<TradeEntity>
        val query = args[1] as String
        val symbol = args[2] as String?
        val tf = args[3] as String?
        val status = args[4] as String?
        val strategy = args[5] as String?
        val grade = args[6] as String?
        val rule = args[7] as String?
        val sort = args[8] as SortOption

        val filtered = trades.filter { trade ->
            (query.isEmpty() || trade.symbol.contains(query, ignoreCase = true) || 
             trade.strategyName.contains(query, ignoreCase = true) || 
             trade.notes.contains(query, ignoreCase = true)) &&
            (symbol == null || trade.symbol == symbol) &&
            (tf == null || trade.timeframe == tf) &&
            (status == null || trade.status == status) &&
            (strategy == null || trade.strategyName == strategy) &&
            (grade == null || trade.tradeQualityGrade == grade) &&
            (rule == null || trade.ruleFollowed == rule)
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

    fun applyPreset(preset: FilterPresetEntity) {
        _filterSymbol.value = preset.symbolFilter.ifBlank { null }
        _filterTimeframe.value = preset.timeframeFilter.ifBlank { null }
        _filterStatus.value = preset.statusFilter.ifBlank { null }
        _filterStrategy.value = preset.strategyFilter.ifBlank { null }
        _filterGrade.value = preset.qualityGradeFilter.ifBlank { null }
        _filterRule.value = preset.ruleFollowedFilter.ifBlank { null }
    }

    fun saveCurrentFilterAsPreset(name: String) {
        viewModelScope.launch {
            val preset = FilterPresetEntity(
                presetName = name,
                symbolFilter = _filterSymbol.value ?: "",
                timeframeFilter = _filterTimeframe.value ?: "",
                statusFilter = _filterStatus.value ?: "",
                strategyFilter = _filterStrategy.value ?: "",
                qualityGradeFilter = _filterGrade.value ?: "",
                ruleFollowedFilter = _filterRule.value ?: "",
                dateFilter = "", // Future: Date range support in presets
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            presetRepository.insertPreset(preset)
        }
    }

    fun deleteTrade(trade: TradeEntity) {
        viewModelScope.launch {
            tradeRepository.deleteTrade(trade)
        }
    }
}
