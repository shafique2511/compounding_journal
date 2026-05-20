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
        topBar = { TopAppBar(title = { Text("Performance Analytics") }) }
    ) { padding ->
        if (uiState.trades.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Not enough data for analytics", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AnalyticsSection("Performance Summary") {
                        val s = uiState.summary
                        StatRow("Total Trades", s.totalTrades.toString())
                        StatRow("Wins / Losses", "${s.wins} / ${s.losses}")
                        StatRow("Win Rate", "${String.format("%.1f", s.winRate)}%")
                        StatRow("Profit Factor", String.format("%.2f", s.profitFactor))
                        StatRow("Net Profit", String.format("%.2f", s.netProfit), color = if (s.netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
                        StatRow("Avg Profit / Loss", "${String.format("%.2f", s.averageProfit)} / ${String.format("%.2f", s.averageLoss)}")
                    }
                }

                item {
                    AnalyticsSection("Risk Analysis") {
                        val r = uiState.riskAnalysis
                        StatRow("Avg Risk Per Trade", String.format("%.2f", r.averageRisk))
                        StatRow("Avg R Multiple", String.format("%.2f", r.averageR))
                        StatRow("Best / Worst R", "${String.format("%.2f", r.bestR)} / ${String.format("%.2f", r.worstR)}")
                        StatRow("Max Drawdown", String.format("%.2f", r.maxDrawdown))
                    }
                }

                item {
                    AnalyticsSection("Timeframe Performance") {
                        uiState.timeframeAnalysis.forEach { stat ->
                            ExpandableStatItem(
                                title = stat.timeframe,
                                subtitle = "Trades: ${stat.totalTrades} | Win Rate: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    StatRow("Average R", String.format("%.2f", stat.averageR))
                                }
                            )
                        }
                    }
                }

                item {
                    AnalyticsSection("Symbol Analysis") {
                        uiState.symbolAnalysis.forEach { stat ->
                            ExpandableStatItem(
                                title = stat.symbol,
                                subtitle = "Trades: ${stat.totalTrades} | WR: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    StatRow("Best Trade", String.format("%.2f", stat.bestTrade))
                                    StatRow("Worst Trade", String.format("%.2f", stat.worstTrade))
                                }
                            )
                        }
                    }
                }

                item {
                    AnalyticsSection("Strategy Analysis") {
                        uiState.strategyAnalysis.forEach { stat ->
                            ExpandableStatItem(
                                title = stat.strategy,
                                subtitle = "Trades: ${stat.totalTrades} | WR: ${String.format("%.1f", stat.winRate)}%",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    StatRow("Average R", String.format("%.2f", stat.averageR))
                                }
                            )
                        }
                    }
                }

                item {
                    AnalyticsSection("Mistake Analysis") {
                        if (uiState.commonMistakes.isEmpty()) {
                            Text("No mistakes recorded yet.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            uiState.commonMistakes.forEach { (mistake, count) ->
                                StatRow(mistake.replaceFirstChar { it.uppercase() }, "$count occurrences")
                            }
                        }
                    }
                }

                item {
                    AnalyticsSection("Monthly Analysis") {
                        uiState.monthlyAnalysis.forEach { stat ->
                            ExpandableStatItem(
                                title = stat.month,
                                subtitle = "Trades: ${stat.totalTrades} | Withdrawals: ${stat.withdrawals}",
                                mainValue = String.format("%.2f", stat.netProfit),
                                details = {
                                    StatRow("Ending Balance", String.format("%.2f", stat.endingBalance))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ExpandableStatItem(
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
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    mainValue, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = if (mainValue.startsWith("-")) Color(0xFFF44336) else if (mainValue == "0.00") Color.Gray else Color(0xFF4CAF50)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)) {
                details()
            }
        }
        Divider(thickness = 0.5.dp)
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
