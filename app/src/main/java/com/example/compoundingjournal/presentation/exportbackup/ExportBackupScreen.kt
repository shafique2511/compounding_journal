package com.example.compoundingjournal.presentation.exportbackup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.SettingsRepositoryImpl
import com.example.compoundingjournal.data.repository.StrategyRepositoryImpl
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.ConfirmationDialog
import com.example.compoundingjournal.presentation.components.SectionCard
import com.example.compoundingjournal.utils.BackupUtils
import com.example.compoundingjournal.utils.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBackupScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val tradeRepository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val strategyRepository = remember { StrategyRepositoryImpl(database.strategyDao()) }
    val settingsRepository = remember { SettingsRepositoryImpl(database.settingsDao()) }
    
    val viewModel: ExportBackupViewModel = viewModel(
        factory = ExportBackupViewModelFactory(tradeRepository, strategyRepository, settingsRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var csvContent by remember { mutableStateOf("") }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            val success = ExportUtils.writeTextToUri(context, it, csvContent)
            viewModel.showMessage(if (success) "Report exported successfully" else "Export failed")
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let {
            val success = BackupUtils.backupDatabase(context, it)
            viewModel.showMessage(if (success) "Database backup created" else "Backup failed")
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            restoreUri = it
            showRestoreConfirm = true
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
                title = "Data Management",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionCard("Standard Exports") {
                ExportButton(
                    label = "Full Trade History (CSV)",
                    onClick = {
                        viewModel.prepareAllTradesExport { content ->
                            csvContent = content
                            csvLauncher.launch("full_trades_${System.currentTimeMillis()}.csv")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ExportButton(
                    label = "Performance Summary (CSV)",
                    onClick = {
                        viewModel.prepareSummaryExport { content ->
                            csvContent = content
                            csvLauncher.launch("summary_${System.currentTimeMillis()}.csv")
                        }
                    }
                )
            }

            SectionCard("Advanced Reports") {
                ExportButton(
                    label = "Trade Review Report (CSV)",
                    onClick = {
                        viewModel.prepareReviewReportExport { content ->
                            csvContent = content
                            csvLauncher.launch("review_report_${System.currentTimeMillis()}.csv")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                ExportButton(
                    label = "Mistake Impact Analysis (CSV)",
                    onClick = {
                        viewModel.prepareMistakeAnalysisExport { content ->
                            csvContent = content
                            csvLauncher.launch("mistake_analysis_${System.currentTimeMillis()}.csv")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExportButton(
                    label = "Quality Grade Analysis (CSV)",
                    onClick = {
                        viewModel.prepareQualityAnalysisExport { content ->
                            csvContent = content
                            csvLauncher.launch("quality_analysis_${System.currentTimeMillis()}.csv")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExportButton(
                    label = "Risk Compliance Report (CSV)",
                    onClick = {
                        viewModel.prepareRiskReportExport { content ->
                            csvContent = content
                            csvLauncher.launch("risk_report_${System.currentTimeMillis()}.csv")
                        }
                    }
                )
            }

            SectionCard("Playbook Export") {
                ExportButton(
                    label = "Strategy Playbook (CSV)",
                    onClick = {
                        viewModel.prepareStrategyExport { content ->
                            csvContent = content
                            csvLauncher.launch("strategy_playbook_${System.currentTimeMillis()}.csv")
                        }
                    }
                )
            }

            SectionCard("Database Backup") {
                Button(
                    onClick = { backupLauncher.launch("compounding_journal_backup_${System.currentTimeMillis()}.db") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Backup Database File")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Restore Database File")
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }

        if (showRestoreConfirm) {
            ConfirmationDialog(
                title = "Confirm Restore",
                text = "Restoring will overwrite your current data. Are you sure you want to proceed?",
                confirmText = "Confirm Restoration",
                onConfirm = {
                    restoreUri?.let { uri ->
                        val success = BackupUtils.restoreDatabase(context, uri)
                        if (success) {
                            viewModel.showMessage("Restore successful. Please restart the app.")
                        } else {
                            viewModel.showMessage("Restore failed")
                        }
                    }
                    showRestoreConfirm = false
                },
                onDismiss = { showRestoreConfirm = false }
            )
        }
    }
}

@Composable
fun ExportButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
