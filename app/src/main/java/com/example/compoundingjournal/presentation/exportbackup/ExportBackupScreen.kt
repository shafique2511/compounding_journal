package com.example.compoundingjournal.presentation.exportbackup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.TradeRepositoryImpl
import com.example.compoundingjournal.utils.BackupUtils
import com.example.compoundingjournal.utils.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBackupScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TradeRepositoryImpl(database.tradeDao()) }
    val viewModel: ExportBackupViewModel = viewModel(
        factory = ExportBackupViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var csvContent by remember { mutableStateOf("") }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            val success = ExportUtils.writeTextToUri(context, it, csvContent)
            viewModel.showMessage(if (success) "Export successful" else "Export failed")
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let {
            val success = BackupUtils.backupDatabase(context, it)
            viewModel.showMessage(if (success) "Backup successful" else "Backup failed")
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
            TopAppBar(
                title = { Text("Export & Backup") },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export Data", style = MaterialTheme.typography.titleMedium)
                    Text("Download your trade history in CSV format.", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = {
                            viewModel.prepareAllTradesExport { content ->
                                csvContent = content
                                csvLauncher.launch("trades_export_${System.currentTimeMillis()}.csv")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export All Trades to CSV")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            viewModel.prepareSummaryExport { content ->
                                csvContent = content
                                csvLauncher.launch("summary_export_${System.currentTimeMillis()}.csv")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Summary to CSV")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Database Backup & Restore", style = MaterialTheme.typography.titleMedium)
                    Text("Create a backup of your local database or restore from a file.", style = MaterialTheme.typography.bodySmall)
                    
                    Button(
                        onClick = { backupLauncher.launch("compounding_journal_backup_${System.currentTimeMillis()}.db") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Backup Database")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore Database")
                    }
                }
            }
        }

        if (showRestoreConfirm) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirm = false },
                title = { Text("Confirm Restore") },
                text = { Text("Restoring will overwrite your current data. Are you sure you want to proceed?") },
                confirmButton = {
                    TextButton(onClick = {
                        restoreUri?.let { uri ->
                            // In a production app, we'd close the DB properly before this.
                            // For this phase, we perform the file copy.
                            val success = BackupUtils.restoreDatabase(context, uri)
                            if (success) {
                                viewModel.showMessage("Restore successful. Please restart the app.")
                            } else {
                                viewModel.showMessage("Restore failed")
                            }
                        }
                        showRestoreConfirm = false
                    }) {
                        Text("Restore", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
