package com.example.compoundingjournal.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.EmptyState
import com.example.compoundingjournal.presentation.components.SectionCard
import com.example.compoundingjournal.presentation.journal.getStatusColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onTradeClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Trade Calendar") }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CalendarView(
                uiState = uiState,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onDateSelected = { viewModel.onDateSelected(it) }
            )
            
            HorizontalDivider()
            
            DayDetailSection(
                uiState = uiState,
                onTradeClick = onTradeClick
            )
        }
    }
}

@Composable
fun CalendarView(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    val calendar = uiState.currentMonth
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            Text(
                text = monthFormat.format(calendar.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Days of Week
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar Grid
        val totalCells = firstDayOfMonth + daysInMonth
        val rows = (totalCells + 6) / 7
        
        for (i in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                for (j in 0 until 7) {
                    val dayIndex = i * 7 + j
                    val dayNumber = dayIndex - firstDayOfMonth + 1
                    
                    if (dayNumber in 1..daysInMonth) {
                        val dateString = String.format("%04d-%02d-%02d", 
                            calendar.get(Calendar.YEAR), 
                            calendar.get(Calendar.MONTH) + 1, 
                            dayNumber
                        )
                        val summary = uiState.dailySummaries[dateString]
                        val isSelected = uiState.selectedDate == dateString
                        
                        CalendarDayCell(
                            day = dayNumber.toString(),
                            summary = summary,
                            isSelected = isSelected,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDateSelected(dateString) }
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: String,
    summary: DailySummary?,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .padding(2.dp)
            .background(bgColor, MaterialTheme.shapes.small)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (summary != null && summary.totalTrades > 0) {
                val pnlColor = when {
                    summary.netProfit > 0 -> Color(0xFF4CAF50)
                    summary.netProfit < 0 -> Color(0xFFF44336)
                    else -> Color.Gray
                }
                
                Text(
                    text = if (summary.netProfit >= 0) "+${String.format("%.0f", summary.netProfit)}" else String.format("%.0f", summary.netProfit),
                    color = pnlColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(pnlColor, CircleShape)
                )
            }
        }
    }
}

@Composable
fun DayDetailSection(
    uiState: CalendarUiState,
    onTradeClick: (Long) -> Unit
) {
    if (uiState.selectedDate == null) {
        EmptyState(message = "Select a day to view details")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Details for ${uiState.selectedDate}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.selectedDaySummary != null) {
            item {
                SectionCard("Daily Performance") {
                    val s = uiState.selectedDaySummary
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            DetailLabelValue("Net P/L", String.format("%.2f", s.netProfit), if (s.netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
                            DetailLabelValue("Withdrawals", String.format("%.2f", s.withdrawals), Color(0xFFFF9800))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            DetailLabelValue("Total Trades", s.totalTrades.toString())
                            DetailLabelValue("Wins/Losses", "${s.wins} / ${s.losses}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DetailLabelValue("Best Trade", String.format("%.2f", s.bestTrade), Color(0xFF4CAF50))
                        DetailLabelValue("Worst Trade", String.format("%.2f", s.worstTrade), Color(0xFFF44336))
                    }
                }
            }
        }

        if (uiState.selectedDayTrades.isEmpty()) {
            item {
                Text(
                    "No trades recorded for this day.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            item {
                Text("Trades List", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            items(uiState.selectedDayTrades) { trade ->
                DailyTradeCard(trade = trade, onClick = { onTradeClick(trade.id) })
            }
        }
    }
}

@Composable
fun DetailLabelValue(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun DailyTradeCard(trade: TradeEntity, onClick: () -> Unit) {
    val pnlColor = if (trade.netProfitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("#${trade.tradeNumber} • ${trade.symbol}", fontWeight = FontWeight.Bold)
                Text("${trade.direction} • ${trade.timeframe}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f", trade.netProfitLoss),
                    color = pnlColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(trade.status, style = MaterialTheme.typography.labelSmall, color = getStatusColor(trade.status))
            }
        }
    }
}
