package com.example.compoundingjournal.presentation.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.compoundingjournal.data.entity.TradeEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailScreen(
    tradeId: Long,
    onEditTrade: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val scope = rememberCoroutineScope()
    
    var trade by remember { mutableStateOf<TradeEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tradeId) {
        trade = repository.getTradeById(tradeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (trade != null) "Trade #${trade?.tradeNumber}" else "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditTrade(tradeId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        if (trade == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val t = trade!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailSection("Trade Info") {
                    DetailRow("Symbol", t.symbol)
                    DetailRow("Direction", t.direction)
                    DetailRow("Timeframe", t.timeframe)
                    DetailRow("Status", t.status)
                    DetailRow("Date", t.date)
                    DetailRow("Time", t.time)
                }

                DetailSection("Price Info") {
                    DetailRow("Entry Price", String.format("%.5f", t.entryPrice))
                    DetailRow("Stop Loss", String.format("%.5f", t.stopLoss))
                    DetailRow("Take Profit", String.format("%.5f", t.takeProfit))
                    DetailRow("Lot Size", t.lotSize.toString())
                }

                DetailSection("Money Info") {
                    DetailRow("Starting Balance", String.format("%.2f", t.startingBalance))
                    DetailRow("Gross P/L", String.format("%.2f", t.grossProfitLoss))
                    DetailRow("Commission", String.format("%.2f", t.commission))
                    DetailRow("Swap", String.format("%.2f", t.swap))
                    DetailRow("Net P/L", String.format("%.2f", t.netProfitLoss), color = if (t.netProfitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336))
                    DetailRow("Withdrawal", String.format("%.2f", t.withdrawalAmount))
                    DetailRow("Ending Balance", String.format("%.2f", t.endingBalance))
                    DetailRow("Growth %", "${String.format("%.2f", t.growthPercent)}%")
                    DetailRow("R/R Ratio", String.format("%.2f", t.riskRewardRatio))
                    DetailRow("R Multiple", String.format("%.2f", t.rMultiple))
                }

                DetailSection("Psychology & Notes") {
                    DetailRow("Strategy", t.strategyName)
                    DetailRow("Setup", t.setupType)
                    DetailRow("Emotion Before", t.emotionBefore)
                    DetailRow("Emotion After", t.emotionAfter)
                    DetailRow("Mistakes", t.mistakeMade)
                    DetailRow("Lessons", t.lessonLearned)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Notes:", style = MaterialTheme.typography.labelMedium)
                    Text(t.notes, style = MaterialTheme.typography.bodyMedium)
                }

                DetailSection("Screenshots") {
                    Text("Before Entry:", style = MaterialTheme.typography.labelMedium)
                    ScreenshotPreview(t.beforeScreenshotPath, onClick = { fullScreenImageUrl = t.beforeScreenshotPath })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("After Entry:", style = MaterialTheme.typography.labelMedium)
                    ScreenshotPreview(t.afterScreenshotPath, onClick = { fullScreenImageUrl = t.afterScreenshotPath })
                }
            }
        }

        if (fullScreenImageUrl != null) {
            FullScreenImageDialog(
                imageUrl = fullScreenImageUrl!!,
                onDismiss = { fullScreenImageUrl = null }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Trade") },
                text = { Text("Are you sure you want to delete this trade? Subsequent balances will be recalculated.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            trade?.let { repository.deleteTrade(it) }
                            onNavigateBack()
                        }
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ScreenshotPreview(path: String?, onClick: () -> Unit = {}) {
    if (path != null) {
        AsyncImage(
            model = path,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 4.dp)
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No screenshot", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
