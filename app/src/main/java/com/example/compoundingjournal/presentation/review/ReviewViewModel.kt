package com.example.compoundingjournal.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ReviewFilter {
    ALL, LOSING, RULE_BROKEN, LOW_QUALITY, WITH_MISTAKES, NO_SCREENSHOT, NO_NOTES, BEST, WORST
}

data class ReviewUiState(
    val trades: List<TradeEntity> = emptyList(),
    val filter: ReviewFilter = ReviewFilter.ALL,
    val isLoading: Boolean = false
)

class ReviewViewModel(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(ReviewFilter.ALL)
    
    val uiState: StateFlow<ReviewUiState> = combine(
        tradeRepository.getAllTrades(),
        _filter
    ) { trades, filter ->
        val filtered = when (filter) {
            ReviewFilter.ALL -> trades
            ReviewFilter.LOSING -> trades.filter { it.netProfitLoss < 0 }
            ReviewFilter.RULE_BROKEN -> trades.filter { it.ruleFollowed.uppercase() == "NO" }
            ReviewFilter.LOW_QUALITY -> trades.filter { it.tradeQualityGrade in listOf("C", "D") }
            ReviewFilter.WITH_MISTAKES -> trades.filter { it.mistakeTags.isNotBlank() }
            ReviewFilter.NO_SCREENSHOT -> trades.filter { it.beforeScreenshotPath == null || it.afterScreenshotPath == null }
            ReviewFilter.NO_NOTES -> trades.filter { it.notes.isBlank() }
            ReviewFilter.BEST -> trades.sortedByDescending { it.netProfitLoss }.take(10)
            ReviewFilter.WORST -> trades.sortedBy { it.netProfitLoss }.take(10)
        }
        ReviewUiState(trades = filtered, filter = filter, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReviewUiState(isLoading = true))

    fun onFilterChanged(filter: ReviewFilter) {
        _filter.value = filter
    }

    fun completeReview(trade: TradeEntity, notes: String) {
        viewModelScope.launch {
            val updated = trade.copy(
                reviewCompleted = true,
                reviewDate = DateTimeUtils.getCurrentDeviceDate(),
                reviewNotes = notes,
                updatedAt = System.currentTimeMillis()
            )
            tradeRepository.updateTrade(updated)
        }
    }
}
