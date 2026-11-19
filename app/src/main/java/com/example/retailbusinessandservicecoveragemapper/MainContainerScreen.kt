package com.example.retailbusinessandservicecoveragemapper

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.net.toUri

val CATEGORIES = listOf("All", "Grocery", "Hardware", "Pharmacy", "Stationery", "Printing Shop", "Gasoline Stations")

enum class AppTab(val label: String, val badgeText: String) {
    DASHBOARD("Dashboard", "📊"),
    BRANCHES("Branches", "🏪"),
    MAP("Coverage Map", "🗺️"),
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "grocery" -> Color(0xFF0288D1)
        "hardware" -> Color(0xFFE65100)
        "pharmacy" -> Color(0xFFD32F2F)
        "stationery" -> Color(0xFF388E3C)
        "printing shop" -> Color(0xFFF57C00)
        "gasoline stations" -> Color(0xFFD84315)
        else -> Color(0xFF00796B)
    }
}

fun getCategoryIcon(category: String): String {
    return when (category.lowercase()) {
        "hardware" -> "🛠️"
        "grocery" -> "🛒"
        "pharmacy" -> "🏥"
        "stationery" -> "✏️"
        "printing shop" -> "🖨️"
        "gasoline stations" -> "⛽"
        else -> "🏬"
    }
}

fun openGoogleMapsDirections(context: Context, branch: Branch) {
    try {
        val uri = "https://www.google.com/maps/search/?api=1&query=${branch.latitude},${branch.longitude}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open Google Maps", Toast.LENGTH_SHORT).show()
    }
}

fun shareBranchInfo(context: Context, branch: Branch) {
    try {
        val shareText = """
            ${getCategoryIcon(branch.category)} ${branch.name}
            🏷️ Category: ${branch.category}
            📍 Address: ${branch.address}
            📞 Contact: ${branch.contact.ifBlank { "N/A" }}
            ⭕ Coverage Radius: ${branch.coverageRadiusKm} km
            🗺️ Location: https://maps.google.com/?q=${branch.latitude},${branch.longitude}
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share Branch Details"))
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to share branch details", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    isAdmin: Boolean,
    currentUser: User? = null,
    onSwitchMode: () -> Unit
) {
    val context = LocalContext.current
    val branches = remember {
        mutableStateListOf<Branch>().apply {
            addAll(BranchRepository.getAllBranches(context))
        }
    }

    fun refreshBranches() {
        branches.clear()
        branches.addAll(BranchRepository.getAllBranches(context))
    }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD.name) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var focusedBranchOnMap by remember { mutableStateOf<Branch?>(null) }

    var editingBranch by remember { mutableStateOf<Branch?>(null) }
    var isAddingBranch by remember { mutableStateOf(false) }
    var deletingBranch by remember { mutableStateOf<Branch?>(null) }

    val activeTab = AppTab.valueOf(selectedTab)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Track-It Nasugbu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isAdmin) "🔑 User: ${currentUser?.name ?: "System Administrator"}" else "📱 Mobile Customer Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSwitchMode) {
                        Text(if (isAdmin) "Log Out" else "Admin Access")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { selectedTab = tab.name },
                        icon = { Text(tab.badgeText) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (activeTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    branches = branches,
                    isAdmin = isAdmin,
                    onManageBranches = { selectedTab = AppTab.BRANCHES.name },
                    onViewMap = { selectedTab = AppTab.MAP.name },
                    onAddBranch = { isAddingBranch = true }
                )
                AppTab.BRANCHES -> BranchesScreen(
                    branches = branches,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    isAdmin = isAdmin,
                    onSearchQueryChange = { searchQuery = it },
                    onCategorySelect = { selectedCategory = it },
                    onAddBranch = { isAddingBranch = true },
                    onEditBranch = { editingBranch = it },
                    onDeleteBranch = { deletingBranch = it },
                    onViewOnMap = { branch ->
                        focusedBranchOnMap = branch
                        selectedTab = AppTab.MAP.name
                    }
                )
                AppTab.MAP -> CoverageMapScreen(
                    branches = branches,
                    focusedBranch = focusedBranchOnMap,
                    onClearFocus = { focusedBranchOnMap = null }
                )
            }
        }
    }

    if (isAddingBranch || (editingBranch != null)) {
        BranchEditorDialog(
            branch = editingBranch,
            onDismiss = {
                isAddingBranch = false
                editingBranch = null
            },
            onSave = { updatedBranch ->
                if (editingBranch != null) {
                    BranchRepository.updateBranch(context, updatedBranch)
                } else {
                    BranchRepository.addBranch(context, updatedBranch)
                }
                refreshBranches()
                isAddingBranch = false
                editingBranch = null
            }
        )
    }

    deletingBranch?.let { branch ->
        AlertDialog(
            onDismissRequest = { deletingBranch = null },
            title = { Text("Delete Branch") },
            text = { Text("Are you sure you want to delete '${branch.name}' from Nasugbu service coverage system?") },
            confirmButton = {
                Button(
                    onClick = {
                        BranchRepository.deleteBranch(context, branch.id)
                        refreshBranches()
                        deletingBranch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingBranch = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
