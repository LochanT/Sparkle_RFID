package com.loyalstring.rfid.ui.utils

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TableMappingScreen(
    excelColumns: List<String>,
    bulkItemFields: List<String>,
    onDismiss: () -> Unit,
    fileselected: Boolean,
    onImport: (Map<String, String>) -> Unit // (ExcelCol -> DBField)
) {
    val mappings = remember { mutableStateMapOf<String, String>() }
    val context: Context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Table Mapping", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Map each Excel column to a Database field.")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Excel Column",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f),
                        fontSize = 13.sp
                    )
                    Text(
                        "Map to DB Field",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f),
                        fontSize = 13.sp
                    )
                }

                excelColumns.forEach { excelColumn ->
                    var expanded by remember { mutableStateOf(false) }
                    var selected by remember { mutableStateOf(mappings[excelColumn] ?: "") }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = excelColumn,
                            modifier = Modifier
                                .weight(1.2f)
                                .padding(end = 8.dp),
                            fontSize = 14.sp
                        )

                        Box(modifier = Modifier.weight(2f)) {
                            OutlinedTextField(
                                value = selected,
                                onValueChange = {},
                                label = { Text("Select DB Field", fontSize = 12.sp) },
                                singleLine = true,
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
                                bulkItemFields.forEach { dbField ->
                                    DropdownMenuItem(
                                        text = { Text(dbField, fontSize = 12.sp, maxLines = 1) },
                                        onClick = {
                                            selected = dbField
                                            mappings[excelColumn] = dbField
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GradientButton(
                text = "Import",
                onClick = {
                    if (fileselected) {
                        onImport(mappings)

                    } else {
                        ToastUtils.showToast(context, "Please Select file first")
                    }
                }, // Call with ExcelCol → DBField
            )
        },
        dismissButton = {
            GradientButton(text = "Cancel", onClick = onDismiss)
        }
    )
}

