package com.example.compoundingjournal.utils

import com.example.compoundingjournal.data.entity.TradeEntity
import kotlin.math.abs

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
        val wins = trades.count { it.netProfitLoss > 0 }
        return (wins.toDouble() / trades.size) * 100
    }

    fun calculateLossRate(trades: List<TradeEntity>): Double {
        if (trades.isEmpty()) return 0.0
        val losses = trades.count { it.netProfitLoss < 0 }
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
        
        // Sort trades by timestamp to reconstruct equity curve
        val sortedTrades = trades.sortedBy { it.timestamp }
        var peak = 0.0
        var currentEquity = 0.0
        var maxDrawdown = 0.0

        // We use relative equity starting from 0 to find max dip from peak
        // Or we could use the startingBalance of the first trade
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
            if (trade.netProfitLoss > 0) {
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
            if (trade.netProfitLoss < 0) {
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
}
