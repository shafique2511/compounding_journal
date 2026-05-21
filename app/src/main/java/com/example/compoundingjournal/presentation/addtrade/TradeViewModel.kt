package com.example.compoundingjournal.presentation.addtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
import com.example.compoundingjournal.utils.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TradeViewModel(
    private val tradeRepository: TradeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeFormState())
    val uiState: StateFlow<TradeFormState> = _uiState.asStateFlow()

    private var editingTradeId: Long? = null

    fun initForAdd() {
        viewModelScope.launch {
            val lastTrade = tradeRepository.getLastTrade()
            val settings = settingsRepository.getSettings().first()
            
            val startBalance = lastTrade?.endingBalance ?: settings?.initialBalance ?: 1000.0
            
            _uiState.update { 
                it.copy(
                    date = DateTimeUtils.getCurrentDeviceDate(),
                    time = DateTimeUtils.getCurrentDeviceTime(),
                    startingBalance = startBalance.toString(),
                    symbol = settings?.defaultSymbol ?: "",
                    timeframe = settings?.defaultTimeframe ?: "H1",
                    commission = (settings?.defaultCommission ?: 0.0).toString(),
                    swap = (settings?.defaultSwap ?: 0.0).toString()
                )
            }
            calculateValues()
        }
    }

    fun initForEdit(tradeId: Long) {
        editingTradeId = tradeId
        viewModelScope.launch {
            val trade = tradeRepository.getTradeById(tradeId)
            trade?.let { t ->
                _uiState.update { 
                    it.copy(
                        symbol = t.symbol,
                        direction = t.direction,
                        timeframe = t.timeframe,
                        date = t.date,
                        time = t.time,
                        status = t.status,
                        entryPrice = t.entryPrice.toString(),
                        stopLoss = t.stopLoss.toString(),
                        takeProfit = t.takeProfit.toString(),
                        lotSize = t.lotSize.toString(),
                        startingBalance = t.startingBalance.toString(),
                        grossProfitLoss = t.grossProfitLoss.toString(),
                        commission = t.commission.toString(),
                        swap = t.swap.toString(),
                        withdrawalAmount = t.withdrawalAmount.toString(),
                        strategyName = t.strategyName,
                        setupType = t.setupType,
                        emotionBefore = t.emotionBefore,
                        emotionAfter = t.emotionAfter,
                        mistakeMade = t.mistakeMade,
                        lessonLearned = t.lessonLearned,
                        notes = t.notes,
                        beforeScreenshotPath = t.beforeScreenshotPath,
                        afterScreenshotPath = t.afterScreenshotPath
                    )
                }
                calculateValues()
            }
        }
    }

    fun onEvent(event: TradeFormEvent) {
        when (event) {
            is TradeFormEvent.SymbolChanged -> _uiState.update { it.copy(symbol = event.value) }
            is TradeFormEvent.DirectionChanged -> {
                _uiState.update { it.copy(direction = event.value) }
                calculateValues()
            }
            is TradeFormEvent.TimeframeChanged -> _uiState.update { it.copy(timeframe = event.value) }
            is TradeFormEvent.DateChanged -> _uiState.update { it.copy(date = event.value) }
            is TradeFormEvent.TimeChanged -> _uiState.update { it.copy(time = event.value) }
            is TradeFormEvent.StatusChanged -> _uiState.update { it.copy(status = event.value) }
            is TradeFormEvent.EntryPriceChanged -> {
                _uiState.update { it.copy(entryPrice = event.value) }
                calculateValues()
            }
            is TradeFormEvent.StopLossChanged -> {
                _uiState.update { it.copy(stopLoss = event.value) }
                calculateValues()
            }
            is TradeFormEvent.TakeProfitChanged -> {
                _uiState.update { it.copy(takeProfit = event.value) }
                calculateValues()
            }
            is TradeFormEvent.LotSizeChanged -> _uiState.update { it.copy(lotSize = event.value) }
            is TradeFormEvent.StartingBalanceChanged -> {
                _uiState.update { it.copy(startingBalance = event.value) }
                calculateValues()
            }
            is TradeFormEvent.GrossProfitLossChanged -> {
                _uiState.update { it.copy(grossProfitLoss = event.value) }
                calculateValues()
            }
            is TradeFormEvent.CommissionChanged -> {
                _uiState.update { it.copy(commission = event.value) }
                calculateValues()
            }
            is TradeFormEvent.SwapChanged -> {
                _uiState.update { it.copy(swap = event.value) }
                calculateValues()
            }
            is TradeFormEvent.WithdrawalAmountChanged -> {
                _uiState.update { it.copy(withdrawalAmount = event.value) }
                calculateValues()
            }
            is TradeFormEvent.StrategyNameChanged -> _uiState.update { it.copy(strategyName = event.value) }
            is TradeFormEvent.SetupTypeChanged -> _uiState.update { it.copy(setupType = event.value) }
            is TradeFormEvent.EmotionBeforeChanged -> _uiState.update { it.copy(emotionBefore = event.value) }
            is TradeFormEvent.EmotionAfterChanged -> _uiState.update { it.copy(emotionAfter = event.value) }
            is TradeFormEvent.MistakeMadeChanged -> _uiState.update { it.copy(mistakeMade = event.value) }
            is TradeFormEvent.LessonLearnedChanged -> _uiState.update { it.copy(lessonLearned = event.value) }
            is TradeFormEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value) }
            is TradeFormEvent.BeforeScreenshotPicked -> _uiState.update { it.copy(beforeScreenshotPath = event.uri) }
            is TradeFormEvent.AfterScreenshotPicked -> _uiState.update { it.copy(afterScreenshotPath = event.uri) }
            TradeFormEvent.Reset -> {
                _uiState.update { TradeFormState() }
                initForAdd()
            }
            is TradeFormEvent.SaveTrade -> saveTrade(event.onSuccess)
        }
    }

    private fun calculateValues() {
        val state = _uiState.value
        val gross = state.grossProfitLoss.toDoubleOrNull() ?: 0.0
        val commission = state.commission.toDoubleOrNull() ?: 0.0
        val swap = state.swap.toDoubleOrNull() ?: 0.0
        val startBalance = state.startingBalance.toDoubleOrNull() ?: 0.0
        val withdrawal = state.withdrawalAmount.toDoubleOrNull() ?: 0.0
        
        val net = CalculationUtils.calculateNetProfitLoss(gross, commission, swap)
        val end = CalculationUtils.calculateEndingBalance(startBalance, net, withdrawal)
        val growth = CalculationUtils.calculateGrowthPercent(net, startBalance)
        
        val entry = state.entryPrice.toDoubleOrNull() ?: 0.0
        val sl = state.stopLoss.toDoubleOrNull() ?: 0.0
        val tp = state.takeProfit.toDoubleOrNull() ?: 0.0
        
        val rr = CalculationUtils.calculateRiskRewardRatio(state.direction, entry, sl, tp)
        
        val riskAmount = Math.abs(entry - sl) 
        val rMultiple = CalculationUtils.calculateRMultiple(net, if (riskAmount == 0.0) 1.0 else riskAmount)

        _uiState.update { 
            it.copy(
                netProfitLoss = String.format("%.2f", net),
                endingBalance = String.format("%.2f", end),
                growthPercent = String.format("%.2f", growth),
                riskRewardRatio = String.format("%.2f", rr),
                rMultiple = String.format("%.2f", rMultiple)
            )
        }
    }

    private fun saveTrade(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.symbol.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Symbol cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val lastTrade = tradeRepository.getLastTrade()
            val tradeNumber = if (editingTradeId == null) (lastTrade?.tradeNumber ?: 0) + 1 else 0 

            val trade = TradeEntity(
                id = editingTradeId ?: 0,
                tradeNumber = if (editingTradeId == null) tradeNumber else tradeRepository.getTradeById(editingTradeId!!)?.tradeNumber ?: 1,
                date = state.date,
                time = state.time,
                timestamp = DateTimeUtils.getCurrentTimestamp(),
                symbol = state.symbol,
                direction = state.direction,
                timeframe = state.timeframe,
                entryPrice = state.entryPrice.toDoubleOrNull() ?: 0.0,
                stopLoss = state.stopLoss.toDoubleOrNull() ?: 0.0,
                takeProfit = state.takeProfit.toDoubleOrNull() ?: 0.0,
                lotSize = state.lotSize.toDoubleOrNull() ?: 0.0,
                riskAmount = Math.abs((state.entryPrice.toDoubleOrNull() ?: 0.0) - (state.stopLoss.toDoubleOrNull() ?: 0.0)),
                rewardAmount = Math.abs((state.takeProfit.toDoubleOrNull() ?: 0.0) - (state.entryPrice.toDoubleOrNull() ?: 0.0)),
                grossProfitLoss = state.grossProfitLoss.toDoubleOrNull() ?: 0.0,
                commission = state.commission.toDoubleOrNull() ?: 0.0,
                swap = state.swap.toDoubleOrNull() ?: 0.0,
                netProfitLoss = state.netProfitLoss.toDouble(),
                withdrawalAmount = state.withdrawalAmount.toDoubleOrNull() ?: 0.0,
                startingBalance = state.startingBalance.toDoubleOrNull() ?: 0.0,
                endingBalance = state.endingBalance.toDouble(),
                growthPercent = state.growthPercent.toDouble(),
                riskRewardRatio = state.riskRewardRatio.toDouble(),
                rMultiple = state.rMultiple.toDouble(),
                status = state.status,
                strategyName = state.strategyName,
                setupType = state.setupType,
                emotionBefore = state.emotionBefore,
                emotionAfter = state.emotionAfter,
                mistakeMade = state.mistakeMade,
                lessonLearned = state.lessonLearned,
                notes = state.notes,
                beforeScreenshotPath = state.beforeScreenshotPath,
                afterScreenshotPath = state.afterScreenshotPath,
                createdAt = if (editingTradeId == null) DateTimeUtils.getCurrentTimestamp() else 0,
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            )

            if (editingTradeId == null) {
                tradeRepository.insertTrade(trade)
            } else {
                tradeRepository.updateTrade(trade)
                tradeRepository.recalculateSubsequentTrades()
            }
            
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}

