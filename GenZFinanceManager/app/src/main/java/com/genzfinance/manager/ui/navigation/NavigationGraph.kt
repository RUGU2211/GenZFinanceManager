package com.genzfinance.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.genzfinance.manager.ui.screens.dashboard.DashboardScreen
import com.genzfinance.manager.ui.screens.transactions.TransactionsScreen
import com.genzfinance.manager.ui.screens.reports.ReportsScreen
import com.genzfinance.manager.ui.screens.settings.SettingsScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.DASHBOARD.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.DASHBOARD.route) {
            DashboardScreen(
                onAddTransaction = {
                    navController.navigate(BottomNavItem.TRANSACTIONS.route)
                }
            )
        }
        
        composable(BottomNavItem.TRANSACTIONS.route) {
            TransactionsScreen()
        }
        
        composable(BottomNavItem.REPORTS.route) {
            ReportsScreen()
        }
        
        composable(BottomNavItem.SETTINGS.route) {
            SettingsScreen()
        }
    }
}
