package com.ishan3299.privacydashboard.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository {
    private val _isAdvancedMode = MutableStateFlow(false)
    val isAdvancedMode: StateFlow<Boolean> = _isAdvancedMode.asStateFlow()

    fun setAdvancedMode(enabled: Boolean) {
        _isAdvancedMode.value = enabled
    }
}
