package com.example.compoundingjournal.presentation.exportbackup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBackupScreen(onNavigateBack: () -> Unit) {
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
        }
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
                    Button(onClick = { /* TODO: Export CSV */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Export to CSV")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Database Backup", style = MaterialTheme.typography.titleMedium)
                    Text("Create a backup of your local database or restore from a file.", style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { /* TODO: Backup */ }, modifier = Modifier.weight(1f)) {
                            Text("Backup")
                        }
                        OutlinedButton(onClick = { /* TODO: Restore */ }, modifier = Modifier.weight(1f)) {
                            Text("Restore")
                        }
                    }
                }
            }
        }
    }
}
