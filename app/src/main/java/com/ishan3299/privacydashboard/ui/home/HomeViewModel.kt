package com.ishan3299.privacydashboard.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ishan3299.privacydashboard.PrivacyDashboardApplication
import com.ishan3299.privacydashboard.data.AppRepository
import com.ishan3299.privacydashboard.data.UserPreferencesRepository
import com.ishan3299.privacydashboard.domain.model.AppInfo
import com.ishan3299.privacydashboard.domain.model.RiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedRiskFilter: RiskLevel? = null,
    val isAdvancedMode: Boolean = false,
    val privacyScore: Int = 100,
    val isLoading: Boolean = true,
    val error: String? = null,
    val sortOption: SortOption = SortOption.RISK_DESC
)

enum class SortOption {
    NAME_ASC, RISK_DESC
}

class HomeViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadApps()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.isAdvancedMode.collect { isAdvanced ->
                // Apply filters with NEW advanced mode
                val newState = _uiState.value.copy(isAdvancedMode = isAdvanced)
                val filtered = applyFilters(newState.allApps, newState.searchQuery, newState.selectedRiskFilter, newState.sortOption)
                _uiState.value = newState.copy(filteredApps = filtered)
            }
        }
    }

    fun toggleAdvancedMode(enabled: Boolean) {
        preferencesRepository.setAdvancedMode(enabled)
    }

    fun toggleSort() {
        val currentSort = _uiState.value.sortOption
        val newSort = if (currentSort == SortOption.RISK_DESC) SortOption.NAME_ASC else SortOption.RISK_DESC
        
        val newState = _uiState.value.copy(sortOption = newSort)
        val filtered = applyFilters(newState.allApps, newState.searchQuery, newState.selectedRiskFilter, newSort)
        _uiState.value = newState.copy(filteredApps = filtered)
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val apps = repository.getInstalledApps()
                val score = calculatePrivacyScore(apps)
                val filtered = applyFilters(apps, _uiState.value.searchQuery, _uiState.value.selectedRiskFilter, _uiState.value.sortOption)
                
                _uiState.value = _uiState.value.copy(
                    allApps = apps,
                    filteredApps = filtered,
                    privacyScore = score,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }
    
    // ... calculatePrivacyScore remains ...
    private fun calculatePrivacyScore(apps: List<AppInfo>): Int {
        if (apps.isEmpty()) return 100
        val criticalCount = apps.count { it.riskLevel == RiskLevel.CRITICAL }
        val highCount = apps.count { it.riskLevel == RiskLevel.HIGH }
        
        val penalty = (criticalCount * 5) + (highCount * 2)
        return (100 - penalty).coerceIn(0, 100)
    }

    fun onSearchQueryChange(query: String) {
        val newState = _uiState.value.copy(searchQuery = query)
        val filtered = applyFilters(newState.allApps, query, newState.selectedRiskFilter, newState.sortOption)
        _uiState.value = newState.copy(filteredApps = filtered)
    }
    
    fun onRiskFilterChange(riskLevel: RiskLevel?) {
        val newState = _uiState.value.copy(selectedRiskFilter = riskLevel)
        val filtered = applyFilters(newState.allApps, newState.searchQuery, riskLevel, newState.sortOption)
        _uiState.value = newState.copy(filteredApps = filtered)
    }

    private fun applyFilters(
        apps: List<AppInfo>, 
        query: String, 
        riskFilter: RiskLevel?,
        sortOption: SortOption
    ): List<AppInfo> {
        var filtered = if (query.isBlank()) apps else apps.filter {
            it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
        
        if (riskFilter != null) {
            filtered = filtered.filter { it.riskLevel == riskFilter }
        }
        
        return when(sortOption) {
            SortOption.NAME_ASC -> filtered.sortedBy { it.label }
            SortOption.RISK_DESC -> filtered.sortedByDescending { it.riskLevel }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PrivacyDashboardApplication)
                HomeViewModel(application.appRepository, application.userPreferencesRepository)
            }
        }
    }
}
