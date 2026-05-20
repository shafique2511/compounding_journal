package com.example.compoundingjournal.presentation.addtrade

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.journal.ScreenshotPreview
import com.example.compoundingjournal.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text(if (tradeId == null) "Add Trade" else "Edit Trade") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Trade Info
            Text("Section 1: Trade Info", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.symbol,
                onValueChange = { viewModel.onEvent(TradeFormEvent.SymbolChanged(it)) },
                label = { Text("Symbol") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownField(
                    label = "Direction",
                    options = listOf("BUY", "SELL"),
                    selectedOption = uiState.direction,
                    onOptionSelected = { viewModel.onEvent(TradeFormEvent.DirectionChanged(it)) },
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    label = "Timeframe",
                    options = listOf("M1", "M5", "M15", "M30", "H1", "H4", "D1", "W1", "MN1"),
                    selectedOption = uiState.timeframe,
                    onOptionSelected = { viewModel.onEvent(TradeFormEvent.TimeframeChanged(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.DateChanged(it)) },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.TimeChanged(it)) },
                    label = { Text("Time") },
                    modifier = Modifier.weight(1f)
                )
            }

            DropdownField(
                label = "Status",
                options = listOf("WIN", "LOSS", "BREAKEVEN", "RUNNING", "CANCELLED"),
                selectedOption = uiState.status,
                onOptionSelected = { viewModel.onEvent(TradeFormEvent.StatusChanged(it)) }
            )

            // Section 2: Price Info
            Divider()
            Text("Section 2: Price Info", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.entryPrice,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.EntryPriceChanged(it)) },
                    label = { Text("Entry Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.stopLoss,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.StopLossChanged(it)) },
                    label = { Text("Stop Loss") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.takeProfit,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.TakeProfitChanged(it)) },
                    label = { Text("Take Profit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.lotSize,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.LotSizeChanged(it)) },
                    label = { Text("Lot Size") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 3: Money Info
            Divider()
            Text("Section 3: Money Info", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.startingBalance,
                onValueChange = { viewModel.onEvent(TradeFormEvent.StartingBalanceChanged(it)) },
                label = { Text("Starting Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.grossProfitLoss,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.GrossProfitLossChanged(it)) },
                    label = { Text("Gross P/L") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.commission,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.CommissionChanged(it)) },
                    label = { Text("Commission") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.swap,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.SwapChanged(it)) },
                    label = { Text("Swap") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.withdrawalAmount,
                    onValueChange = { viewModel.onEvent(TradeFormEvent.WithdrawalAmountChanged(it)) },
                    label = { Text("Withdrawal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Net P/L: ${uiState.netProfitLoss}", style = MaterialTheme.typography.bodyLarge)
                    Text("Ending Balance: ${uiState.endingBalance}", style = MaterialTheme.typography.bodyLarge)
                    Text("Growth %: ${uiState.growthPercent}%", style = MaterialTheme.typography.bodyLarge)
                    Text("R/R Ratio: ${uiState.riskRewardRatio}", style = MaterialTheme.typography.bodyLarge)
                    Text("R Multiple: ${uiState.rMultiple}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Section 4: Psychology
            Divider()
            Text("Section 4: Psychology", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.strategyName,
                onValueChange = { viewModel.onEvent(TradeFormEvent.StrategyNameChanged(it)) },
                label = { Text("Strategy Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.setupType,
                onValueChange = { viewModel.onEvent(TradeFormEvent.SetupTypeChanged(it)) },
                label = { Text("Setup Type") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.emotionBefore,
                onValueChange = { viewModel.onEvent(TradeFormEvent.EmotionBeforeChanged(it)) },
                label = { Text("Emotion Before") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.emotionAfter,
                onValueChange = { viewModel.onEvent(TradeFormEvent.EmotionAfterChanged(it)) },
                label = { Text("Emotion After") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.mistakeMade,
                onValueChange = { viewModel.onEvent(TradeFormEvent.MistakeMadeChanged(it)) },
                label = { Text("Mistake Made") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.lessonLearned,
                onValueChange = { viewModel.onEvent(TradeFormEvent.LessonLearnedChanged(it)) },
                label = { Text("Lesson Learned") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onEvent(TradeFormEvent.NotesChanged(it)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Section 5: Screenshots
            Divider()
            Text("Section 5: Screenshots", style = MaterialTheme.typography.titleMedium)
            
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

            ScreenshotSection(
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

            ScreenshotSection(
                label = "After Entry",
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

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { 
                    val saveEvent = TradeFormEvent.SaveTrade
                    saveEvent.onSuccess = onNavigateBack
                    viewModel.onEvent(saveEvent) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                else Text("Save Trade")
            }
            
            OutlinedButton(
                onClick = { viewModel.onEvent(TradeFormEvent.Reset) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Form")
            }
            
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }

            uiState.errorMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ScreenshotSection(
    label: String,
    path: String?,
    onPickGallery: () -> Unit,
    onPickCamera: () -> Unit,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (path != null) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                ScreenshotPreview(path)
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPickGallery, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Gallery", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onPickCamera, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Camera", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
