package com.example.compoundingjournal.presentation.exportbackup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
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
    val strategyRepository = remember { com.example.compoundingjournal.data.repository.StrategyRepositoryImpl(database.strategyDao()) }
    val viewModel: ExportBackupViewModel = viewModel(
        factory = ExportBackupViewModelFactory(tradeRepository, strategyRepository)
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionCard("Export History") {
                Text(
                    text = "Download your trading data to an external CSV file compatible with Excel or Google Sheets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.prepareAllTradesExport { content ->
                            csvContent = content
                            csvLauncher.launch("trades_report_${System.currentTimeMillis()}.csv")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export All Trades (CSV)")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = {
                        viewModel.prepareSummaryExport { content ->
                            csvContent = content
                            csvLauncher.launch("performance_summary_${System.currentTimeMillis()}.csv")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Export Performance Summary")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.prepareStrategyExport { content ->
                            csvContent = content
                            csvLauncher.launch("strategy_playbook_${System.currentTimeMillis()}.csv")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Export Strategy Playbook")
                }
            }

            SectionCard("Cloud-less Backup") {
                Text(
                    text = "Secure your journal locally. We don't store your data on servers, so keep your backups safe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { backupLauncher.launch("journal_backup_${System.currentTimeMillis()}.db") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Create Backup")
                    }
                    
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Restore Data")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showRestoreConfirm) {
            ConfirmationDialog(
                title = "Overwrite Current Data",
                text = "Restoring from a backup will replace your current trading journal completely. This cannot be undone.",
                confirmText = "Confirm Restoration",
                onConfirm = {
                    restoreUri?.let { uri ->
                        val success = BackupUtils.restoreDatabase(context, uri)
                        if (success) {
                            viewModel.showMessage("Journal restored successfully. Restarting app recommended.")
                        } else {
                            viewModel.showMessage("Database restoration failed")
                        }
                    }
                    showRestoreConfirm = false
                },
                onDismiss = { showRestoreConfirm = false }
            )
        }
    }
}
