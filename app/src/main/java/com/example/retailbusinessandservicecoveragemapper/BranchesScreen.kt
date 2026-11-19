package com.example.retailbusinessandservicecoveragemapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BranchesScreen(
    branches: List<Branch>,
    searchQuery: String,
    selectedCategory: String,
    isAdmin: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onAddBranch: () -> Unit,
    onEditBranch: (Branch) -> Unit,
    onDeleteBranch: (Branch) -> Unit,
    onViewOnMap: (Branch) -> Unit
) {
    val filteredBranches = remember(branches, searchQuery, selectedCategory) {
        branches.filter { branch ->
            val matchesQuery = searchQuery.isBlank() ||
                branch.name.contains(searchQuery, ignoreCase = true) ||
                branch.address.contains(searchQuery, ignoreCase = true) ||
                branch.contact.contains(searchQuery, ignoreCase = true)
            val matchesCategory = (selectedCategory == "All") || branch.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Branch Directory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Displaying ${filteredBranches.size} branches in Nasugbu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (isAdmin) {
                Button(
                    onClick = onAddBranch,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Branch")
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search by name, barangay, contact...") },
            leadingIcon = { Text("🔍") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    TextButton(onClick = { onSearchQueryChange("") }) {
                        Text("✕", fontWeight = FontWeight.Bold)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CATEGORIES.forEach { category ->
                val icon = if (category == "All") "🔍" else getCategoryIcon(category)
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text("$icon $category") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filteredBranches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (branches.isEmpty()) "No branches added yet." else "No matching branches found in Nasugbu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBranches, key = { it.id }) { branch ->
                    val catColor = getCategoryColor(branch.category)
                    val icon = getCategoryIcon(branch.category)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$icon ${branch.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = catColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        branch.category,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = catColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("📍 ${branch.address}", style = MaterialTheme.typography.bodyMedium)
                            if (branch.contact.isNotBlank()) {
                                Text("📞 ${branch.contact}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "⭕ Radius: ${branch.coverageRadiusKm} km | Lat: ${branch.latitude}, Lng: ${branch.longitude}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onViewOnMap(branch) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🗺️ Map View")
                                }
                                OutlinedButton(
                                    onClick = { openGoogleMapsDirections(context, branch) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🧭 Directions")
                                }
                                TextButton(
                                    onClick = { shareBranchInfo(context, branch) }
                                ) {
                                    Text("📤 Share")
                                }
                                if (isAdmin) {
                                    TextButton(onClick = { onEditBranch(branch) }) {
                                        Text("✏️ Edit")
                                    }
                                    TextButton(onClick = { onDeleteBranch(branch) }) {
                                        Text("🗑️ Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
