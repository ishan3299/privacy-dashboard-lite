package com.ishan3299.privacydashboard.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ishan3299.privacydashboard.domain.model.AppInfo
import com.ishan3299.privacydashboard.domain.model.RiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    onAppClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Privacy Dashboard Lite") },
            text = {
                Column {
                    Text("Version 1.0.0")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Developed by Ishan")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This app helps you understand which apps might be exposing your privacy by analyzing their requested permissions.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { 
                    Text(
                        if (uiState.isAdvancedMode) "Privacy Dashboard (Adv)" else "Privacy Dashboard",
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall // Slightly smaller font for Medium
                    ) 
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSort() }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Sort",
                            tint = if (uiState.sortOption == SortOption.RISK_DESC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    showMenu = false
                                    showAboutDialog = true
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
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
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilterChips(
                            selectedRisk = uiState.selectedRiskFilter,
                            onRiskSelected = viewModel::onRiskFilterChange
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SummaryCard(
                            privacyScore = uiState.privacyScore,
                            isAdvanced = uiState.isAdvancedMode,
                            onToggleAdvanced = viewModel::toggleAdvancedMode
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Installed Apps (${uiState.filteredApps.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.filteredApps) { app ->
                        AppItem(app = app, isAdvanced = uiState.isAdvancedMode, onClick = { onAppClick(app.packageName) })
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search apps...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedRisk: RiskLevel?,
    onRiskSelected: (RiskLevel?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedRisk == null,
            onClick = { onRiskSelected(null) },
            label = { Text("All") }
        )
        RiskLevel.values().forEach { risk ->
            FilterChip(
                selected = selectedRisk == risk,
                onClick = { onRiskSelected(if (selectedRisk == risk) null else risk) },
                label = { Text(risk.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = getRiskColor(risk).copy(alpha = 0.2f),
                    selectedLabelColor = getRiskColor(risk)
                )
            )
        }
    }
}

fun getRiskColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.CRITICAL -> Color.Red
        RiskLevel.HIGH -> Color(0xFFFF9800)
        RiskLevel.MEDIUM -> Color(0xFFFFC107)
        RiskLevel.LOW -> Color(0xFF4CAF50)
    }
}

@Composable
fun SummaryCard(privacyScore: Int, isAdvanced: Boolean, onToggleAdvanced: (Boolean) -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Privacy Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$privacyScore",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (privacyScore > 80) Color(0xFF4CAF50) else if (privacyScore > 50) Color(0xFFFFC107) else Color.Red
                    )
                }
                
                // Ring Indicator Placeholder
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (privacyScore > 80) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                            else if (privacyScore > 50) Color(0xFFFFC107).copy(alpha = 0.2f) 
                            else Color.Red.copy(alpha = 0.2f)
                        )
                ) {
                   Icon(
                       imageVector = Icons.Default.Lock, 
                       contentDescription = null,
                       tint = if (privacyScore > 80) Color(0xFF4CAF50) else if (privacyScore > 50) Color(0xFFFFC107) else Color.Red,
                       modifier = Modifier.size(32.dp)
                   )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                 verticalAlignment = Alignment.CenterVertically,
                 modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                     "Advanced Mode", 
                     style = MaterialTheme.typography.bodyMedium,
                     modifier = Modifier.weight(1f)
                )
                Switch(checked = isAdvanced, onCheckedChange = onToggleAdvanced)
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo, isAdvanced: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(app.label, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            Column {
                if (isAdvanced) {
                   Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                Text("${app.sensitivePermissionCount} sensitive permissions", style = MaterialTheme.typography.bodySmall)
            }
        },
        leadingContent = {
            // App Icon Placeholder: Colored Circle with First Letter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                 Text(
                     text = app.label.take(1).uppercase(),
                     style = MaterialTheme.typography.titleMedium,
                     color = MaterialTheme.colorScheme.onPrimaryContainer
                 )
            }
        },
        trailingContent = {
            RiskBadge(riskLevel = app.riskLevel)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun RiskBadge(riskLevel: RiskLevel) {
    val color = when (riskLevel) {
        RiskLevel.CRITICAL -> Color.Red
        RiskLevel.HIGH -> Color(0xFFFF9800)
        RiskLevel.MEDIUM -> Color(0xFFFFC107)
        RiskLevel.LOW -> Color(0xFF4CAF50)
    }
    
    // Using Badges/Chips style
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = riskLevel.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
