package com.genzfinance.manager.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genzfinance.manager.data.model.Transaction
import com.genzfinance.manager.data.model.TransactionType
import com.genzfinance.manager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CategoryTotal(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class ReportsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netSavings: Double = 0.0,
    val savingsRate: Float = 0f,
    val expensesByCategory: List<CategoryTotal> = emptyList(),
    val incomeByCategory: List<CategoryTotal> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.MONTH,
    val transactions: List<Transaction> = emptyList()
)

enum class TimeRange {
    WEEK,
    MONTH,
    YEAR,
    ALL
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    // Temporary user ID - In a real app, this would come from authentication
    private val userId = "temp_user_id"

    init {
        loadReportData()
    }

    fun onTimeRangeSelected(timeRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = timeRange) }
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val startDate = getStartDate(_uiState.value.selectedTimeRange)
                val endDate = Calendar.getInstance().timeInMillis

                transactionRepository.getTransactionsByDateRange(userId, startDate, endDate)
                    .onSuccess { transactions ->
                        val income = transactions.filter { it.type == TransactionType.INCOME }
                        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

                        val totalIncome = income.sumOf { it.amount }
                        val totalExpenses = expenses.sumOf { it.amount }
                        val netSavings = totalIncome - totalExpenses
                        val savingsRate = if (totalIncome > 0) 
                            (netSavings / totalIncome * 100).toFloat() else 0f

                        val expensesByCategory = calculateCategoryTotals(expenses)
                        val incomeByCategory = calculateCategoryTotals(income)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalIncome = totalIncome,
                                totalExpenses = totalExpenses,
                                netSavings = netSavings,
                                savingsRate = savingsRate,
                                expensesByCategory = expensesByCategory,
                                incomeByCategory = incomeByCategory,
                                transactions = transactions,
                                error = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load report data: ${error.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load report data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun calculateCategoryTotals(transactions: List<Transaction>): List<CategoryTotal> {
        val totalAmount = transactions.sumOf { it.amount }
        return transactions
            .groupBy { it.category }
            .map { (category, categoryTransactions) ->
                val categoryTotal = categoryTransactions.sumOf { it.amount }
                CategoryTotal(
                    category = category,
                    amount = categoryTotal,
                    percentage = (categoryTotal / totalAmount * 100).toFloat()
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun getStartDate(timeRange: TimeRange): Long {
        val calendar = Calendar.getInstance()
        when (timeRange) {
            TimeRange.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            TimeRange.MONTH -> calendar.add(Calendar.MONTH, -1)
            TimeRange.YEAR -> calendar.add(Calendar.YEAR, -1)
            TimeRange.ALL -> calendar.add(Calendar.YEAR, -100) // Arbitrary past date
        }
        return calendar.timeInMillis
    }
}
