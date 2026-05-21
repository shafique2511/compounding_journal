package com.example.compoundingjournal.presentation.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.EmptyState
import com.example.compoundingjournal.presentation.components.SectionCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Deep Analytics") }
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
                        AnalyticsRow("Profit Factor", String.format("%.2f", s.profitFactor))
                        AnalyticsRow("Net Profit", String.format("%.2f", s.netProfit), color = if (s.netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336), isBold = true)
                        AnalyticsRow("Avg Profit / Loss", "${String.format("%.2f", s.averageProfit)} / ${String.format("%.2f", s.averageLoss)}")
                    }
                }

                item {
                    SectionCard("Risk & Drawdown") {
                        val r = uiState.riskAnalysis
                        AnalyticsRow("Avg Risk Per Trade", String.format("%.2f", r.averageRisk))
                        AnalyticsRow("Avg R Multiple", String.format("%.2f", r.averageR), isBold = true)
                        AnalyticsRow("Best / Worst R", "${String.format("%.2f", r.bestR)} / ${String.format("%.2f", r.worstR)}")
                        AnalyticsRow("Maximum Drawdown", String.format("%.2f", r.maxDrawdown), color = Color(0xFFFF9800))
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
                                }
                            )
                        }
                    }
                }

                item {
                    SectionCard("Common Mistakes") {
                        if (uiState.commonMistakes.isEmpty()) {
                            Text("No mistakes documented in your notes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            uiState.commonMistakes.forEach { (mistake, count) ->
                                AnalyticsRow(mistake.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, "$count times")
                            }
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
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mainValue, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = if (mainValue.startsWith("-")) Color(0xFFF44336) else if (mainValue == "0.00") Color.Gray else Color(0xFF4CAF50)
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

class AnalyticsViewModelFactory(private val repository: TradeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
