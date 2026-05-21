package com.example.compoundingjournal.presentation.strategy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.entity.StrategyEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.StrategyRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.ConfirmationDialog
import com.example.compoundingjournal.presentation.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyPlaybookScreen(
    onAddStrategy: () -> Unit,
    onEditStrategy: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { StrategyRepositoryImpl(database.strategyDao()) }
    val viewModel: StrategyViewModel = viewModel(
        factory = StrategyViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<StrategyEntity?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Strategy Playbook",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilter() }) {
                        Icon(
                            imageVector = if (uiState.showOnlyActive) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                            contentDescription = "Toggle Filter"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStrategy) {
                Icon(Icons.Default.Add, contentDescription = "Add Strategy")
            }
        }
    ) { padding ->
        if (uiState.strategies.isEmpty()) {
            EmptyState(
                message = "No strategies found.\nStart by adding your trading rules.",
                icon = Icons.Default.MenuBook,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.strategies) { strategy ->
                    StrategyCard(
                        strategy = strategy,
                        onClick = { onEditStrategy(strategy.id) },
                        onDelete = { showDeleteDialog = strategy }
                    )
                }
            }
        }

        if (showDeleteDialog != null) {
            ConfirmationDialog(
                title = "Delete Strategy",
                text = "Are you sure you want to delete '${showDeleteDialog?.strategyName}'? This won't affect existing trades but will remove the playbook entry.",
                onConfirm = {
                    showDeleteDialog?.let { viewModel.deleteStrategy(it) }
                    showDeleteDialog = null
                },
                onDismiss = { showDeleteDialog = null }
            )
        }
    }
}

@Composable
fun StrategyCard(
    strategy: StrategyEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strategy.strategyName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${strategy.marketType} • ${strategy.timeframe}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = strategy.isActive,
                    onCheckedChange = null, // Display only in card
                    enabled = false
                )
            }
            
            if (strategy.entryRules.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Entry: ${strategy.entryRules.take(100)}${if (strategy.entryRules.length > 100) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
