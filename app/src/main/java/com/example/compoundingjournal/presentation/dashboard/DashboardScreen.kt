package com.example.compoundingjournal.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import java.util.Locale

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
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Filters
                item {
                    DateRangeFilters(
                        selectedOption = uiState.filters.dateRange,
                        onOptionSelected = { viewModel.onFilterDateRangeChange(it) }
                    )
                }

                // KPI Grid
                item {
                    KpiGrid(uiState.kpis)
                }

                item {
                    Text("Performance Charts", style = MaterialTheme.typography.titleLarge)
                }

                item {
                    ChartCard("Balance Growth") {
                        LineChart(data = uiState.chartData.balanceGrowth.map { it.second })
                    }
                }

                item {
                    ChartCard("Profit/Loss History") {
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
                    ChartCard("Monthly Profit") {
                        HorizontalBarChart(data = uiState.chartData.monthlyProfit)
                    }
                }
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
                label = { Text(option.name.replace("_", " ").lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) }
            )
        }
    }
}

@Composable
fun KpiGrid(kpis: DashboardKpis) {
    val items = listOf(
        "Balance" to String.format("%.2f", kpis.currentBalance),
        "Net Profit" to String.format("%.2f", kpis.totalNetProfit),
        "Win Rate" to "${String.format("%.1f", kpis.winRate)}%",
        "Trades" to kpis.totalTrades.toString(),
        "Avg R" to String.format("%.2f", kpis.averageRMultiple),
        "Profit Factor" to String.format("%.2f", kpis.profitFactor),
        "Max DD" to String.format("%.2f", kpis.maxDrawdown),
        "Streak" to kpis.currentStreak.toString()
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (label, value) ->
                    KpiCard(label, value, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun KpiCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun ChartCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                content()
            }
        }
    }
}

@Composable
fun LineChart(data: List<Double>, modifier: Modifier = Modifier) {
    if (data.size < 2) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Not enough data") }
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
        
        drawPath(path, color = Color(0xFF2196F3), style = Stroke(width = 4f))
    }
}

@Composable
fun BarChart(data: List<Double>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data") }
        return
    }

    val max = data.maxOf { Math.abs(it) }.coerceAtLeast(1.0)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barWidth = width / data.size
        
        data.forEachIndexed { index, value ->
            val barHeight = (Math.abs(value) / max * (height / 2)).toFloat()
            val x = index * barWidth
            val y = if (value >= 0) (height / 2) - barHeight else (height / 2)
            
            drawRect(
                color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                topLeft = Offset(x + 2f, y.toFloat()),
                size = androidx.compose.ui.geometry.Size(barWidth - 4f, barHeight.toFloat())
            )
        }
        drawLine(Color.Gray, Offset(0f, height / 2), Offset(width, height / 2))
    }
}

@Composable
fun HorizontalBarChart(data: Map<String, Double>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data") }
        return
    }

    val max = data.values.maxOf { Math.abs(it) }.coerceAtLeast(1.0)
    val entries = data.toList()

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.forEach { (label, value) ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 10.sp)
                    Text(String.format("%.2f", value), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                    val progress = (Math.abs(value) / max).toFloat()
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxSize(),
                        color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        trackColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}
