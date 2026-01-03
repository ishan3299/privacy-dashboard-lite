package com.ishan3299.privacydashboard.domain

import com.ishan3299.privacydashboard.domain.model.RiskLevel

object PrivacyAnalyzer {

    private val SENSITIVE_PERMISSIONS = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.BODY_SENSORS",
        "android.permission.ACTIVITY_RECOGNITION"
    )

    fun calculateRisk(permissions: List<String>, exportedComponents: Int): RiskLevel {
        val sensitiveCount = permissions.count { it in SENSITIVE_PERMISSIONS }
        
        return when {
            sensitiveCount >= 5 || exportedComponents >= 10 -> RiskLevel.CRITICAL
            sensitiveCount >= 3 || exportedComponents >= 5 -> RiskLevel.HIGH
            sensitiveCount >= 1 || exportedComponents >= 2 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    fun isSensitive(permission: String): Boolean {
        return permission in SENSITIVE_PERMISSIONS
    }
}