sealed class TradeFormEvent {
    data class SymbolChanged(val value: String) : TradeFormEvent()
    data class DirectionChanged(val value: String) : TradeFormEvent()
    data class TimeframeChanged(val value: String) : TradeFormEvent()
    data class DateChanged(val value: String) : TradeFormEvent()
    data class TimeChanged(val value: String) : TradeFormEvent()
    data class StatusChanged(val value: String) : TradeFormEvent()
    data class EntryPriceChanged(val value: String) : TradeFormEvent()
    data class StopLossChanged(val value: String) : TradeFormEvent()
    data class TakeProfitChanged(val value: String) : TradeFormEvent()
    data class LotSizeChanged(val value: String) : TradeFormEvent()
    data class StartingBalanceChanged(val value: String) : TradeFormEvent()
    data class GrossProfitLossChanged(val value: String) : TradeFormEvent()
    data class CommissionChanged(val value: String) : TradeFormEvent()
    data class SwapChanged(val value: String) : TradeFormEvent()
    data class WithdrawalAmountChanged(val value: String) : TradeFormEvent()
    data class StrategyNameChanged(val value: String) : TradeFormEvent()
    data class SetupTypeChanged(val value: String) : TradeFormEvent()
    data class EmotionBeforeChanged(val value: String) : TradeFormEvent()
    data class EmotionAfterChanged(val value: String) : TradeFormEvent()
    data class MistakeMadeChanged(val value: String) : TradeFormEvent()
    data class LessonLearnedChanged(val value: String) : TradeFormEvent()
    data class NotesChanged(val value: String) : TradeFormEvent()
    data class BeforeScreenshotPicked(val uri: String?) : TradeFormEvent()
    data class AfterScreenshotPicked(val uri: String?) : TradeFormEvent()
    object SaveTrade : TradeFormEvent() {
        var onSuccess: () -> Unit = {}
    }
    object Reset : TradeFormEvent()
}
