package com.example.compoundingjournal.utils

import com.example.compoundingjournal.data.entity.TradeEntity
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CalculationUtils {

    fun calculateNetProfitLoss(
        grossProfitLoss: Double,
        commission: Double,
        swap: Double
    ): Double {
        return grossProfitLoss - commission - swap
    }

    fun calculateEndingBalance(
        startingBalance: Double,
        netProfitLoss: Double,
        withdrawalAmount: Double
    ): Double {
        return startingBalance + netProfitLoss - withdrawalAmount
    }

    fun calculateGrowthPercent(
        netProfitLoss: Double,
        startingBalance: Double
    ): Double {
        if (startingBalance == 0.0) return 0.0
        return (netProfitLoss / startingBalance) * 100
    }

    fun calculateRiskRewardRatio(
        direction: String,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double
    ): Double {
        val risk = if (direction.equals("BUY", ignoreCase = true)) {
            entryPrice - stopLoss
        } else {
            stopLoss - entryPrice
        }

        if (risk <= 0) return 0.0

        val reward = if (direction.equals("BUY", ignoreCase = true)) {
            takeProfit - entryPrice
        } else {
            entryPrice - takeProfit
        }

        return reward / risk
    }

    fun calculateRMultiple(
        netProfitLoss: Double,
        riskAmount: Double
    ): Double {
        if (riskAmount == 0.0) return 0.0
        return netProfitLoss / riskAmount
    }

    fun calculateWinRate(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        val wins = trades.count { it.status.equals("WIN", ignoreCase = true) }
        return (wins.toDouble() / trades.size) * 100
    }

    fun calculateLossRate(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        val losses = trades.count { it.status.equals("LOSS", ignoreCase = true) }
        return (losses.toDouble() / trades.size) * 100
    }

    fun calculateProfitFactor(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        val totalProfit = trades.filter { it.netProfitLoss > 0 }.sumOf { it.netProfitLoss }
        val totalLoss = abs(trades.filter { it.netProfitLoss < 0 }.sumOf { it.netProfitLoss })
        
        if (totalLoss == 0.0) return if (totalProfit > 0) totalProfit else 0.0
        return totalProfit / totalLoss
    }

    fun calculateMaxDrawdown(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        
        val sortedTrades = trades.sortedBy { it.timestamp }
        var peak = 0.0
        var currentEquity = 0.0
        var maxDrawdown = 0.0

        if (sortedTrades.isNotEmpty()) {
            currentEquity = sortedTrades.first().startingBalance
            peak = currentEquity
        }

        for (trade in sortedTrades) {
            currentEquity = trade.endingBalance
            if (currentEquity > peak) {
                peak = currentEquity
            }
            val drawdown = peak - currentEquity
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown
            }
        }
        
        return maxDrawdown
    }

    fun calculateCurrentStreak(trades: List<TradeEntity>): Int {
        if (trades.isEmpty()) return 0
        val sortedTrades = trades.sortedByDescending { it.timestamp }
        val firstTradeResult = sortedTrades.first().netProfitLoss > 0
        var streak = 0
        
        for (trade in sortedTrades) {
            val isWin = trade.netProfitLoss > 0
            if (isWin == firstTradeResult) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    fun calculateLongestWinStreak(trades: List<TradeEntity>): Int {
        if (trades.isEmpty()) return 0
        val sortedTrades = trades.sortedBy { it.timestamp }
        var maxStreak = 0
        var currentStreak = 0
        
        for (trade in sortedTrades) {
            if (trade.status.equals("WIN", ignoreCase = true)) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else {
                currentStreak = 0
            }
        }
        return maxStreak
    }

    fun calculateLongestLossStreak(trades: List<TradeEntity>): Int {
        if (trades.isEmpty()) return 0
        val sortedTrades = trades.sortedBy { it.timestamp }
        var maxStreak = 0
        var currentStreak = 0
        
        for (trade in sortedTrades) {
            if (trade.status.equals("LOSS", ignoreCase = true)) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else {
                currentStreak = 0
            }
        }
        return maxStreak
    }

    fun calculateAverageRMultiple(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        val sumR = trades.sumOf { it.rMultiple }
        return sumR / trades.size
    }

    fun calculateBestTrade(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        return trades.maxOf { it.netProfitLoss }
    }

    fun calculateWorstTrade(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        return trades.minOf { it.netProfitLoss }
    }

    // New Functions for Phase 2 Update

    fun calculateChecklistScore(checkedCount: Int, totalCount: Int): Double {
        if (totalCount == 0) return 0.0
        return (checkedCount.toDouble() / totalCount) * 100
    }

    fun calculateChecklistStatus(checklistScore: Double): String {
        return if (checklistScore >= 80.0) "Plan Passed" else "Plan Warning"
    }

    fun calculateTradeQualityScore(
        checklistScore: Double,
        ruleFollowed: String,
        riskRewardRatio: Double,
        rMultiple: Double,
        mistakeTags: String,
        notes: String,
        beforeScreenshotPath: String?
    ): Double {
        var score = 100.0

        if (checklistScore < 80.0) score -= 20.0
        
        when (ruleFollowed.uppercase()) {
            "NO" -> score -= 15.0
            "PARTIALLY" -> score -= 10.0
        }

        if (riskRewardRatio < 1.5) score -= 15.0
        if (rMultiple < 0.0) score -= 15.0
        if (mistakeTags.isNotBlank()) score -= 10.0
        if (notes.isBlank()) score -= 10.0
        if (beforeScreenshotPath == null) score -= 10.0

        return max(0.0, min(100.0, score))
    }

    fun calculateTradeQualityGrade(score: Double): String {
        return when {
            score >= 90.0 -> "A+"
            score >= 80.0 -> "A"
            score >= 70.0 -> "B"
            score >= 60.0 -> "C"
            else -> "D"
        }
    }

    fun calculateRuleDisciplineScore(trades: List<TradeEntity>): Double {
        val completedTrades = trades.filter { 
            it.status.uppercase() in listOf("WIN", "LOSS", "BREAKEVEN") 
        }
        if (completedTrades.isEmpty()) return 0.0
        
        val followedCount = completedTrades.count { it.ruleFollowed.equals("YES", ignoreCase = true) }
        return (followedCount.toDouble() / completedTrades.size) * 100
    }

    // Risk Functions

    fun calculateDailyLossUsed(trades: List<TradeEntity>, date: String, baseBalance: Double): Double {
        if (baseBalance <= 0.0) return 0.0
        val dailyLoss = trades.filter { it.date == date && it.netProfitLoss < 0 }
            .sumOf { abs(it.netProfitLoss) }
        return (dailyLoss / baseBalance) * 100
    }

    fun calculateWeeklyLossUsed(trades: List<TradeEntity>, weekOfYear: Int, year: Int, baseBalance: Double): Double {
        if (baseBalance <= 0.0) return 0.0
        val calendar = Calendar.getInstance()
        val weeklyLoss = trades.filter { 
            calendar.timeInMillis = it.timestamp
            calendar.get(Calendar.WEEK_OF_YEAR) == weekOfYear && 
            calendar.get(Calendar.YEAR) == year &&
            it.netProfitLoss < 0
        }.sumOf { abs(it.netProfitLoss) }
        return (weeklyLoss / baseBalance) * 100
    }

    fun checkRiskPerTradeWarning(riskPercent: Double, maxAllowedPercent: Double): Boolean {
        return riskPercent > maxAllowedPercent
    }

    fun checkDailyLossWarning(currentLossPercent: Double, maxAllowedPercent: Double): Boolean {
        return currentLossPercent >= maxAllowedPercent
    }

    fun checkWeeklyLossWarning(currentLossPercent: Double, maxAllowedPercent: Double): Boolean {
        return currentLossPercent >= maxAllowedPercent
    }

    fun checkMaxTradesPerDayWarning(currentTradesCount: Int, maxAllowed: Int): Boolean {
        return currentTradesCount >= maxAllowed
    }

    fun checkLosingStreakWarning(currentLossStreak: Int, maxAllowed: Int): Boolean {
        return currentLossStreak >= maxAllowed
    }

    fun checkMinimumRiskRewardWarning(currentRR: Double, minimumRR: Double): Boolean {
        return currentRR < minimumRR
    }

    fun calculateRiskWarningCount(
        dailyLossUsed: Double, maxDaily: Double,
        weeklyLossUsed: Double, maxWeekly: Double,
        tradesToday: Int, maxTrades: Int,
        lossStreak: Int, maxStreak: Int
    ): Int {
        var count = 0
        if (dailyLossUsed >= maxDaily) count++
        if (weeklyLossUsed >= maxWeekly) count++
        if (tradesToday >= maxTrades) count++
        if (lossStreak >= maxStreak) count++
        return count
    }

    // Chart Functions

    fun calculateEquityCurve(trades: List<TradeEntity>, initialBalance: Double): List<Pair<Long, Double>> {
        val sortedTrades = trades.sortedBy { it.timestamp }
        var currentBalance = initialBalance
        val points = mutableListOf<Pair<Long, Double>>()
        
        // Starting point
        if (sortedTrades.isNotEmpty()) {
            points.add(sortedTrades.first().timestamp - 1 to initialBalance)
        }

        for (trade in sortedTrades) {
            currentBalance = trade.endingBalance
            points.add(trade.timestamp to currentBalance)
        }
        return points
    }

    fun calculateDrawdownSeries(trades: List<TradeEntity>, initialBalance: Double): List<Pair<Long, Double>> {
        val sortedTrades = trades.sortedBy { it.timestamp }
        var peak = initialBalance
        var currentBalance = initialBalance
        val points = mutableListOf<Pair<Long, Double>>()

        for (trade in sortedTrades) {
            currentBalance = trade.endingBalance
            if (currentBalance > peak) peak = currentBalance
            
            val drawdown = if (peak > 0) ((peak - currentBalance) / peak) * 100 else 0.0
            points.add(trade.timestamp to drawdown)
        }
        return points
    }

    fun calculateCumulativeProfit(trades: List<TradeEntity>): List<Pair<Long, Double>> {
        val sortedTrades = trades.sortedBy { it.timestamp }
        var cumulative = 0.0
        val points = mutableListOf<Pair<Long, Double>>()

        for (trade in sortedTrades) {
            cumulative += trade.netProfitLoss
            points.add(trade.timestamp to cumulative)
        }
        return points
    }

    // Analytics Functions

    fun calculateMistakeTagStats(trades: List<TradeEntity>): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        trades.forEach { trade ->
            trade.mistakeTags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                val cleanTag = tag.trim()
                stats[cleanTag] = stats.getOrDefault(cleanTag, 0) + 1
            }
        }
        return stats.toList().sortedByDescending { it.second }.toMap()
    }

    fun calculateQualityGradeStats(trades: List<TradeEntity>): Map<String, Int> {
        return trades.groupBy { it.tradeQualityGrade }
            .mapValues { it.value.size }
    }

    fun calculateStrategyStats(trades: List<TradeEntity>): Map<String, Double> {
        return trades.groupBy { it.strategyName }
            .mapValues { entry -> entry.value.sumOf { it.netProfitLoss } }
    }

    fun calculateRuleFollowedStats(trades: List<TradeEntity>): Map<String, Int> {
        return trades.groupBy { it.ruleFollowed.uppercase() }
            .mapValues { it.value.size }
    }
}
