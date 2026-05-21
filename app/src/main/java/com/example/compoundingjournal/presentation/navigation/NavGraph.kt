package com.example.compoundingjournal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.compoundingjournal.presentation.addtrade.AddTradeScreen
import com.example.compoundingjournal.presentation.addtrade.EditTradeScreen
import com.example.compoundingjournal.presentation.analytics.AnalyticsScreen
import com.example.compoundingjournal.presentation.dashboard.DashboardScreen
import com.example.compoundingjournal.presentation.exportbackup.ExportBackupScreen
import com.example.compoundingjournal.presentation.journal.JournalScreen
import com.example.compoundingjournal.presentation.journal.TradeDetailScreen
import com.example.compoundingjournal.presentation.settings.SettingsScreen
import com.example.compoundingjournal.presentation.strategy.AddStrategyScreen
import com.example.compoundingjournal.presentation.strategy.StrategyPlaybookScreen
import com.example.compoundingjournal.presentation.calendar.CalendarScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Journal.route) {
            JournalScreen(
                onAddTrade = { navController.navigate(Screen.AddTrade.route) },
                onTradeClick = { tradeId -> navController.navigate(Screen.TradeDetail.createRoute(tradeId)) },
                onEditTrade = { tradeId -> navController.navigate(Screen.EditTrade.createRoute(tradeId)) }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onTradeClick = { tradeId -> navController.navigate(Screen.TradeDetail.createRoute(tradeId)) }
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToExportBackup = { navController.navigate(Screen.ExportBackup.route) },
                onNavigateToStrategyPlaybook = { navController.navigate(Screen.StrategyPlaybook.route) }
            )
        }
        composable(Screen.AddTrade.route) {
            AddTradeScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditTrade.route,
            arguments = listOf(navArgument("tradeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tradeId = backStackEntry.arguments?.getLong("tradeId") ?: 0L
            EditTradeScreen(tradeId = tradeId, onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.TradeDetail.route,
            arguments = listOf(navArgument("tradeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tradeId = backStackEntry.arguments?.getLong("tradeId") ?: 0L
            TradeDetailScreen(
                tradeId = tradeId,
                onEditTrade = { id -> navController.navigate(Screen.EditTrade.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ExportBackup.route) {
            ExportBackupScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.StrategyPlaybook.route) {
            StrategyPlaybookScreen(
                onAddStrategy = { navController.navigate(Screen.AddStrategy.route) },
                onEditStrategy = { id -> navController.navigate(Screen.EditStrategy.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddStrategy.route) {
            AddStrategyScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditStrategy.route,
            arguments = listOf(navArgument("strategyId") { type = NavType.LongType })
        ) { backStackEntry ->
            val strategyId = backStackEntry.arguments?.getLong("strategyId") ?: 0L
            AddStrategyScreen(strategyId = strategyId, onNavigateBack = { navController.popBackStack() })
        }
    }
}
