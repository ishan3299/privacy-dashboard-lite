package com.ishan3299.privacydashboard.domain.model

data class AppDetails(
    val appInfo: AppInfo,
    val permissions: List<PermissionDetail>,
    val activities: List<String>,
    val services: List<String>,
    val receivers: List<String>,
    val providers: List<String>
)

data class PermissionDetail(
    val name: String,
    val description: String,
    val isSensitive: Boolean,
    val isDangerous: Boolean
)
