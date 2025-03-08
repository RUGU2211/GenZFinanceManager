package com.genzfinance.manager.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val currency: String = "USD",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCurrencyDialog: Boolean = false,
    val availableCurrencies: List<String> = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD")
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isDarkMode = !it.isDarkMode)
            }
            // In a real app, save this preference to DataStore
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(notificationsEnabled = !it.notificationsEnabled)
            }
            // In a real app, save this preference to DataStore
        }
    }

    fun showCurrencyDialog() {
        _uiState.update { it.copy(showCurrencyDialog = true) }
    }

    fun hideCurrencyDialog() {
        _uiState.update { it.copy(showCurrencyDialog = false) }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    currency = currency,
                    showCurrencyDialog = false
                )
            }
            // In a real app, save this preference to DataStore
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // In a real app, implement sign out logic with Firebase Auth
            // For now, just show a mock implementation
            _uiState.update { 
                it.copy(error = "Sign out functionality will be implemented with Firebase Auth")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
