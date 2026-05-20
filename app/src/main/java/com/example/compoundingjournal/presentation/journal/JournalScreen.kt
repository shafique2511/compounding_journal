package com.example.compoundingjournal.presentation.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.TradeRepository
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onAddTrade: () -> Unit,
    onTradeClick: (Long) -> Unit,
    onEditTrade: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<TradeEntity?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrade) {
                Icon(Icons.Default.Add, contentDescription = "Add Trade")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search symbol, strategy, notes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            if (uiState.trades.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No trades found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.trades) { trade ->
                        TradeCard(
                            trade = trade,
                            onClick = { onTradeClick(trade.id) },
                            onEdit = { onEditTrade(trade.id) },
                            onDelete = { showDeleteDialog = trade }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Trade") },
                text = { Text("Are you sure you want to delete trade #${showDeleteDialog?.tradeNumber}? This will recalculate subsequent balances.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog?.let { viewModel.deleteTrade(it) }
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                FilterOptionsContent(
                    uiState = uiState,
                    onSortByChange = { viewModel.onSortByChange(it) },
                    onFilterSymbolChange = { viewModel.onFilterSymbolChange(it) },
                    onFilterTimeframeChange = { viewModel.onFilterTimeframeChange(it) },
                    onFilterStatusChange = { viewModel.onFilterStatusChange(it) },
                    onFilterStrategyChange = { viewModel.onFilterStrategyChange(it) },
                    onClearFilters = {
                        viewModel.onFilterSymbolChange(null)
                        viewModel.onFilterTimeframeChange(null)
                        viewModel.onFilterStatusChange(null)
                        viewModel.onFilterStrategyChange(null)
                    }
                )
            }
        }
    }
}

@Composable
fun TradeCard(
    trade: TradeEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (trade.status.uppercase()) {
        "WIN" -> Color(0xFF4CAF50)
        "LOSS" -> Color(0xFFF44336)
        "BREAKEVEN" -> Color.Gray
        "RUNNING" -> Color(0xFF2196F3)
        "CANCELLED" -> Color.LightGray
        else -> Color.Black
    }

    val pnlColor = if (trade.netProfitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("#${trade.tradeNumber} • ${trade.date} ${trade.time}", style = MaterialTheme.typography.bodySmall)
                    Text("${trade.symbol} • ${trade.direction} • ${trade.timeframe}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = trade.status,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Net P/L", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = String.format("%.2f", trade.netProfitLoss),
                        color = pnlColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Ending Balance", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = String.format("%.2f", trade.endingBalance),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (trade.withdrawalAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Withdrawal: ${String.format("%.2f", trade.withdrawalAmount)}",
                    color = Color(0xFFFF9800),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOptionsContent(
    uiState: JournalUiState,
    onSortByChange: (SortOption) -> Unit,
    onFilterSymbolChange: (String?) -> Unit,
    onFilterTimeframeChange: (String?) -> Unit,
    onFilterStatusChange: (String?) -> Unit,
    onFilterStrategyChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Sort By", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortOption.values().forEach { option ->
                FilterChip(
                    selected = uiState.sortBy == option,
                    onClick = { onSortByChange(option) },
                    label = { Text(option.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status Filter", style = MaterialTheme.typography.titleSmall)
        val statuses = listOf("WIN", "LOSS", "BREAKEVEN", "RUNNING", "CANCELLED")
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                FilterChip(
                    selected = uiState.filterStatus == status,
                    onClick = { onFilterStatusChange(if (uiState.filterStatus == status) null else status) },
                    label = { Text(status) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClearFilters, modifier = Modifier.fillMaxWidth()) {
            Text("Clear All Filters")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

class JournalViewModelFactory(
    private val repository: TradeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
