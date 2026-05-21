package com.example.compoundingjournal.utils

import android.content.Context
import android.net.Uri
import com.example.compoundingjournal.data.entity.StrategyEntity
import com.example.compoundingjournal.data.entity.TradeEntity
import java.io.OutputStreamWriter

object ExportUtils {

    fun generateTradesCsv(trades: List<TradeEntity>): String {
        val header = listOf(
            "Trade Number", "Date", "Time", "Symbol", "Direction", "Timeframe",
            "Entry Price", "Stop Loss", "Take Profit", "Lot Size", "Risk Amount",
            "Gross Profit/Loss", "Commission", "Swap", "Net Profit/Loss",
            "Withdrawal Amount", "Starting Balance", "Ending Balance", "Growth %",
            "Risk Reward Ratio", "R Multiple", "Status", "Strategy Name",
            "Setup Type", "Emotion Before", "Emotion After", "Mistake Tags", "Mistake Made",
            "Lesson Learned", "Trade Quality Score", "Trade Quality Grade", "Notes"
        ).joinToString(",")

        val rows = trades.map { t ->
            listOf(
                t.tradeNumber, t.date, t.time, t.symbol, t.direction, t.timeframe,
                t.entryPrice, t.stopLoss, t.takeProfit, t.lotSize, t.riskAmount,
                t.grossProfitLoss, t.commission, t.swap, t.netProfitLoss,
                t.withdrawalAmount, t.startingBalance, t.endingBalance, t.growthPercent,
                t.riskRewardRatio, t.rMultiple, t.status, t.strategyName,
                t.setupType, t.emotionBefore, t.emotionAfter, t.mistakeTags, t.mistakeMade,
                t.lessonLearned, t.tradeQualityScore, t.tradeQualityGrade, "\"${t.notes.replace("\"", "\"\"")}\""
            ).joinToString(",")
        }

        return (listOf(header) + rows).joinToString("\n")
    }

    fun writeTextToUri(context: Context, uri: Uri, text: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(text)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generateSummaryCsv(kpis: Map<String, String>): String {
        val header = "Metric,Value"
        val rows = kpis.map { (k, v) -> "$k,$v" }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun generateReviewReportCsv(trades: List<TradeEntity>): String {
        val header = "Trade #,Symbol,Result,Grade,Mistakes,Lesson,Reviewed,Review Date,Review Notes"
        val rows = trades.map { t ->
            listOf(
                t.tradeNumber, t.symbol, t.netProfitLoss, t.tradeQualityGrade,
                "\"${t.mistakeTags}\"", "\"${t.lessonLearned}\"",
                t.reviewCompleted, t.reviewDate, "\"${t.reviewNotes.replace("\"", "\"\"")}\""
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun generateStrategyCsv(strategies: List<StrategyEntity>): String {
        val header = "Name,Market,Timeframe,Active,Entry Rules,Exit Rules,SL Rules,TP Rules,Risk Rules,Notes"
        val rows = strategies.map { s ->
            listOf(
                s.strategyName, s.marketType, s.timeframe, s.isActive,
                "\"${s.entryRules.replace("\"", "\"\"")}\"",
                "\"${s.exitRules.replace("\"", "\"\"")}\"",
                "\"${s.stopLossRules.replace("\"", "\"\"")}\"",
                "\"${s.takeProfitRules.replace("\"", "\"\"")}\"",
                "\"${s.riskRules.replace("\"", "\"\"")}\"",
                "\"${s.notes.replace("\"", "\"\"")}\""
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}
