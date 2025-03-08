package com.genzfinance.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Home
    ),
    TRANSACTIONS(
        route = "transactions",
        title = "Transactions",
        icon = Icons.Default.List
    ),
    REPORTS(
        route = "reports",
        title = "Reports",
        icon = Icons.Default.Assessment
    ),
    SETTINGS(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
}
