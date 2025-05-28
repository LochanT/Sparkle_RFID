package com.loyalstring.rfid.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TableMappingScreen(
    excelColumns: List<String>,
    bulkItemFields: List<String>,
    onDismiss: () -> Unit,
    onImport: (Map<String, String>) -> Unit
) {
    val mappings = remember { mutableStateMapOf<String, String>() }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Table Mapping", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Map each database field to a column from the Excel file.")
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Excel Columns", fontSize = 14.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                val filteredColumns = excelColumns.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    bulkItemFields.forEach { dbField ->
                        var expanded by remember { mutableStateOf(false) }
                        var selected by remember { mutableStateOf(mappings[dbField] ?: "") }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dbField,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .padding(end = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Box(modifier = Modifier.weight(2f)) {
                                OutlinedTextField(
                                    value = selected,
                                    onValueChange = {},
                                    label = { Text("Select Column", fontSize = 12.sp) },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = true },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { expanded = !expanded }
                                        )
                                    }
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    filteredColumns.forEach { column ->
                                        DropdownMenuItem(
                                            text = { Text(column, fontSize = 12.sp) },
                                            onClick = {
                                                selected = column
                                                mappings[dbField] = column
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GradientButton(text = "Import", onClick = { onImport(mappings) })


        },
        dismissButton = {
            GradientButton(text = "Cancel", onClick = onDismiss)
        }
    )
}

