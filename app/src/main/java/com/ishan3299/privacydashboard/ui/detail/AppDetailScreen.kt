package com.ishan3299.privacydashboard.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ishan3299.privacydashboard.PrivacyDashboardApplication
import com.ishan3299.privacydashboard.domain.model.PermissionDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    onBackClick: () -> Unit
) {
    val application = LocalContext.current.applicationContext as PrivacyDashboardApplication
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.provideFactory(application, packageName)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.appDetails?.appInfo?.label ?: "App Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                uiState.appDetails?.let { details ->
                    DetailContent(details, uiState.isAdvancedMode)
                }
            }
        }
    }
}

@Composable
fun DetailContent(details: com.ishan3299.privacydashboard.domain.model.AppDetails, isAdvanced: Boolean) {
    var showRiskInfo by remember { mutableStateOf(false) }

    if (showRiskInfo) {
        AlertDialog(
            onDismissRequest = { showRiskInfo = false },
            title = { Text("Understanding Privacy Risk") },
            text = {
                Text("This risk level is calculated based on the number of sensitive permissions requested and the number of components exported to other apps.\n\nHigh risk does not mean the app is malicious, but it has more potential to expose data.")
            },
            confirmButton = {
                TextButton(onClick = { showRiskInfo = false }) {
                    Text("Got it")
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Package: ${details.appInfo.packageName}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Total Permissions: ${details.appInfo.totalPermissions}")
                    // Only show component count if advanced or high risk
                    if (isAdvanced || details.appInfo.exportedComponentsCount > 0) {
                         Text(text = "Exposed Components: ${details.appInfo.exportedComponentsCount}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Risk Level: ${details.appInfo.riskLevel}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        IconButton(onClick = { showRiskInfo = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader("Permissions")
        }

        if (details.permissions.isEmpty()) {
            item { Text("No permissions requested.") }
        } else {
            // Sort: Sensitive first, then Dangerous, then others
            val sortedPermissions = details.permissions.sortedWith(
                compareByDescending<PermissionDetail> { it.isSensitive }
                    .thenByDescending { it.isDangerous }
                    .thenBy { it.name }
            )
            items(sortedPermissions) { perm ->
                PermissionItem(perm)
            }
        }

        if (isAdvanced) {
            if (details.activities.isNotEmpty()) {
                item { SectionHeader("Exported Activities") }
                items(details.activities) { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
            
            if (details.services.isNotEmpty()) {
                item { SectionHeader("Exported Services") }
                items(details.services) { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }

            if (details.receivers.isNotEmpty()) {
                item { SectionHeader("Exported Receivers") }
                items(details.receivers) { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        } else if (details.appInfo.exportedComponentsCount > 0) {
             item { 
                 SectionHeader("Exported Components") 
                 Text(
                     "Turn on Advanced Mode to see technical details about ${details.appInfo.exportedComponentsCount} exposed components.",
                     style = MaterialTheme.typography.bodySmall,
                     fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                 )
             }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun PermissionItem(perm: PermissionDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (perm.isSensitive) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer) else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = perm.name.substringAfterLast("."),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (perm.isSensitive || perm.isDangerous) {
                 Text(
                    text = if (perm.isSensitive) "SENSITIVE" else "DANGEROUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = perm.description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
