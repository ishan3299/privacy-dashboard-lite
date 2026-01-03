package com.ishan3299.privacydashboard.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ishan3299.privacydashboard.PrivacyDashboardApplication
import com.ishan3299.privacydashboard.data.AppRepository
import com.ishan3299.privacydashboard.domain.model.AppDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.ishan3299.privacydashboard.data.UserPreferencesRepository

data class DetailUiState(
    val appDetails: AppDetails? = null,
    val isAdvancedMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DetailViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val packageName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
        observePreferences()
    }
    
    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.isAdvancedMode.collect { isAdvanced ->
                _uiState.value = _uiState.value.copy(isAdvancedMode = isAdvanced)
            }
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val details = repository.getAppDetailsFull(packageName)
            if (details != null) {
                _uiState.value = _uiState.value.copy(appDetails = details, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(error = "App not found", isLoading = false)
            }
        }
    }

    companion object {
        fun provideFactory(application: PrivacyDashboardApplication, packageName: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DetailViewModel(application.appRepository, application.userPreferencesRepository, packageName)
            }
        }
    }
}
