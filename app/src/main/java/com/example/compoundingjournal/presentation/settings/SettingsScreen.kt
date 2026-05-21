package com.example.compoundingjournal.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToExportBackup: () -> Unit,
    onNavigateToStrategyPlaybook: () -> Unit,
    onNavigateToFilterPresets: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val settingsRepository = remember { SettingsRepositoryImpl(database.settingsDao()) }
    val tradeRepository = remember { TradeRepositoryImpl(database.tradeDao()) }
    
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(settingsRepository, tradeRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Local state for form fields
    var initialBalance by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var timezoneOffset by remember { mutableStateOf("UTC+0") }
    var dateFormat by remember { mutableStateOf("YYYY-MM-DD") }
    var timeFormat by remember { mutableStateOf("24-hour") }
    var autoTradeNumber by remember { mutableStateOf(true) }
    var defaultTimeframe by remember { mutableStateOf("H1") }
    var defaultSymbol by remember { mutableStateOf("") }
    var defaultCommission by remember { mutableStateOf("0.0") }
    var defaultSwap by remember { mutableStateOf("0.0") }
    var themeMode by remember { mutableStateOf("SYSTEM") }
    var accentColor by remember { mutableStateOf("BLUE") }
    
    // Phase 7 Risk settings state
    var maxRiskPerTrade by remember { mutableStateOf("") }
    var maxDailyLoss by remember { mutableStateOf("") }
    var maxWeeklyLoss by remember { mutableStateOf("") }
    var maxTradesPerDay by remember { mutableStateOf("") }
    var maxLossStreak by remember { mutableStateOf("") }
    var minRR by remember { mutableStateOf("") }
    var enableRiskWarning by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.settings) {
        uiState.settings?.let { s ->
            initialBalance = s.initialBalance.toString()
            currency = s.currency
            timezoneOffset = s.timezoneOffset
            dateFormat = s.dateFormat
            timeFormat = s.timeFormat
            autoTradeNumber = s.autoTradeNumber
            defaultTimeframe = s.defaultTimeframe
            defaultSymbol = s.defaultSymbol
            defaultCommission = s.defaultCommission.toString()
            defaultSwap = s.defaultSwap.toString()
            themeMode = s.themeMode
            accentColor = s.accentColor
            
            // Phase 7
            maxRiskPerTrade = s.maxRiskPerTradePercent.toString()
            maxDailyLoss = s.maxDailyLossPercent.toString()
            maxWeeklyLoss = s.maxWeeklyLossPercent.toString()
            maxTradesPerDay = s.maxTradesPerDay.toString()
            maxLossStreak = s.maxLosingStreakWarning.toString()
            minRR = s.minimumRiskRewardRatio.toString()
            enableRiskWarning = s.enableRiskWarning
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Preferences",
                actions = {
                    Button(
                        onClick = {
                            uiState.settings?.let { current ->
                                val updated = current.copy(
                                    initialBalance = initialBalance.toDoubleOrNull() ?: current.initialBalance,
                                    currency = currency,
                                    timezoneOffset = timezoneOffset,
                                    dateFormat = dateFormat,
                                    timeFormat = timeFormat,
                                    autoTradeNumber = autoTradeNumber,
                                    defaultTimeframe = defaultTimeframe,
                                    defaultSymbol = defaultSymbol,
                                    defaultCommission = defaultCommission.toDoubleOrNull() ?: current.defaultCommission,
                                    defaultSwap = defaultSwap.toDoubleOrNull() ?: current.defaultSwap,
                                    themeMode = themeMode,
                                    accentColor = accentColor,
                                    // Phase 7
                                    maxRiskPerTradePercent = maxRiskPerTrade.toDoubleOrNull() ?: current.maxRiskPerTradePercent,
                                    maxDailyLossPercent = maxDailyLoss.toDoubleOrNull() ?: current.maxDailyLossPercent,
                                    maxWeeklyLossPercent = maxWeeklyLoss.toDoubleOrNull() ?: current.maxWeeklyLossPercent,
                                    maxTradesPerDay = maxTradesPerDay.toIntOrNull() ?: current.maxTradesPerDay,
                                    maxLosingStreakWarning = maxLossStreak.toIntOrNull() ?: current.maxLosingStreakWarning,
                                    minimumRiskRewardRatio = minRR.toDoubleOrNull() ?: current.minimumRiskRewardRatio,
                                    enableRiskWarning = enableRiskWarning
                                )
                                viewModel.updateSettings(updated)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.settings == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SectionCard("Financial Account") {
                    MoneyInputField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = "Starting Capital"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppDropdownField(
                        label = "Default Currency",
                        options = listOf("USD", "MYR", "EUR", "GBP", "JPY", "AUD", "Custom"),
                        selectedOption = currency,
                        onOptionSelected = { currency = it }
                    )
                }

                SectionCard("Risk Management Rules") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Risk Warnings", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = enableRiskWarning, onCheckedChange = { enableRiskWarning = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberInputField(
                            value = maxRiskPerTrade,
                            onValueChange = { maxRiskPerTrade = it },
                            label = "Max Risk/Trade %",
                            modifier = Modifier.weight(1f)
                        )
                        NumberInputField(
                            value = minRR,
                            onValueChange = { minRR = it },
                            label = "Min R:R Ratio",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberInputField(
                            value = maxDailyLoss,
                            onValueChange = { maxDailyLoss = it },
                            label = "Max Daily Loss %",
                            modifier = Modifier.weight(1f)
                        )
                        NumberInputField(
                            value = maxWeeklyLoss,
                            onValueChange = { maxWeeklyLoss = it },
                            label = "Max Weekly Loss %",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberInputField(
                            value = maxTradesPerDay,
                            onValueChange = { maxTradesPerDay = it },
                            label = "Max Trades/Day",
                            modifier = Modifier.weight(1f),
                            isDecimal = false
                        )
                        NumberInputField(
                            value = maxLossStreak,
                            onValueChange = { maxLossStreak = it },
                            label = "Losing Streak Limit",
                            modifier = Modifier.weight(1f),
                            isDecimal = false
                        )
                    }
                }

                SectionCard("Regional & Format") {
                    OutlinedTextField(
                        value = timezoneOffset,
                        onValueChange = { timezoneOffset = it },
                        label = { Text("Display Timezone Offset") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppDropdownField(
                            label = "Date Format",
                            options = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"),
                            selectedOption = dateFormat,
                            onOptionSelected = { dateFormat = it },
                            modifier = Modifier.weight(1f)
                        )
                        AppDropdownField(
                            label = "Time Format",
                            options = listOf("12-hour", "24-hour"),
                            selectedOption = timeFormat,
                            onOptionSelected = { timeFormat = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                SectionCard("Journal Defaults") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-generate Trade IDs", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = autoTradeNumber, onCheckedChange = { autoTradeNumber = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AppDropdownField(
                        label = "Default Timeframe",
                        options = listOf("M1", "M5", "M15", "M30", "H1", "H4", "D1", "W1", "MN1"),
                        selectedOption = defaultTimeframe,
                        onOptionSelected = { defaultTimeframe = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = defaultSymbol,
                        onValueChange = { defaultSymbol = it },
                        label = { Text("Default Symbol") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        placeholder = { Text("e.g. BTCUSD") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MoneyInputField(
                            value = defaultCommission,
                            onValueChange = { defaultCommission = it },
                            label = "Def. Commission",
                            modifier = Modifier.weight(1f)
                        )
                        MoneyInputField(
                            value = defaultSwap,
                            onValueChange = { defaultSwap = it },
                            label = "Def. Swap",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                SectionCard("App Appearance") {
                    AppDropdownField(
                        label = "Theme Mode",
                        options = listOf("LIGHT", "DARK", "SYSTEM"),
                        selectedOption = themeMode,
                        onOptionSelected = { themeMode = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppDropdownField(
                        label = "Accent Color",
                        options = listOf("BLUE", "GREEN", "PURPLE", "ORANGE", "RED"),
                        selectedOption = accentColor,
                        onOptionSelected = { accentColor = it }
                    )
                }

                SectionCard("Data Tools") {
                    Button(
                        onClick = onNavigateToStrategyPlaybook,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Open Strategy Playbook")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToFilterPresets,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Manage Filter Presets")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToExportBackup, 
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Open Export & Backup Tools")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Purge All Trade Data")
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = "Erase All Data",
                text = "This will permanently delete every trade in your journal. This action cannot be undone.",
                confirmText = "Erase Everything",
                onConfirm = {
                    viewModel.deleteAllTrades()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}
