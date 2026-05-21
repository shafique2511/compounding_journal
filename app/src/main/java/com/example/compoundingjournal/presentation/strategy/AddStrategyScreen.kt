package com.example.compoundingjournal.presentation.strategy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compoundingjournal.data.local.AppDatabase
import com.example.compoundingjournal.data.repository.StrategyRepositoryImpl
import com.example.compoundingjournal.presentation.components.AppTopBar
import com.example.compoundingjournal.presentation.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStrategyScreen(
    strategyId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { StrategyRepositoryImpl(database.strategyDao()) }
    val viewModel: AddStrategyViewModel = viewModel(
        factory = AddStrategyViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(strategyId) {
        if (strategyId != null) {
            viewModel.initForEdit(strategyId)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (strategyId == null) "New Strategy" else "Edit Strategy",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(StrategyFormEvent.Save(onNavigateBack)) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionCard("General Info") {
                OutlinedTextField(
                    value = uiState.strategyName,
                    onValueChange = { viewModel.onEvent(StrategyFormEvent.NameChanged(it)) },
                    label = { Text("Strategy Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.marketType,
                        onValueChange = { viewModel.onEvent(StrategyFormEvent.MarketTypeChanged(it)) },
                        label = { Text("Market (e.g. Forex)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.timeframe,
                        onValueChange = { viewModel.onEvent(StrategyFormEvent.TimeframeChanged(it)) },
                        label = { Text("Timeframe") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.isActive,
                        onCheckedChange = { viewModel.onEvent(StrategyFormEvent.IsActiveChanged(it)) }
                    )
                    Text("Strategy is Active")
                }
            }

            SectionCard("Trading Rules") {
                RuleInputField("Entry Rules", uiState.entryRules) { viewModel.onEvent(StrategyFormEvent.EntryRulesChanged(it)) }
                RuleInputField("Exit Rules", uiState.exitRules) { viewModel.onEvent(StrategyFormEvent.ExitRulesChanged(it)) }
                RuleInputField("Stop Loss Rules", uiState.stopLossRules) { viewModel.onEvent(StrategyFormEvent.StopLossRulesChanged(it)) }
                RuleInputField("Take Profit Rules", uiState.takeProfitRules) { viewModel.onEvent(StrategyFormEvent.TakeProfitRulesChanged(it)) }
                RuleInputField("Risk Rules", uiState.riskRules) { viewModel.onEvent(StrategyFormEvent.RiskRulesChanged(it)) }
            }

            SectionCard("Documentation") {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(StrategyFormEvent.NotesChanged(it)) },
                    label = { Text("Strategy Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RuleInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        minLines = 2
    )
}
