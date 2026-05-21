package com.example.compoundingjournal.presentation.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.ConfirmationDialog
import com.example.compoundingjournal.presentation.components.SectionCard
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
            AppTopBar(
                title = if (trade != null) "Trade Detail #${trade?.tradeNumber}" else "Loading...",
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
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SectionCard("Trade Overview") {
                    DetailItem("Symbol", t.symbol, isBold = true)
                    DetailItem("Direction", t.direction)
                    DetailItem("Timeframe", t.timeframe)
                    DetailItem("Status", t.status, valueColor = getStatusColor(t.status))
                    DetailItem("Date", t.date)
                    DetailItem("Time", t.time)
                }

                SectionCard("Pre-Trade Checklist") {
                    DetailItem("Score", "${t.checklistScore.toInt()}%", isBold = true)
                    DetailItem("Status", t.checklistStatus, valueColor = if (t.checklistScore >= 80.0) Color(0xFF4CAF50) else Color(0xFFF44336))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    ChecklistDetailItem("Trend confirmed", t.checklistTrendConfirmed)
                    ChecklistDetailItem("Key level confirmed", t.checklistKeyLevelConfirmed)
                    ChecklistDetailItem("Entry reason confirmed", t.checklistEntryReasonConfirmed)
                    ChecklistDetailItem("Stop loss planned", t.checklistStopLossPlanned)
                    ChecklistDetailItem("Take profit planned", t.checklistTakeProfitPlanned)
                    ChecklistDetailItem("Risk amount accepted", t.checklistRiskAccepted)
                    ChecklistDetailItem("No revenge trade", t.checklistNoRevengeTrade)
                    ChecklistDetailItem("No overlot", t.checklistNoOverlot)
                    ChecklistDetailItem("News checked", t.checklistNewsChecked)
                    ChecklistDetailItem("Emotion stable", t.checklistEmotionStable)
                }

                SectionCard("Rule Tracking") {
                    DetailItem("Rule Followed", t.ruleFollowed, valueColor = when(t.ruleFollowed.uppercase()) {
                        "YES" -> Color(0xFF4CAF50)
                        "NO" -> Color(0xFFF44336)
                        else -> Color(0xFFFF9800)
                    })
                    if (t.ruleBrokenNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Notes on Rule Violation:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(t.ruleBrokenNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                SectionCard("Execution Details") {
                    DetailItem("Entry Price", String.format("%.5f", t.entryPrice))
                    DetailItem("Stop Loss", String.format("%.5f", t.stopLoss))
                    DetailItem("Take Profit", String.format("%.5f", t.takeProfit))
                    DetailItem("Lot Size", t.lotSize.toString())
                }

                SectionCard("Profit & Loss") {
                    DetailItem("Starting Balance", String.format("%.2f", t.startingBalance))
                    DetailItem("Gross P/L", String.format("%.2f", t.grossProfitLoss))
                    DetailItem("Commission", String.format("%.2f", t.commission))
                    DetailItem("Swap", String.format("%.2f", t.swap))
                    DetailItem(
                        label = "Net P/L", 
                        value = String.format("%.2f", t.netProfitLoss), 
                        valueColor = if (t.netProfitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        isBold = true
                    )
                    DetailItem("Withdrawal", String.format("%.2f", t.withdrawalAmount))
                    DetailItem("Ending Balance", String.format("%.2f", t.endingBalance), isBold = true)
                    DetailItem("Growth", "${String.format("%.2f", t.growthPercent)}%")
                    DetailItem("R/R Ratio", String.format("%.2f", t.riskRewardRatio))
                    DetailItem("R Multiple", String.format("%.2f", t.rMultiple))
                }

                SectionCard("Psychology & Performance") {
                    DetailItem("Strategy", t.strategyName.ifBlank { "Not specified" })
                    DetailItem("Setup", t.setupType.ifBlank { "Not specified" })
                    DetailItem("Emotion Before", t.emotionBefore.ifBlank { "N/A" })
                    DetailItem("Emotion After", t.emotionAfter.ifBlank { "N/A" })
                    DetailItem("Mistakes", t.mistakeMade.ifBlank { "None" })
                    DetailItem("Lessons", t.lessonLearned.ifBlank { "N/A" })
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Journal Notes:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = t.notes.ifBlank { "No notes provided." }, 
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                SectionCard("Screenshots") {
                    Text("Before Entry", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    ScreenshotPreview(t.beforeScreenshotPath, onClick = { fullScreenImageUrl = t.beforeScreenshotPath })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("After Exit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    ScreenshotPreview(t.afterScreenshotPath, onClick = { fullScreenImageUrl = t.afterScreenshotPath })
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (fullScreenImageUrl != null) {
            FullScreenImageDialog(
                imageUrl = fullScreenImageUrl!!,
                onDismiss = { fullScreenImageUrl = null }
            )
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = "Delete Trade Record",
                text = "Are you sure you want to permanently delete this trade record? This action will trigger a recalculation of all subsequent trades.",
                onConfirm = {
                    scope.launch {
                        trade?.let { repository.deleteTrade(it) }
                        onNavigateBack()
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun ChecklistDetailItem(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (checked) Color(0xFF4CAF50) else Color(0xFFF44336)
        Icon(
            imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun DetailItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            color = valueColor, 
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "WIN" -> Color(0xFF4CAF50)
        "LOSS" -> Color(0xFFF44336)
        "BREAKEVEN" -> Color.Gray
        "RUNNING" -> Color(0xFF2196F3)
        "CANCELLED" -> Color.LightGray
        else -> Color.Black
    }
}

@Composable
fun ScreenshotPreview(path: String?, onClick: () -> Unit = {}) {
    if (path != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
                .clickable { onClick() },
            shape = MaterialTheme.shapes.medium
        ) {
            AsyncImage(
                model = path,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No image uploaded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
