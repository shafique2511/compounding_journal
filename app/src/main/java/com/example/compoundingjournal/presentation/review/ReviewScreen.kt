package com.example.compoundingjournal.presentation.review

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.*
import com.example.compoundingjournal.presentation.journal.getGradeColor
import com.example.compoundingjournal.presentation.journal.getStatusColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(onTradeClick: (Long) -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    var tradeToReview by remember { mutableStateOf<TradeEntity?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = "Trade Review Mode") }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ReviewFiltersRow(
                selectedFilter = uiState.filter,
                onFilterSelected = { viewModel.onFilterChanged(it) }
            )

            if (uiState.trades.isEmpty()) {
                EmptyState(
                    message = "No trades match this review filter.",
                    icon = Icons.Default.FactCheck
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trades) { trade ->
                        ReviewTradeCard(
                            trade = trade,
                            onClick = { onTradeClick(trade.id) },
                            onReview = { tradeToReview = trade }
                        )
                    }
                }
            }
        }

        if (tradeToReview != null) {
            ReviewDialog(
                trade = tradeToReview!!,
                onDismiss = { tradeToReview = null },
                onSave = { notes ->
                    viewModel.completeReview(tradeToReview!!, notes)
                    tradeToReview = null
                }
            )
        }
    }
}

@Composable
fun ReviewFiltersRow(
    selectedFilter: ReviewFilter,
    onFilterSelected: (ReviewFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReviewFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewTradeCard(
    trade: TradeEntity,
    onClick: () -> Unit,
    onReview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("#${trade.tradeNumber} • ${trade.symbol}", fontWeight = FontWeight.Bold)
                    Text("${trade.date} ${trade.time}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                if (trade.reviewCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Reviewed", tint = Color(0xFF4CAF50))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Net P/L", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = String.format("%.2f", trade.netProfitLoss),
                        color = if (trade.netProfitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Grade", style = MaterialTheme.typography.labelSmall)
                    Text(trade.tradeQualityGrade, color = getGradeColor(trade.tradeQualityGrade), fontWeight = FontWeight.Bold)
                }
            }

            if (trade.mistakeTags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    trade.mistakeTags.split(",").forEach { tag ->
                        SuggestionChip(onClick = {}, label = { Text(tag, fontSize = 10.sp) })
                    }
                }
            }

            if (trade.lessonLearned.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lesson: ${trade.lessonLearned}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReview,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (trade.reviewCompleted) "Edit Review" else "Start Review")
            }
        }
    }
}

@Composable
fun ReviewDialog(
    trade: TradeEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(trade.reviewNotes) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Trade #${trade.tradeNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Analysis of ${trade.symbol} (${trade.status})", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Review Notes") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("What did you do well? What can be improved?") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(notes) }) {
                Text("Save Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
