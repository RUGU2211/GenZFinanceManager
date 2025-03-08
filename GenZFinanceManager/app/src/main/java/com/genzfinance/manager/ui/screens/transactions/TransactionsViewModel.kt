package com.genzfinance.manager.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genzfinance.manager.data.model.Transaction
import com.genzfinance.manager.data.model.TransactionCategory
import com.genzfinance.manager.data.model.TransactionType
import com.genzfinance.manager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingTransaction: Boolean = false,
    val newTransaction: NewTransactionState = NewTransactionState()
)

data class NewTransactionState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val description: String = "",
    val date: Date = Date(),
    val amountError: String? = null,
    val descriptionError: String? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    // Temporary user ID - In a real app, this would come from authentication
    private val userId = "temp_user_id"

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                transactionRepository.getTransactionsFlow(userId)
                    .collect { transactions ->
                        _uiState.update {
                            it.copy(
                                transactions = transactions.sortedByDescending { t -> t.date },
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load transactions: ${e.message}"
                    )
                }
            }
        }
    }

    fun onAmountChanged(amount: String) {
        _uiState.update {
            it.copy(
                newTransaction = it.newTransaction.copy(
                    amount = amount,
                    amountError = validateAmount(amount)
                )
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(
                newTransaction = it.newTransaction.copy(
                    description = description,
                    descriptionError = validateDescription(description)
                )
            )
        }
    }

    fun onTypeChanged(type: TransactionType) {
        _uiState.update {
            it.copy(newTransaction = it.newTransaction.copy(type = type))
        }
    }

    fun onCategoryChanged(category: TransactionCategory) {
        _uiState.update {
            it.copy(newTransaction = it.newTransaction.copy(category = category))
        }
    }

    fun onDateChanged(date: Date) {
        _uiState.update {
            it.copy(newTransaction = it.newTransaction.copy(date = date))
        }
    }

    fun onAddTransactionClick() {
        _uiState.update { it.copy(isAddingTransaction = true) }
    }

    fun onDismissAddTransaction() {
        _uiState.update {
            it.copy(
                isAddingTransaction = false,
                newTransaction = NewTransactionState()
            )
        }
    }

    fun onSaveTransaction() {
        val currentState = _uiState.value.newTransaction
        
        // Validate inputs
        val amountError = validateAmount(currentState.amount)
        val descriptionError = validateDescription(currentState.description)
        
        if (amountError != null || descriptionError != null) {
            _uiState.update {
                it.copy(
                    newTransaction = it.newTransaction.copy(
                        amountError = amountError,
                        descriptionError = descriptionError
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val amount = currentState.amount.toDoubleOrNull() ?: 0.0
                val transaction = Transaction(
                    amount = amount,
                    type = currentState.type,
                    category = currentState.category.displayName,
                    description = currentState.description,
                    date = currentState.date,
                    userId = userId
                )

                transactionRepository.addTransaction(transaction)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isAddingTransaction = false,
                                newTransaction = NewTransactionState()
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(error = "Failed to save transaction: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to save transaction: ${e.message}")
                }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transactionId)
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(error = "Failed to delete transaction: ${error.message}")
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete transaction: ${e.message}")
                }
            }
        }
    }

    private fun validateAmount(amount: String): String? {
        return when {
            amount.isEmpty() -> "Amount is required"
            amount.toDoubleOrNull() == null -> "Invalid amount"
            amount.toDouble() <= 0 -> "Amount must be greater than 0"
            else -> null
        }
    }

    private fun validateDescription(description: String): String? {
        return when {
            description.isEmpty() -> "Description is required"
            description.length < 3 -> "Description must be at least 3 characters"
            else -> null
        }
    }
}
