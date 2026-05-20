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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.entity.SettingsEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.addtrade.DropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToExportBackup: () -> Unit) {
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

    // Local state for form fields to allow editing before saving
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
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    IconButton(onClick = {
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
                                accentColor = accentColor
                            )
                            viewModel.updateSettings(updated)
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsSection("Account Settings") {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Initial Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownField(
                        label = "Account Currency",
                        options = listOf("USD", "MYR", "EUR", "GBP", "JPY", "AUD", "Custom"),
                        selectedOption = currency,
                        onOptionSelected = { currency = it }
                    )
                }

                SettingsSection("Time Settings") {
                    OutlinedTextField(
                        value = timezoneOffset,
                        onValueChange = { timezoneOffset = it },
                        label = { Text("Timezone Offset (Display only)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownField(
                        label = "Date Format",
                        options = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"),
                        selectedOption = dateFormat,
                        onOptionSelected = { dateFormat = it }
                    )
                    DropdownField(
                        label = "Time Format",
                        options = listOf("12-hour", "24-hour"),
                        selectedOption = timeFormat,
                        onOptionSelected = { timeFormat = it }
                    )
                }

                SettingsSection("Journal Settings") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto Trade Number")
                        Switch(checked = autoTradeNumber, onCheckedChange = { autoTradeNumber = it })
                    }
                    DropdownField(
                        label = "Default Timeframe",
                        options = listOf("M1", "M5", "M15", "M30", "H1", "H4", "D1", "W1", "MN1"),
                        selectedOption = defaultTimeframe,
                        onOptionSelected = { defaultTimeframe = it }
                    )
                    OutlinedTextField(
                        value = defaultSymbol,
                        onValueChange = { defaultSymbol = it },
                        label = { Text("Default Symbol") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = defaultCommission,
                            onValueChange = { defaultCommission = it },
                            label = { Text("Def. Commission") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = defaultSwap,
                            onValueChange = { defaultSwap = it },
                            label = { Text("Def. Swap") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                SettingsSection("Appearance") {
                    DropdownField(
                        label = "Theme Mode",
                        options = listOf("LIGHT", "DARK", "SYSTEM"),
                        selectedOption = themeMode,
                        onOptionSelected = { themeMode = it }
                    )
                    DropdownField(
                        label = "Accent Color",
                        options = listOf("BLUE", "GREEN", "PURPLE", "ORANGE", "RED"),
                        selectedOption = accentColor,
                        onOptionSelected = { accentColor = it }
                    )
                }

                SettingsSection("Data Management") {
                    Button(onClick = onNavigateToExportBackup, modifier = Modifier.fillMaxWidth()) {
                        Text("Export & Backup")
                    }
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete All Trades")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Trades") },
                text = { Text("Are you absolutely sure? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteAllTrades()
                        showDeleteDialog = false
                    }) {
                        Text("Delete All", color = MaterialTheme.colorScheme.error)
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
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}
