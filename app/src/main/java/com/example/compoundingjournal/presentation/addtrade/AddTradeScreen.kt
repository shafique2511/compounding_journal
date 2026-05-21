package com.example.compoundingjournal.presentation.addtrade

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.*
import com.example.compoundingjournal.presentation.journal.ScreenshotPreview
import com.example.compoundingjournal.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTradeScreen(
    onNavigateBack: () -> Unit,
    tradeId: Long? = null
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val tradeRepository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val settingsRepository = remember { SettingsRepositoryImpl(database.settingsDao()) }
    
    val viewModel: TradeViewModel = viewModel(
        factory = TradeViewModelFactory(tradeRepository, settingsRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(tradeId) {
        if (tradeId != null) {
            viewModel.initForEdit(tradeId)
        } else {
            viewModel.initForAdd()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (tradeId == null) "New Trade" else "Edit Trade",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(TradeFormEvent.Reset) }) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Section 1: Trade Info
            SectionCard("Trade Information") {
                OutlinedTextField(
                    value = uiState.symbol,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.SymbolChanged(it)) },
                    label = { Text("Symbol (e.g. EURUSD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppDropdownField(
                        label = "Direction",
                        options = listOf("BUY", "SELL"),
                        selectedOption = uiState.direction,
                        onOptionSelected = { viewModel.onEvent(TradeFormEvent.DirectionChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    AppDropdownField(
                        label = "Timeframe",
                        options = listOf("M1", "M5", "M15", "M30", "H1", "H4", "D1", "W1", "MN1"),
                        selectedOption = uiState.timeframe,
                        onOptionSelected = { viewModel.onEvent(TradeFormEvent.TimeframeChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.date,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.DateChanged(it)) },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = uiState.time,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.TimeChanged(it)) },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AppDropdownField(
                    label = "Status",
                    options = listOf("WIN", "LOSS", "BREAKEVEN", "RUNNING", "CANCELLED"),
                    selectedOption = uiState.status,
                    onOptionSelected = { viewModel.onEvent(TradeFormEvent.StatusChanged(it)) }
                )
            }

            // Phase 3: Pre-Trade Checklist
            SectionCard("Pre-Trade Checklist") {
                ChecklistItem("Trend confirmed", uiState.checklistTrendConfirmed) { viewModel.onEvent(TradeFormEvent.ChecklistTrendChanged(it)) }
                ChecklistItem("Key level confirmed", uiState.checklistKeyLevelConfirmed) { viewModel.onEvent(TradeFormEvent.ChecklistKeyLevelChanged(it)) }
                ChecklistItem("Entry reason confirmed", uiState.checklistEntryReasonConfirmed) { viewModel.onEvent(TradeFormEvent.ChecklistEntryReasonChanged(it)) }
                ChecklistItem("Stop loss planned", uiState.checklistStopLossPlanned) { viewModel.onEvent(TradeFormEvent.ChecklistStopLossChanged(it)) }
                ChecklistItem("Take profit planned", uiState.checklistTakeProfitPlanned) { viewModel.onEvent(TradeFormEvent.ChecklistTakeProfitChanged(it)) }
                ChecklistItem("Risk amount accepted", uiState.checklistRiskAccepted) { viewModel.onEvent(TradeFormEvent.ChecklistRiskAcceptedChanged(it)) }
                ChecklistItem("No revenge trade", uiState.checklistNoRevengeTrade) { viewModel.onEvent(TradeFormEvent.ChecklistNoRevengeTradeChanged(it)) }
                ChecklistItem("No overlot", uiState.checklistNoOverlot) { viewModel.onEvent(TradeFormEvent.ChecklistNoOverlotChanged(it)) }
                ChecklistItem("News checked", uiState.checklistNewsChecked) { viewModel.onEvent(TradeFormEvent.ChecklistNewsCheckedClicked(it)) }
                ChecklistItem("Emotion stable", uiState.checklistEmotionStable) { viewModel.onEvent(TradeFormEvent.ChecklistEmotionStableChanged(it)) }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                val statusColor = if (uiState.checklistScore >= 80.0) Color(0xFF4CAF50) else Color(0xFFF44336)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Checklist Score: ${uiState.checklistScore.toInt()}%", fontWeight = FontWeight.Bold)
                    Text(uiState.checklistStatus, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            // Section 2: Price Info
            SectionCard("Execution Details") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoneyInputField(
                        value = uiState.entryPrice,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.EntryPriceChanged(it)) },
                        label = "Entry Price",
                        modifier = Modifier.weight(1f)
                    )
                    MoneyInputField(
                        value = uiState.stopLoss,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.StopLossChanged(it)) },
                        label = "Stop Loss",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoneyInputField(
                        value = uiState.takeProfit,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.TakeProfitChanged(it)) },
                        label = "Take Profit",
                        modifier = Modifier.weight(1f)
                    )
                    MoneyInputField(
                        value = uiState.lotSize,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.LotSizeChanged(it)) },
                        label = "Lot Size",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 3: Money Info
            SectionCard("Financial Metrics") {
                MoneyInputField(
                    value = uiState.startingBalance,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.StartingBalanceChanged(it)) },
                    label = "Starting Balance"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoneyInputField(
                        value = uiState.grossProfitLoss,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.GrossProfitLossChanged(it)) },
                        label = "Gross P/L",
                        modifier = Modifier.weight(1f)
                    )
                    MoneyInputField(
                        value = uiState.commission,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.CommissionChanged(it)) },
                        label = "Commission",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoneyInputField(
                        value = uiState.swap,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.SwapChanged(it)) },
                        label = "Swap",
                        modifier = Modifier.weight(1f)
                    )
                    MoneyInputField(
                        value = uiState.withdrawalAmount,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.WithdrawalAmountChanged(it)) },
                        label = "Withdrawal",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatLine("Net P/L", uiState.netProfitLoss, isBold = true)
                        StatLine("Ending Balance", uiState.endingBalance)
                        StatLine("Growth", "${uiState.growthPercent}%")
                        StatLine("R/R Ratio", uiState.riskRewardRatio)
                        StatLine("R Multiple", uiState.rMultiple)
                    }
                }
            }

            // Phase 3: Rule tracking
            SectionCard("Rule Tracking") {
                AppDropdownField(
                    label = "Rule Followed",
                    options = listOf("YES", "NO", "PARTIALLY"),
                    selectedOption = uiState.ruleFollowed,
                    onOptionSelected = { viewModel.onEvent(TradeFormEvent.RuleFollowedChanged(it)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.ruleBrokenNotes,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.RuleBrokenNotesChanged(it)) },
                    label = { Text("Rule Broken Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Phase 4: Mistake Tags
            SectionCard("Mistake Analysis") {
                val tags = listOf(
                    "FOMO", "Revenge Trade", "Overlot", "Early Entry", "Late Entry",
                    "Early Exit", "Late Exit", "Moved Stop Loss", "No Stop Loss",
                    "Ignored Trend", "Ignored News", "Bad Risk Reward", "Chased Price",
                    "Emotional Entry", "Poor Setup", "Other"
                )
                val selectedTags = uiState.mistakeTags.split(",").filter { it.isNotBlank() }.toSet()
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag),
                            onClick = {
                                val newTags = if (selectedTags.contains(tag)) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                                viewModel.onEvent(TradeFormEvent.MistakeTagsChanged(newTags.joinToString(",")))
                            },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            // Section 4: Psychology
            SectionCard("Trade Psychology & Notes") {
                OutlinedTextField(
                    value = uiState.strategyName,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.StrategyNameChanged(it)) },
                    label = { Text("Strategy Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.setupType,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.SetupTypeChanged(it)) },
                    label = { Text("Setup Type (e.g. Retest)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.emotionBefore,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.EmotionBeforeChanged(it)) },
                        label = { Text("Before Entry") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = uiState.emotionAfter,
                        onValueChange = { viewModel.onEvent(TradeFormEvent.EmotionAfterChanged(it)) },
                        label = { Text("After Exit") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.mistakeMade,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.MistakeMadeChanged(it)) },
                    label = { Text("Mistakes Made") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.lessonLearned,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.LessonLearnedChanged(it)) },
                    label = { Text("Lesson Learned") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.NotesChanged(it)) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Section 5: Screenshots
            SectionCard("Screenshots") {
                var tempUri by remember { mutableStateOf<android.net.Uri?>(null) }
                var pickingForBefore by remember { mutableStateOf(true) }

                val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    uri?.let {
                        if (pickingForBefore) viewModel.onEvent(TradeFormEvent.BeforeScreenshotPicked(it.toString()))
                        else viewModel.onEvent(TradeFormEvent.AfterScreenshotPicked(it.toString()))
                    }
                }

                val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                    if (success) {
                        tempUri?.let {
                            if (pickingForBefore) viewModel.onEvent(TradeFormEvent.BeforeScreenshotPicked(it.toString()))
                            else viewModel.onEvent(TradeFormEvent.AfterScreenshotPicked(it.toString()))
                        }
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        val uri = ImageUtils.createImageUri(context)
                        tempUri = uri
                        uri?.let { cameraLauncher.launch(it) }
                    }
                }

                ScreenshotPickerSection(
                    label = "Before Entry",
                    path = uiState.beforeScreenshotPath,
                    onPickGallery = {
                        pickingForBefore = true
                        galleryLauncher.launch("image/*")
                    },
                    onPickCamera = {
                        pickingForBefore = true
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    onRemove = { viewModel.onEvent(TradeFormEvent.BeforeScreenshotPicked(null)) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                ScreenshotPickerSection(
                    label = "After Exit",
                    path = uiState.afterScreenshotPath,
                    onPickGallery = {
                        pickingForBefore = false
                        galleryLauncher.launch("image/*")
                    },
                    onPickCamera = {
                        pickingForBefore = false
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    onRemove = { viewModel.onEvent(TradeFormEvent.AfterScreenshotPicked(null)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { 
                    viewModel.onEvent(TradeFormEvent.SaveTrade(onNavigateBack))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    var btnText = if (tradeId == null) "Save Trade Record" else "Update Trade Record"
                    Text(btnText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Cancel")
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.showChecklistWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TradeFormEvent.DismissWarning) },
            title = { Text("Weak Trade Plan") },
            text = { Text("Checklist score is below 80%. This trade plan may be weak. Save anyway?") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.onEvent(TradeFormEvent.ConfirmSaveWeakPlan(onNavigateBack))
                }) {
                    Text("Save Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TradeFormEvent.DismissWarning) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChecklistItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StatLine(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
fun ScreenshotPickerSection(
    label: String,
    path: String?,
    onPickGallery: () -> Unit,
    onPickCamera: () -> Unit,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        if (path != null) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                ScreenshotPreview(path)
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPickGallery, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }
                OutlinedButton(onClick = onPickCamera, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }
            }
        }
    }
}
