package com.example.compoundingjournal.presentation.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.compoundingjournal.utils.ColorUtils
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
        topBar = { AppTopBar(title = "Post-Trade Review") }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ReviewFiltersRow(
                selectedFilter = uiState.filter,
                onFilterSelected = { viewModel.onFilterChanged(it) }
            )

            if (uiState.trades.isEmpty()) {
                EmptyState(
                    message = "No trades found for this review criteria.",
                    icon = Icons.Default.MenuBook
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        ReviewFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                shape = MaterialTheme.shapes.medium
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewTradeCard(
    trade: TradeEntity,
    onClick: () -> Unit,
    onReview: () -> Unit
) {
    val gradeColor = ColorUtils.getGradeColor(trade.tradeQualityGrade)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("#${trade.tradeNumber} • ${trade.symbol}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${trade.date} • ${trade.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                if (trade.reviewCompleted) {
                    Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = CircleShape) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reviewed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = String.format("%.2f", trade.netProfitLoss),
                        color = if (trade.netProfitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Execution Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "Grade ${trade.tradeQualityGrade}", 
                        color = gradeColor, 
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (trade.mistakeTags.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    trade.mistakeTags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                        MistakeTagChip(tag)
                    }
                }
            }

            if (trade.lessonLearned.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Lesson Learned:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(trade.lessonLearned, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReview,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(if (trade.reviewCompleted) Icons.Default.Edit else Icons.Default.Assignment, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (trade.reviewCompleted) "Edit Review Analysis" else "Perform Post-Trade Review")
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
        title = { Text("Deep Review #${trade.tradeNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${trade.symbol} • ${trade.direction} • ${trade.status}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Performance Analysis & Feedback") },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    placeholder = { Text("Why did this trade result in this outcome? Was the strategy followed perfectly? What emotional triggers were present?") },
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(notes) }, shape = MaterialTheme.shapes.medium) {
                Text("Complete Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
