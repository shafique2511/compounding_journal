package com.example.compoundingjournal.presentation.addtrade

import androidx.compose.runtime.Composable

@Composable
fun EditTradeScreen(
    tradeId: Long,
    onNavigateBack: () -> Unit
) {
    // Reuse AddTradeScreen for editing by passing the tradeId
    AddTradeScreen(
        onNavigateBack = onNavigateBack,
        tradeId = tradeId
    )
}
