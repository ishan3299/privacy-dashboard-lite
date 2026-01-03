package com.ishan3299.privacydashboard.domain.model

data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val permissions: List<String>,
    val riskLevel: RiskLevel,
    val totalPermissions: Int = permissions.size,
    val sensitivePermissionCount: Int,
    val exportedComponentsCount: Int
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}
