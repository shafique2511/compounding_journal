package com.example.compoundingjournal.presentation.journal

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
import com.example.compoundingjournal.presentation.components.*
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
            AppTopBar(
                title = "Trade Journal",
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTrade,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trade")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) }
            )

            if (uiState.trades.isEmpty()) {
                EmptyState(
                    message = if (uiState.searchQuery.isEmpty()) "Your journal is empty.\nTap + to add your first trade." else "No trades match your search.",
                    icon = Icons.Default.Inventory2
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trades, key = { it.id }) { trade ->
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
            ConfirmationDialog(
                title = "Delete Trade",
                text = "Are you sure you want to delete trade #${showDeleteDialog?.tradeNumber}? This will automatically recalculate subsequent trade balances.",
                onConfirm = {
                    showDeleteDialog?.let { viewModel.deleteTrade(it) }
                    showDeleteDialog = null
                },
                onDismiss = { showDeleteDialog = null }
            )
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                FilterOptionsContent(
                    uiState = uiState,
                    onSortByChange = { viewModel.onSortByChange(it) },
                    onFilterStatusChange = { viewModel.onFilterStatusChange(it) },
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
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search symbol, strategy, notes...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOptionsContent(
    uiState: JournalUiState,
    onSortByChange: (SortOption) -> Unit,
    onFilterStatusChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Text("Sort By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortOption.values().forEach { option ->
                FilterChip(
                    selected = uiState.sortBy == option,
                    onClick = { onSortByChange(option) },
                    label = { Text(option.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Filter by Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        val statuses = listOf("WIN", "LOSS", "BREAKEVEN", "RUNNING", "CANCELLED")
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                FilterChip(
                    selected = uiState.filterStatus == status,
                    onClick = { onFilterStatusChange(if (uiState.filterStatus == status) null else status) },
                    label = { Text(status) },
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onClearFilters,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Clear All Filters")
        }
        Spacer(modifier = Modifier.height(48.dp))
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
