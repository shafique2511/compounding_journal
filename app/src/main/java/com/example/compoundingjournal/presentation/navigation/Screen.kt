package com.example.compoundingjournal.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Event
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Journal : Screen("journal", "Journal", Icons.Default.List)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.Event)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    
    object AddTrade : Screen("add_trade", "Add Trade")
    object EditTrade : Screen("edit_trade/{tradeId}", "Edit Trade") {
        fun createRoute(tradeId: Long) = "edit_trade/$tradeId"
    }
    object TradeDetail : Screen("trade_detail/{tradeId}", "Trade Detail") {
        fun createRoute(tradeId: Long) = "trade_detail/$tradeId"
    }
    object ExportBackup : Screen("export_backup", "Export & Backup")

    object StrategyPlaybook : Screen("strategy_playbook", "Strategy Playbook")
    object AddStrategy : Screen("add_strategy", "Add Strategy")
    object EditStrategy : Screen("edit_strategy/{strategyId}", "Edit Strategy") {
        fun createRoute(strategyId: Long) = "edit_strategy/$strategyId"
    }
    
    object FilterPresets : Screen("filter_presets", "Filter Presets")
}
