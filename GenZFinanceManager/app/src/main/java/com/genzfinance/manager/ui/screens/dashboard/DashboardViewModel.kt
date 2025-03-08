package com.genzfinance.manager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genzfinance.manager.data.model.Transaction
import com.genzfinance.manager.data.model.TransactionType
import com.genzfinance.manager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Temporary user ID - In a real app, this would come from authentication
    private val userId = "temp_user_id"

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                transactionRepository.getTransactionsFlow(userId)
                    .collect { transactions ->
                        val totalIncome = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }

                        val totalExpenses = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        val recentTransactions = transactions
                            .sortedByDescending { it.date }
                            .take(5)

                        _uiState.update {
                            it.copy(
                                totalIncome = totalIncome,
                                totalExpenses = totalExpenses,
                                totalBalance = totalIncome - totalExpenses,
                                recentTransactions = recentTransactions,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dashboard data: ${e.message}"
                    )
                }
            }
        }
    }

    fun getMonthlyTransactions() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, -1)
            val startDate = calendar.timeInMillis

            transactionRepository.getTransactionsByDateRange(userId, startDate, endDate)
                .onSuccess { transactions ->
                    // Process monthly transactions if needed
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to load monthly transactions: ${error.message}")
                    }
                }
        }
    }

    fun refreshDashboard() {
        loadDashboardData()
    }
}
