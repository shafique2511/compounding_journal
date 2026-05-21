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
            "Setup Type", "Emotion Before", "Emotion After", "Checklist Score", "Checklist Status",
            "Mistake Tags", "Mistake Made", "Lesson Learned", "Rule Followed", "Rule Broken Notes", 
            "Trade Quality Score", "Trade Quality Grade", "Review Completed", "Review Date", "Review Notes", "Notes"
        ).joinToString(",")

        val rows = trades.map { t ->
            listOf(
                t.tradeNumber, t.date, t.time, t.symbol, t.direction, t.timeframe,
                t.entryPrice, t.stopLoss, t.takeProfit, t.lotSize, t.riskAmount,
                t.grossProfitLoss, t.commission, t.swap, t.netProfitLoss,
                t.withdrawalAmount, t.startingBalance, t.endingBalance, t.growthPercent,
                t.riskRewardRatio, t.rMultiple, t.status, t.strategyName,
                t.setupType, t.emotionBefore, t.emotionAfter, t.checklistScore, t.checklistStatus,
                "\"${t.mistakeTags}\"", t.mistakeMade, "\"${t.lessonLearned.replace("\"", "\"\"")}\"", 
                t.ruleFollowed, "\"${t.ruleBrokenNotes.replace("\"", "\"\"")}\"",
                t.tradeQualityScore, t.tradeQualityGrade, t.reviewCompleted, t.reviewDate, "\"${t.reviewNotes.replace("\"", "\"\"")}\"",
                "\"${t.notes.replace("\"", "\"\"")}\""
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

    fun generateReviewReportCsv(trades: List<TradeEntity>): String {
        val header = "Trade #,Symbol,Date,Result,Grade,Checklist Score,Rule Followed,Reviewed,Review Date,Review Notes"
        val rows = trades.map { t ->
            listOf(
                t.tradeNumber, t.symbol, t.date, t.netProfitLoss, t.tradeQualityGrade,
                t.checklistScore, t.ruleFollowed, t.reviewCompleted, t.reviewDate, 
                "\"${t.reviewNotes.replace("\"", "\"\"")}\""
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun generateMistakeAnalysisCsv(mistakeStats: List<Pair<String, Int>>, financialImpact: Map<String, Double>): String {
        val header = "Mistake Tag,Occurrence Count,Net Profit/Loss Impact"
        val rows = mistakeStats.map { (tag, count) ->
            listOf(tag, count, financialImpact[tag] ?: 0.0).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun generateQualityAnalysisCsv(gradeStats: Map<String, Int>, winRates: Map<String, Double>): String {
        val header = "Quality Grade,Trade Count,Win Rate %"
        val rows = gradeStats.map { (grade, count) ->
            listOf(grade, count, winRates[grade] ?: 0.0).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun generateRiskReportCsv(warningCount: Int, followedProfit: Double, brokenProfit: Double): String {
        val header = "Risk Metric,Value"
        val rows = listOf(
            "Total Trades with Risk Warnings,$warningCount",
            "Net Profit (Risk Compliant Trades),$followedProfit",
            "Net Profit (Risk Violating Trades),$brokenProfit"
        )
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
}
