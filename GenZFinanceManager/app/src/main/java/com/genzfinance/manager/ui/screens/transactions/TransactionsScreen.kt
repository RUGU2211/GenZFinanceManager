package com.genzfinance.manager.ui.screens.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.genzfinance.manager.data.model.Transaction
import com.genzfinance.manager.data.model.TransactionCategory
import com.genzfinance.manager.data.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddTransactionClick() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onDeleteClick = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }

            // Add Transaction Dialog
            if (uiState.isAddingTransaction) {
                AddTransactionDialog(
                    state = uiState.newTransaction,
                    onDismiss = { viewModel.onDismissAddTransaction() },
                    onAmountChange = { viewModel.onAmountChanged(it) },
                    onDescriptionChange = { viewModel.onDescriptionChanged(it) },
                    onTypeChange = { viewModel.onTypeChanged(it) },
                    onCategoryChange = { viewModel.onCategoryChanged(it) },
                    onDateChange = { viewModel.onDateChanged(it) },
                    onSave = { viewModel.onSaveTransaction() }
                )
            }

            // Error Snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatAmount(transaction.amount, transaction.type),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.type == TransactionType.INCOME)
                        Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    state: NewTransactionState,
    onDismiss: () -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (TransactionCategory) -> Unit,
    onDateChange: (Date) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount Input
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.amountError != null,
                    supportingText = state.amountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Transaction Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { onTypeChange(type) },
                            label = { Text(type.name) },
                            leadingIcon = {
                                Icon(
                                    if (type == TransactionType.INCOME)
                                        Icons.Default.ArrowUpward
                                    else Icons.Default.ArrowDownward,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = state.category.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Description Input
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    isError = state.descriptionError != null,
                    supportingText = state.descriptionError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.amountError == null && state.descriptionError == null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatAmount(amount: Double, type: TransactionType): String {
    val format = NumberFormat.getCurrencyInstance()
    return when (type) {
        TransactionType.INCOME -> format.format(amount)
        TransactionType.EXPENSE -> "-${format.format(amount)}"
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
}
