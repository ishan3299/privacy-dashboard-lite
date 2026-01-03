package com.ishan3299.privacydashboard.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import com.ishan3299.privacydashboard.domain.PrivacyAnalyzer
import com.ishan3299.privacydashboard.domain.model.AppDetails
import com.ishan3299.privacydashboard.domain.model.AppInfo
import com.ishan3299.privacydashboard.domain.model.PermissionDetail
import android.content.pm.PermissionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        // Optimization: Only fetching permissions. Fetching all components (activities, services, etc.)
        // for ALL apps is too heavy and causes UI jank due to massive memory allocation/GC.
        // We will rely primarily on Permissions for the Dashboard Risk Score.
        // Detailed component analysis is deferred to the Detail Screen.
        val flags = PackageManager.GET_PERMISSIONS

        val packages = packageManager.getInstalledPackages(flags)

        packages.mapNotNull { packageInfo ->
            if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) == null && isSystemApp(packageInfo)) {
                return@mapNotNull null
            }
            
            convertPackageToAppInfoLite(packageInfo, packageManager)
        }
    }

    private fun isSystemApp(packageInfo: PackageInfo): Boolean {
        return (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    // LITE version for List View - skips component counting
    private fun convertPackageToAppInfoLite(packageInfo: PackageInfo, pm: PackageManager): AppInfo {
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        val appName = packageInfo.applicationInfo.loadLabel(pm).toString()
        
        // We don't have component info in the LITE fetch, so we default to 0.
        // This makes the list load much faster.
        val exportedComponents = 0
        
        val sensitiveCount = permissions.count { PrivacyAnalyzer.isSensitive(it) }
        
        // Risk essentially based on permissions only for the overview
        val risk = PrivacyAnalyzer.calculateRisk(permissions, exportedComponents)

        return AppInfo(
            packageName = packageInfo.packageName,
            label = appName,
            isSystem = isSystemApp(packageInfo),
            permissions = permissions,
            riskLevel = risk,
            sensitivePermissionCount = sensitiveCount,
            exportedComponentsCount = exportedComponents
        )
    }

    private fun convertPackageToAppInfo(packageInfo: PackageInfo, pm: PackageManager): AppInfo {
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        val appName = packageInfo.applicationInfo.loadLabel(pm).toString()
        
        val activities = packageInfo.activities?.count { it.exported } ?: 0
        val services = packageInfo.services?.count { it.exported } ?: 0
        val receivers = packageInfo.receivers?.count { it.exported } ?: 0
        val providers = packageInfo.providers?.count { it.exported } ?: 0
        
        val exportedComponents = activities + services + receivers + providers
        val sensitiveCount = permissions.count { PrivacyAnalyzer.isSensitive(it) }
        
        val risk = PrivacyAnalyzer.calculateRisk(permissions, exportedComponents)

        return AppInfo(
            packageName = packageInfo.packageName,
            label = appName,
            isSystem = isSystemApp(packageInfo),
            permissions = permissions,
            riskLevel = risk,
            sensitivePermissionCount = sensitiveCount,
            exportedComponentsCount = exportedComponents
        )
    }

    suspend fun getAppDetail(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val flags = PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS
            val packageInfo = packageManager.getPackageInfo(packageName, flags)
            convertPackageToAppInfo(packageInfo, packageManager)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    suspend fun getAppDetailsFull(packageName: String): AppDetails? = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val flags = PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS
            val packageInfo = packageManager.getPackageInfo(packageName, flags)
            
            val appInfo = convertPackageToAppInfo(packageInfo, packageManager)
            
            // Process Permissions
            val permissionDetails = packageInfo.requestedPermissions?.map { permName ->
                getPermissionDetail(permName, packageManager)
            } ?: emptyList()

            // Process Components (only exported ones are interesting for privacy)
            val activities = packageInfo.activities?.filter { it.exported }?.map { it.name } ?: emptyList()
            val services = packageInfo.services?.filter { it.exported }?.map { it.name } ?: emptyList()
            val receivers = packageInfo.receivers?.filter { it.exported }?.map { it.name } ?: emptyList()
            val providers = packageInfo.providers?.filter { it.exported }?.map { it.name } ?: emptyList()

            AppDetails(
                appInfo = appInfo,
                permissions = permissionDetails,
                activities = activities,
                services = services,
                receivers = receivers,
                providers = providers
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getPermissionDetail(permissionName: String, pm: PackageManager): PermissionDetail {
        var description = "No description available"
        var isDangerous = false
        
        try {
            val permInfo = pm.getPermissionInfo(permissionName, 0)
            val label = permInfo.loadLabel(pm).toString()
            val desc = permInfo.loadDescription(pm)?.toString()
            description = if (!desc.isNullOrEmpty()) desc else label
            
            isDangerous = (permInfo.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            // Permission definition not found (maybe custom permission or system hidden)
        }

        return PermissionDetail(
            name = permissionName,
            description = description,
            isSensitive = PrivacyAnalyzer.isSensitive(permissionName),
            isDangerous = isDangerous
        )
    }
}
