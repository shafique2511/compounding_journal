package com.example.compoundingjournal.presentation.addtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundingjournal.data.entity.StrategyEntity
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.StrategyRepository
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.utils.CalculationUtils
import com.example.compoundingjournal.utils.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class TradeViewModel(
    private val tradeRepository: TradeRepository,
    private val settingsRepository: SettingsRepository,
    private val strategyRepository: StrategyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeFormState())
    val uiState: StateFlow<TradeFormState> = _uiState.asStateFlow()

    private val _strategies = MutableStateFlow<List<StrategyEntity>>(emptyList())
    val strategies = _strategies.asStateFlow()

    private var editingTradeId: Long? = null

    init {
        viewModelScope.launch {
            strategyRepository.getActiveStrategies().collect {
                _strategies.value = it
            }
        }
    }

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
                        afterScreenshotPath = t.afterScreenshotPath,
                        checklistTrendConfirmed = t.checklistTrendConfirmed,
                        checklistKeyLevelConfirmed = t.checklistKeyLevelConfirmed,
                        checklistEntryReasonConfirmed = t.checklistEntryReasonConfirmed,
                        checklistStopLossPlanned = t.checklistStopLossPlanned,
                        checklistTakeProfitPlanned = t.checklistTakeProfitPlanned,
                        checklistRiskAccepted = t.checklistRiskAccepted,
                        checklistNoRevengeTrade = t.checklistNoRevengeTrade,
                        checklistNoOverlot = t.checklistNoOverlot,
                        checklistNewsChecked = t.checklistNewsChecked,
                        checklistEmotionStable = t.checklistEmotionStable,
                        ruleFollowed = t.ruleFollowed,
                        ruleBrokenNotes = t.ruleBrokenNotes,
                        mistakeTags = t.mistakeTags,
                        tradeQualityScore = t.tradeQualityScore,
                        tradeQualityGrade = t.tradeQualityGrade,
                        reviewCompleted = t.reviewCompleted,
                        reviewDate = t.reviewDate,
                        reviewNotes = t.reviewNotes
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
            is TradeFormEvent.BeforeScreenshotPicked -> {
                _uiState.update { it.copy(beforeScreenshotPath = event.uri) }
                calculateValues()
            }
            is TradeFormEvent.AfterScreenshotPicked -> _uiState.update { it.copy(afterScreenshotPath = event.uri) }
            
            is TradeFormEvent.ChecklistTrendChanged -> {
                _uiState.update { it.copy(checklistTrendConfirmed = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistKeyLevelChanged -> {
                _uiState.update { it.copy(checklistKeyLevelConfirmed = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistEntryReasonChanged -> {
                _uiState.update { it.copy(checklistEntryReasonConfirmed = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistStopLossChanged -> {
                _uiState.update { it.copy(checklistStopLossPlanned = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistTakeProfitChanged -> {
                _uiState.update { it.copy(checklistTakeProfitPlanned = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistRiskAcceptedChanged -> {
                _uiState.update { it.copy(checklistRiskAccepted = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistNoRevengeTradeChanged -> {
                _uiState.update { it.copy(checklistNoRevengeTrade = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistNoOverlotChanged -> {
                _uiState.update { it.copy(checklistNoOverlot = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistNewsCheckedClicked -> {
                _uiState.update { it.copy(checklistNewsChecked = event.value) }
                calculateValues()
            }
            is TradeFormEvent.ChecklistEmotionStableChanged -> {
                _uiState.update { it.copy(checklistEmotionStable = event.value) }
                calculateValues()
            }
            is TradeFormEvent.RuleFollowedChanged -> {
                _uiState.update { it.copy(ruleFollowed = event.value) }
                calculateValues()
            }
            is TradeFormEvent.RuleBrokenNotesChanged -> _uiState.update { it.copy(ruleBrokenNotes = event.value) }
            
            is TradeFormEvent.MistakeTagsChanged -> {
                _uiState.update { it.copy(mistakeTags = event.value) }
                calculateValues()
            }
            
            TradeFormEvent.Reset -> {
                _uiState.update { TradeFormState() }
                initForAdd()
            }
            is TradeFormEvent.SaveTrade -> {
                viewModelScope.launch {
                    val settings = settingsRepository.getSettings().first()
                    if (settings?.enableRiskWarning == true) {
                        val warnings = checkRiskRules(settings)
                        if (warnings.isNotEmpty()) {
                            _uiState.update { it.copy(riskWarningMessages = warnings, showRiskWarning = true) }
                        } else if (_uiState.value.checklistScore < 80.0) {
                            _uiState.update { it.copy(showChecklistWarning = true) }
                        } else {
                            saveTrade(event.onSuccess)
                        }
                    } else if (_uiState.value.checklistScore < 80.0) {
                        _uiState.update { it.copy(showChecklistWarning = true) }
                    } else {
                        saveTrade(event.onSuccess)
                    }
                }
            }
            is TradeFormEvent.ConfirmSaveWeakPlan -> {
                _uiState.update { it.copy(showChecklistWarning = false) }
                saveTrade(event.onSuccess)
            }
            is TradeFormEvent.ConfirmSaveRiskWarning -> {
                _uiState.update { it.copy(showRiskWarning = false) }
                if (_uiState.value.checklistScore < 80.0) {
                    _uiState.update { it.copy(showChecklistWarning = true) }
                } else {
                    saveTrade(event.onSuccess)
                }
            }
            TradeFormEvent.DismissWarning -> _uiState.update { it.copy(showChecklistWarning = false, showRiskWarning = false) }
        }
    }

    private suspend fun checkRiskRules(settings: com.example.compoundingjournal.data.entity.SettingsEntity): List<String> {
        val warnings = mutableListOf<String>()
        val state = _uiState.value
        val trades = tradeRepository.getAllTrades().first()
        val startBalance = state.startingBalance.toDoubleOrNull() ?: 1.0
        val entry = state.entryPrice.toDoubleOrNull() ?: 0.0
        val sl = state.stopLoss.toDoubleOrNull() ?: 0.0
        val lotSize = state.lotSize.toDoubleOrNull() ?: 0.0
        val riskAmount = abs(entry - sl) * lotSize

        // Risk Per Trade %
        val riskPercent = if (startBalance > 0) (riskAmount / startBalance) * 100 else 0.0
        if (CalculationUtils.checkRiskPerTradeWarning(riskPercent, settings.maxRiskPerTradePercent)) {
            warnings.add("Risk per trade (${String.format("%.2f", riskPercent)}%) is above limit (${settings.maxRiskPerTradePercent}%)")
        }

        // Minimum RR
        val rr = state.riskRewardRatio.toDoubleOrNull() ?: 0.0
        if (CalculationUtils.checkMinimumRiskRewardWarning(rr, settings.minimumRiskRewardRatio)) {
            warnings.add("Risk Reward Ratio (${String.format("%.2f", rr)}) is below minimum (${settings.minimumRiskRewardRatio})")
        }

        // Max Trades Per Day
        val tradesTodayCount = trades.count { it.date == state.date }
        if (CalculationUtils.checkMaxTradesPerDayWarning(tradesTodayCount, settings.maxTradesPerDay)) {
            warnings.add("Maximum trades per day (${settings.maxTradesPerDay}) reached")
        }

        // Daily Loss
        val dailyLossUsed = CalculationUtils.calculateDailyLossUsed(trades, state.date, startBalance)
        if (CalculationUtils.checkDailyLossWarning(dailyLossUsed, settings.maxDailyLossPercent)) {
            warnings.add("Daily loss limit (${settings.maxDailyLossPercent}%) reached or exceeded")
        }

        // Weekly Loss
        val calendar = java.util.Calendar.getInstance()
        val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        val year = calendar.get(java.util.Calendar.YEAR)
        val weeklyLossUsed = CalculationUtils.calculateWeeklyLossUsed(trades, week, year, startBalance)
        if (CalculationUtils.checkWeeklyLossWarning(weeklyLossUsed, settings.maxWeeklyLossPercent)) {
            warnings.add("Weekly loss limit (${settings.maxWeeklyLossPercent}%) reached or exceeded")
        }

        // Losing Streak
        val currentLossStreak = CalculationUtils.calculateCurrentLossStreak(trades)
        if (CalculationUtils.checkLosingStreakWarning(currentLossStreak, settings.maxLosingStreakWarning)) {
            warnings.add("Current losing streak ($currentLossStreak losses) is at or above limit (${settings.maxLosingStreakWarning})")
        }

        return warnings
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
        val lotSize = state.lotSize.toDoubleOrNull() ?: 0.0
        
        val riskAmount = abs(entry - sl) * lotSize
        val rewardAmount = abs(tp - entry) * lotSize
        
        val rr = CalculationUtils.calculateRiskRewardRatio(state.direction, entry, sl, tp)
        val rMultiple = CalculationUtils.calculateRMultiple(net, if (riskAmount == 0.0) 1.0 else riskAmount)

        val checklistItems = listOf(
            state.checklistTrendConfirmed, state.checklistKeyLevelConfirmed,
            state.checklistEntryReasonConfirmed, state.checklistStopLossPlanned,
            state.checklistTakeProfitPlanned, state.checklistRiskAccepted,
            state.checklistNoRevengeTrade, state.checklistNoOverlot,
            state.checklistNewsChecked, state.checklistEmotionStable
        )
        val checkedCount = checklistItems.count { it }
        val checklistScore = CalculationUtils.calculateChecklistScore(checkedCount, checklistItems.size)
        val checklistStatus = CalculationUtils.calculateChecklistStatus(checklistScore)

        val qScore = CalculationUtils.calculateTradeQualityScore(
            checklistScore = checklistScore,
            ruleFollowed = state.ruleFollowed,
            riskRewardRatio = rr,
            rMultiple = rMultiple,
            mistakeTags = state.mistakeTags,
            notes = state.notes,
            beforeScreenshotPath = state.beforeScreenshotPath
        )
        val qGrade = CalculationUtils.calculateTradeQualityGrade(qScore)

        _uiState.update { 
            it.copy(
                netProfitLoss = String.format("%.2f", net),
                endingBalance = String.format("%.2f", end),
                growthPercent = String.format("%.2f", growth),
                riskRewardRatio = String.format("%.2f", rr),
                rMultiple = String.format("%.2f", rMultiple),
                checklistScore = checklistScore,
                checklistStatus = checklistStatus,
                tradeQualityScore = qScore,
                tradeQualityGrade = qGrade
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
            val originalTrade = if (editingTradeId != null) tradeRepository.getTradeById(editingTradeId!!) else null
            val tradeNumber = if (editingTradeId == null) (lastTrade?.tradeNumber ?: 0) + 1 else originalTrade?.tradeNumber ?: 1

            val trade = TradeEntity(
                id = editingTradeId ?: 0,
                tradeNumber = tradeNumber,
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
                riskAmount = abs((state.entryPrice.toDoubleOrNull() ?: 0.0) - (state.stopLoss.toDoubleOrNull() ?: 0.0)) * (state.lotSize.toDoubleOrNull() ?: 0.0),
                rewardAmount = abs((state.takeProfit.toDoubleOrNull() ?: 0.0) - (state.entryPrice.toDoubleOrNull() ?: 0.0)) * (state.lotSize.toDoubleOrNull() ?: 0.0),
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
                createdAt = originalTrade?.createdAt ?: DateTimeUtils.getCurrentTimestamp(),
                updatedAt = DateTimeUtils.getCurrentTimestamp(),
                
                checklistTrendConfirmed = state.checklistTrendConfirmed,
                checklistKeyLevelConfirmed = state.checklistKeyLevelConfirmed,
                checklistEntryReasonConfirmed = state.checklistEntryReasonConfirmed,
                checklistStopLossPlanned = state.checklistStopLossPlanned,
                checklistTakeProfitPlanned = state.checklistTakeProfitPlanned,
                checklistRiskAccepted = state.checklistRiskAccepted,
                checklistNoRevengeTrade = state.checklistNoRevengeTrade,
                checklistNoOverlot = state.checklistNoOverlot,
                checklistNewsChecked = state.checklistNewsChecked,
                checklistEmotionStable = state.checklistEmotionStable,
                checklistScore = state.checklistScore,
                checklistStatus = state.checklistStatus,
                ruleFollowed = state.ruleFollowed,
                ruleBrokenNotes = state.ruleBrokenNotes,
                
                mistakeTags = state.mistakeTags,
                tradeQualityScore = state.tradeQualityScore,
                tradeQualityGrade = state.tradeQualityGrade,
                reviewCompleted = state.reviewCompleted,
                reviewDate = state.reviewDate,
                reviewNotes = state.reviewNotes
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
    
    data class ChecklistTrendChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistKeyLevelChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistEntryReasonChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistStopLossChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistTakeProfitChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistRiskAcceptedChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistNoRevengeTradeChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistNoOverlotChanged(val value: Boolean) : TradeFormEvent()
    data class ChecklistNewsCheckedClicked(val value: Boolean) : TradeFormEvent()
    data class ChecklistEmotionStableChanged(val value: Boolean) : TradeFormEvent()
    data class RuleFollowedChanged(val value: String) : TradeFormEvent()
    data class RuleBrokenNotesChanged(val value: String) : TradeFormEvent()
    
    data class MistakeTagsChanged(val value: String) : TradeFormEvent()
    
    data class SaveTrade(val onSuccess: () -> Unit) : TradeFormEvent()
    data class ConfirmSaveWeakPlan(val onSuccess: () -> Unit) : TradeFormEvent()
    data class ConfirmSaveRiskWarning(val onSuccess: () -> Unit) : TradeFormEvent()
    object DismissWarning : TradeFormEvent()
    object Reset : TradeFormEvent()
}
