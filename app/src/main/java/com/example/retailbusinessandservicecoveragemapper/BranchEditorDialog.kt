package com.example.retailbusinessandservicecoveragemapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchEditorDialog(
    branch: Branch?,
    onDismiss: () -> Unit,
    onSave: (Branch) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(branch?.name ?: "") }
    var address by rememberSaveable { mutableStateOf(branch?.address ?: "") }
    var contact by rememberSaveable { mutableStateOf(branch?.contact ?: "") }
    var category by rememberSaveable { mutableStateOf(branch?.category ?: "Grocery") }
    var latitude by rememberSaveable { mutableStateOf(branch?.latitude?.toString() ?: "14.07326") }
    var longitude by rememberSaveable { mutableStateOf(branch?.longitude?.toString() ?: "120.63265") }
    var coverageRadius by rememberSaveable { mutableStateOf(branch?.coverageRadiusKm?.toString() ?: "2.5") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var barangayExpanded by rememberSaveable { mutableStateOf(false) }

    val categories = CATEGORIES.filter { it != "All" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (branch == null) "Add New Branch" else "Edit Branch", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Branch Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${getCategoryIcon(cat)} $cat") },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = barangayExpanded,
                    onExpandedChange = { barangayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Barangay / Address in Nasugbu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = barangayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = barangayExpanded,
                        onDismissRequest = { barangayExpanded = false }
                    ) {
                        NasugbuBoundary.NASUGBU_BARANGAYS
                            .asSequence()
                            .filter { it.contains(address, ignoreCase = true) }
                            .take(5)
                            .forEach { barangay ->
                                DropdownMenuItem(
                                    text = { Text(barangay) },
                                    onClick = {
                                        address = "$barangay, Nasugbu, Batangas"
                                        barangayExpanded = false
                                    }
                                )
                            }
                    }
                }

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = coverageRadius,
                    onValueChange = { coverageRadius = it },
                    label = { Text("Coverage Radius (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedLat = latitude.toDoubleOrNull()
                    val parsedLng = longitude.toDoubleOrNull()
                    val parsedRadius = coverageRadius.toDoubleOrNull()

                    when {
                        name.isBlank() -> errorMessage = "Branch name is required."
                        address.isBlank() -> errorMessage = "Address or barangay is required."
                        parsedLat == null || parsedLng == null -> errorMessage = "Enter valid latitude and longitude numbers."
                        parsedRadius == null || parsedRadius <= 0 -> errorMessage = "Enter a valid coverage radius in km."
                        !NasugbuBoundary.contains(parsedLat, parsedLng) ->
                            errorMessage = "⚠️ Location is OUTSIDE Nasugbu! The coverage area is strictly limited to Nasugbu, Batangas."
                        else -> {
                            val newId = branch?.id ?: System.currentTimeMillis()
                            onSave(
                                Branch(
                                    newId,
                                    name.trim(),
                                    address.trim(),
                                    contact.trim(),
                                    category,
                                    parsedLat,
                                    parsedLng,
                                    parsedRadius
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Save Branch")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
