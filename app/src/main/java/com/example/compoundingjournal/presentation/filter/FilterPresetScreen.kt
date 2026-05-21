package com.example.compoundingjournal.presentation.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.entity.FilterPresetEntity
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.FilterPresetRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.ConfirmationDialog
import com.example.compoundingjournal.presentation.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPresetScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { FilterPresetRepositoryImpl(database.filterPresetDao()) }
    val viewModel: FilterPresetViewModel = viewModel(
        factory = FilterPresetViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<FilterPresetEntity?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Filter Presets",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.presets.isEmpty()) {
            EmptyState(
                message = "No saved presets.\nSave your current filters from the Journal or Dashboard.",
                icon = Icons.Default.FilterList,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.presets) { preset ->
                    PresetCard(
                        preset = preset,
                        onDelete = { showDeleteDialog = preset }
                    )
                }
            }
        }

        if (showDeleteDialog != null) {
            ConfirmationDialog(
                title = "Delete Preset",
                text = "Are you sure you want to delete '${showDeleteDialog?.presetName}'?",
                onConfirm = {
                    showDeleteDialog?.let { viewModel.deletePreset(it) }
                    showDeleteDialog = null
                },
                onDismiss = { showDeleteDialog = null }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetCard(preset: FilterPresetEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.presetName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (preset.symbolFilter.isNotBlank()) FilterIndicator("Symbol: ${preset.symbolFilter}")
                if (preset.timeframeFilter.isNotBlank()) FilterIndicator("TF: ${preset.timeframeFilter}")
                if (preset.statusFilter.isNotBlank()) FilterIndicator("Status: ${preset.statusFilter}")
                if (preset.strategyFilter.isNotBlank()) FilterIndicator("Strategy: ${preset.strategyFilter}")
                if (preset.qualityGradeFilter.isNotBlank()) FilterIndicator("Grade: ${preset.qualityGradeFilter}")
                if (preset.ruleFollowedFilter.isNotBlank()) FilterIndicator("Rules: ${preset.ruleFollowedFilter}")
            }
        }
    }
}

@Composable
fun FilterIndicator(text: String) {
    SuggestionChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) }
    )
}
