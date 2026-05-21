package com.example.compoundingjournal.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.ChartCard
import com.example.compoundingjournal.presentation.components.KpiCard
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val tradeRepository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val settingsRepository = remember { SettingsRepositoryImpl(database.settingsDao()) }
    
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(tradeRepository, settingsRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(uiState.isRefreshing, { viewModel.refresh() })

    Scaffold(
        topBar = {
            AppTopBar(title = "Dashboard")
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DateRangeFilters(
                        selectedOption = uiState.filters.dateRange,
                        onOptionSelected = { viewModel.onFilterDateRangeChange(it) }
                    )
                }

                item {
                    KpiGrid(uiState.kpis)
                }

                item {
                    Text(
                        text = "Performance Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    ChartCard("Equity Curve") {
                        LineChart(data = uiState.chartData.balanceGrowth.map { it.second })
                    }
                }

                item {
                    ChartCard("Profit & Loss Distribution") {
                        BarChart(data = uiState.chartData.profitLossHistory)
                    }
                }

                item {
                    ChartCard("Strategy Performance") {
                        HorizontalBarChart(data = uiState.chartData.strategyPerformance)
                    }
                }
                
                item {
                    ChartCard("Symbol Performance") {
                        HorizontalBarChart(data = uiState.chartData.symbolPerformance)
                    }
                }
                
                item {
                    ChartCard("Monthly Revenue") {
                        HorizontalBarChart(data = uiState.chartData.monthlyProfit)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            PullRefreshIndicator(uiState.isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun DateRangeFilters(
    selectedOption: DateRangeOption,
    onOptionSelected: (DateRangeOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DateRangeOption.values().forEach { option ->
            FilterChip(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
                label = { Text(option.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Composable
fun KpiGrid(kpis: DashboardKpis) {
    val profitColor = if (kpis.totalNetProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    val items = listOf(
        KpiItem("Account Balance", String.format("%.2f", kpis.currentBalance)),
        KpiItem("Net Profit", String.format("%.2f", kpis.totalNetProfit), profitColor),
        KpiItem("Win Rate", "${String.format("%.1f", kpis.winRate)}%"),
        KpiItem("Total Trades", kpis.totalTrades.toString()),
        KpiItem("Average R", String.format("%.2f", kpis.averageRMultiple)),
        KpiItem("Quality Score", "${kpis.averageQualityScore.toInt()}/100"),
        KpiItem("Profit Factor", String.format("%.2f", kpis.profitFactor)),
        KpiItem("Max Drawdown", String.format("%.2f", kpis.maxDrawdown), Color(0xFFFF9800)),
        KpiItem("Daily Loss %", "${String.format("%.1f", kpis.dailyLossUsed)}%"),
        KpiItem("Weekly Loss %", "${String.format("%.1f", kpis.weeklyLossUsed)}%"),
        KpiItem("Risk Warnings", kpis.riskWarningCount.toString(), if (kpis.riskWarningCount > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface),
        KpiItem("Current Streak", kpis.currentStreak.toString())
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { kpi ->
                    KpiCard(
                        label = kpi.label,
                        value = kpi.value,
                        modifier = Modifier.weight(1f),
                        valueColor = kpi.color ?: MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

data class KpiItem(val label: String, val value: String, val color: Color? = null)

@Composable
fun LineChart(data: List<Double>, modifier: Modifier = Modifier) {
    if (data.size < 2) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Text("Not enough data to plot equity curve", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) 
        }
        return
    }

    val max = data.maxOrNull() ?: 1.0
    val min = data.minOrNull() ?: 0.0
    val range = if (max - min == 0.0) 1.0 else max - min

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)
        
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - min) / range * height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(path, color = Color(0xFF6200EE), style = Stroke(width = 6f))
    }
}

@Composable
fun BarChart(data: List<Double>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Text("No trade history available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) 
        }
        return
    }

    val max = data.maxOf { abs(it) }.coerceAtLeast(1.0)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barWidth = width / data.size
        
        data.forEachIndexed { index, value ->
            val barHeight = (abs(value) / max * (height / 2)).toFloat()
            val x = index * barWidth
            val y = if (value >= 0) (height / 2) - barHeight else (height / 2)
            
            drawRect(
                color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                topLeft = Offset(x + 4f, y.toFloat()),
                size = androidx.compose.ui.geometry.Size((barWidth - 8f).coerceAtLeast(1f), barHeight.toFloat())
            )
        }
        drawLine(Color.Gray.copy(alpha = 0.5f), Offset(0f, height / 2), Offset(width, height / 2), strokeWidth = 2f)
    }
}

@Composable
fun HorizontalBarChart(data: Map<String, Double>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Text("No comparative data", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) 
        }
        return
    }

    val max = data.values.maxOf { abs(it) }.coerceAtLeast(1.0)
    val entries = data.toList()

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entries.forEach { (label, value) ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = String.format("%.2f", value),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                    val progress = (abs(value) / max).toFloat()
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxSize(),
                        color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}
