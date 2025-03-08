package com.genzfinance.manager.ui.screens.reports

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time Range Selection
                item {
                    TimeRangeSelector(
                        selectedRange = uiState.selectedTimeRange,
                        onRangeSelected = { viewModel.onTimeRangeSelected(it) }
                    )
                }

                // Summary Cards
                item {
                    SummaryCards(
                        totalIncome = uiState.totalIncome,
                        totalExpenses = uiState.totalExpenses,
                        netSavings = uiState.netSavings,
                        savingsRate = uiState.savingsRate
                    )
                }

                // Expenses by Category
                item {
                    Text(
                        text = "Expenses by Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.expensesByCategory) { categoryTotal ->
                    CategoryProgressCard(
                        category = categoryTotal.category,
                        amount = categoryTotal.amount,
                        percentage = categoryTotal.percentage,
                        color = Color(0xFFF44336)
                    )
                }

                // Income by Category
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Income by Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.incomeByCategory) { categoryTotal ->
                    CategoryProgressCard(
                        category = categoryTotal.category,
                        amount = categoryTotal.amount,
                        percentage = categoryTotal.percentage,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Error Snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.values().forEach { range ->
            FilterChip(
                selected = range == selectedRange,
                onClick = { onRangeSelected(range) },
                label = { Text(range.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCards(
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    savingsRate: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Income and Expenses Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatCurrency(totalIncome),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatCurrency(totalExpenses),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }

        // Savings Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Net Savings",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatCurrency(netSavings),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (netSavings >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Text(
                    text = "Savings Rate: ${String.format("%.1f", savingsRate)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProgressCard(
    category: String,
    amount: Double,
    percentage: Float,
    color: Color
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = color
            )
            
            Text(
                text = String.format("%.1f%%", percentage),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    return if (amount < 0) {
        "-${format.format(amount.absoluteValue)}"
    } else {
        format.format(amount)
    }
}
