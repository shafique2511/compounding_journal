package com.example.compoundingjournal.presentation.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.FilterPresetRepository
import com.example.compoundingjournal.data.repository.FilterPresetRepositoryImpl
import com.example.compoundingjournal.data.repository.SettingsRepository
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.EmptyState
import com.example.compoundingjournal.presentation.components.SectionCard
import com.example.compoundingjournal.utils.ColorUtils
import com.example.compoundingjournal.utils.CalculationUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val settingsRepository = remember { SettingsRepositoryImpl(database.settingsDao()) }
    val presetRepository = remember { FilterPresetRepositoryImpl(database.filterPresetDao()) }
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(repository, settingsRepository, presetRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val presets by viewModel.presets.collectAsState()
    var showPresetMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Deep Analytics",
                actions = {
                    if (presets.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showPresetMenu = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Presets")
                            }
                            DropdownMenu(expanded = showPresetMenu, onDismissRequest = { showPresetMenu = false }) {
                                presets.forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset.presetName) },
                                        onClick = {
                                            viewModel.applyPreset(preset)
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.trades.isEmpty()) {
            EmptyState(
                message = "Not enough trade data to generate analytics.\nStart journaling to see insights here."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    SectionCard("Performance Summary") {
                        val s = uiState.summary
                        AnalyticsRow("Total Trades", s.totalTrades.toString())
                        AnalyticsRow("Wins / Losses", "${s.wins} / ${s.losses}")
                        AnalyticsRow("Win Rate", "${String.format("%.1f", s.winRate)}%", isBold = true)
                        AnalyticsRow("Avg Quality Score", "${uiState.averageQualityScore.toInt()}/100", color = ColorUtils.getGradeColor(CalculationUtils.calculateTradeQualityGrade(uiState.averageQualityScore)))
                        AnalyticsRow("Profit Factor", String.format("%.2f", s.profitFactor))
                        AnalyticsRow("Net Profit", String.format("%.2f", s.netProfit), color = if (s.netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336), isBold = true)
                        AnalyticsRow("Avg Profit / Loss", "${String.format("%.2f", s.averageProfit)} / ${String.format("%.2f", s.averageLoss)}")
                    }
                }

                item {
                    SectionCard("Risk Rule Analysis") {
                        val rs = uiState.riskRuleAnalysis
                        AnalyticsRow("Trades with Warnings", rs.tradesWithWarnings.toString(), if (rs.tradesWithWarnings > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Risk-Compliant Trades:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        AnalyticsRow("Win Rate", "${String.format("%.1f", rs.followedRulesWinRate)}%")
                        AnalyticsRow("Net Profit", String.format("%.2f", rs.followedRulesNetProfit), color = if (rs.followedRulesNetProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Trades with Risk Violations:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF44336))
                        AnalyticsRow("Win Rate", "${String.format("%.1f", rs.brokeRulesWinRate)}%")
                        AnalyticsRow("Net Profit", String.format("%.2f", rs.brokeRulesNetProfit), color = if (rs.brokeRulesNetProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
                    }
                }

                item {
                    SectionCard("Review Analysis") {
                        val ra = uiState.reviewAnalysis
                        AnalyticsRow("Reviewed Trades", ra.reviewedCount.toString())
                        AnalyticsRow("Unreviewed Losses", ra.unreviewedLosingCount.toString(), if (ra.unreviewedLosingCount > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Top Recurring Patterns:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        AnalyticsRow("Top Mistake", ra.mostRepeatedMistake)
                        AnalyticsRow("Top Lesson", ra.mostRepeatedLesson)
                    }
                }

                item {
                    SectionCard("Trade Quality Analysis") {
                        uiState.qualityAnalysis.forEach { stat ->
                            ExpandableAnalyticItem(
                                title = "Grade ${stat.grade}",
                                subtitle = "Count: ${stat.count} • Win Rate: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                colorOverride = ColorUtils.getGradeColor(stat.grade),
                                details = {
                                    AnalyticsRow("Trades in Grade", stat.count.toString())
                                    AnalyticsRow("Cumulative Impact", String.format("%.2f", stat.netProfit))
                                }
                            )
                        }
                    }
                }

                item {
                    SectionCard("Risk & Drawdown") {
                        val r = uiState.riskAnalysis
                        AnalyticsRow("Avg Risk Per Trade", String.format("%.2f", r.averageRisk))
                        AnalyticsRow("Avg R Multiple", String.format("%.2f", r.averageR), isBold = true)
                        AnalyticsRow("Best / Worst R", "${String.format("%.2f", r.bestR)} / ${String.format("%.2f", r.worstR)}")
                        AnalyticsRow("Maximum Drawdown", "${String.format("%.2f", r.maxDrawdown)}%", color = Color(0xFFFF9800))
                    }
                }

                item {
                    SectionCard("Mistake Analysis") {
                        if (uiState.mistakeAnalysis.isEmpty()) {
                            Text("No mistake tags selected in your trades.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            val topMistake = uiState.mistakeAnalysis.first()
                            Text(
                                text = "Most Common: ${topMistake.tag} (${topMistake.count})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            uiState.mistakeAnalysis.forEach { stat ->
                                ExpandableAnalyticItem(
                                    title = stat.tag,
                                    subtitle = "Count: ${stat.count} • Win Rate: ${String.format("%.1f", stat.winRate)}%",
                                    mainValue = String.format("%.2f", stat.netProfit),
                                    details = {
                                        AnalyticsRow("Total Impact", String.format("%.2f", stat.netProfit))
                                        AnalyticsRow("Avg Impact", String.format("%.2f", stat.netProfit / stat.count))
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard("Timeframe Breakdown") {
                        uiState.timeframeAnalysis.forEach { stat ->
                            ExpandableAnalyticItem(
                                title = stat.timeframe,
                                subtitle = "Trades: ${stat.totalTrades} • WR: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    AnalyticsRow("Average R", String.format("%.2f", stat.averageR))
                                }
                            )
                        }
                    }
                }

                item {
                    SectionCard("Asset Performance") {
                        uiState.symbolAnalysis.forEach { stat ->
                            ExpandableAnalyticItem(
                                title = stat.symbol,
                                subtitle = "Trades: ${stat.totalTrades} • Win Rate: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    AnalyticsRow("Best Individual Trade", String.format("%.2f", stat.bestTrade))
                                    AnalyticsRow("Worst Individual Trade", String.format("%.2f", stat.worstTrade))
                                }
                            )
                        }
                    }
                }

                item {
                    SectionCard("Strategy Insights") {
                        uiState.strategyAnalysis.forEach { stat ->
                            ExpandableAnalyticItem(
                                title = stat.strategy,
                                subtitle = "Trades: ${stat.totalTrades} • Win Rate: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    AnalyticsRow("Average R-Multiple", String.format("%.2f", stat.averageR))
                                    AnalyticsRow("Profit Factor", String.format("%.2f", stat.profitFactor))
                                    AnalyticsRow("Max Drawdown", String.format("%.2f", stat.maxDrawdown))
                                    AnalyticsRow("Best / Worst Trade", "${String.format("%.2f", stat.bestTrade)} / ${String.format("%.2f", stat.worstTrade)}")
                                }
                            )
                        }
                    }
                }

                item {
                    SectionCard("Monthly Progress") {
                        uiState.monthlyAnalysis.forEach { stat ->
                            ExpandableAnalyticItem(
                                title = stat.month,
                                subtitle = "Trades: ${stat.totalTrades} • Withdrawals: ${stat.withdrawals}",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    AnalyticsRow("Ending Balance", String.format("%.2f", stat.endingBalance))
                                }
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AnalyticsRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold, 
            color = color
        )
    }
}

@Composable
fun ExpandableAnalyticItem(
    title: String,
    subtitle: String,
    mainValue: String,
    colorOverride: Color? = null,
    details: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = colorOverride ?: MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mainValue, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = colorOverride ?: if (mainValue.startsWith("-")) Color(0xFFF44336) else if (mainValue == "0.00") Color.Gray else Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)) {
                details()
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

class AnalyticsViewModelFactory(
    private val repository: TradeRepository,
    private val settingsRepository: SettingsRepository,
    private val presetRepository: FilterPresetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository, settingsRepository, presetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
