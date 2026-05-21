package com.example.compoundingjournal.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
import com.example.compoundingjournal.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CalendarUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val tradesByDay: Map<String, List<TradeEntity>> = emptyMap(),
    val dailySummaries: Map<String, DailySummary> = emptyMap(),
    val selectedDate: String? = null,
    val selectedDayTrades: List<TradeEntity> = emptyList(),
    val selectedDaySummary: DailySummary? = null,
    val isLoading: Boolean = false
)

data class DailySummary(
    val date: String,
    val totalTrades: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val breakevens: Int = 0,
    val running: Int = 0,
    val cancelled: Int = 0,
    val netProfit: Double = 0.0,
    val withdrawals: Double = 0.0,
    val bestTrade: Double = 0.0,
    val worstTrade: Double = 0.0
)

class CalendarViewModel(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    })
    
    private val _selectedDate = MutableStateFlow<String?>(DateTimeUtils.getCurrentDeviceDate())

    val uiState: StateFlow<CalendarUiState> = combine(
        tradeRepository.getAllTrades(),
        _currentMonth,
        _selectedDate
    ) { allTrades, month, selectedDate ->
        
        val monthTrades = allTrades.filter { trade ->
            val tradeCal = Calendar.getInstance().apply { timeInMillis = trade.timestamp }
            tradeCal.get(Calendar.MONTH) == month.get(Calendar.MONTH) &&
            tradeCal.get(Calendar.YEAR) == month.get(Calendar.YEAR)
        }

        val grouped = monthTrades.groupBy { it.date }
        val summaries = grouped.mapValues { (date, trades) ->
            calculateDailySummary(date, trades)
        }

        CalendarUiState(
            currentMonth = month.clone() as Calendar,
            tradesByDay = grouped,
            dailySummaries = summaries,
            selectedDate = selectedDate,
            selectedDayTrades = grouped[selectedDate] ?: emptyList(),
            selectedDaySummary = summaries[selectedDate],
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState(isLoading = true))

    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun nextMonth() {
        _currentMonth.update { 
            val next = it.clone() as Calendar
            next.add(Calendar.MONTH, 1)
            next
        }
    }

    fun previousMonth() {
        _currentMonth.update { 
            val prev = it.clone() as Calendar
            prev.add(Calendar.MONTH, -1)
            prev
        }
    }

    private fun calculateDailySummary(date: String, trades: List<TradeEntity>): DailySummary {
        return DailySummary(
            date = date,
            totalTrades = trades.size,
            wins = trades.count { it.status.uppercase() == "WIN" },
            losses = trades.count { it.status.uppercase() == "LOSS" },
            breakevens = trades.count { it.status.uppercase() == "BREAKEVEN" },
            running = trades.count { it.status.uppercase() == "RUNNING" },
            cancelled = trades.count { it.status.uppercase() == "CANCELLED" },
            netProfit = trades.sumOf { it.netProfitLoss },
            withdrawals = trades.sumOf { it.withdrawalAmount },
            bestTrade = if (trades.isNotEmpty()) trades.maxOf { it.netProfitLoss } else 0.0,
            worstTrade = if (trades.isNotEmpty()) trades.minOf { it.netProfitLoss } else 0.0
        )
    }
}
