package com.loyalstring.rfid.ui.utils

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun TableMappingScreen(
    excelColumns: List<String>,
    bulkItemFields: List<String>,
    onDismiss: () -> Unit,
    fileselected: Boolean,
    onImport: (Map<String, String>) -> Unit,
    isFromSheet: Boolean// (ExcelCol -> DBField)
) {
    val mappings = remember { mutableStateMapOf<String, String>() }
    val context: Context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val filteredItems = remember(searchQuery.value, bulkItemFields) {
        val query = searchQuery.value.trim().lowercase()
        if (query.isBlank()) bulkItemFields
        else bulkItemFields.filter { item ->
            item.lowercase().contains(query)
        }
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = {

            // val headerGradient = Brush.verticalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .background(BackgroundGradient)

            ) {
                Column( modifier = Modifier.padding(5.dp) ) {
                    Text(
                        "Table View",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins,
                        color = Color.White
                    )
                    if (isFromSheet) {
                        Text(
                            "Select the fields that should  appear in the table view",
                            fontSize = 12.sp,
                            fontFamily = poppins,
                            color = Color.White,
                            lineHeight = 15.sp
                        )
                    } else {
                        Text(
                            "Select the fields that should  appear in the table view",
                            fontSize = 12.sp,
                            fontFamily = poppins,
                            color = Color.White,
                            lineHeight = 15.sp
                        )
                    }
                }
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
                        .height(45.dp)
                        .background(Color(0xFFF4F5F7), RoundedCornerShape(8.dp)) // light gray rounded bg
                        .padding(horizontal = 8.dp, vertical = 6.dp),             // match inner spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFromSheet) "Excel Column" else "Sheet Column",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f),

                        fontSize = 13.sp
                    )
               /*     Box(
                        modifier = Modifier
                            .weight(1.5f)          // share space with siblings
                            .fillMaxWidth()        // actually claim it
                    ) {
                        TextField(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            placeholder = { Text("Search", fontFamily = poppins, fontSize = 13.sp, color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp)   // avoid clipping; don't force 60.dp against a shorter parent
                                .widthIn(min = 160.dp),  // prevent collapsing to tiny width
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp) // keeps icon from eating vertical space
                                )
                            },
                            singleLine = true,
                            visualTransformation = VisualTransformation.None, // ✅ ensure not masked
                            shape = RoundedCornerShape(5.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 13.sp,
                                color = Color.Black
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedContainerColor = Color(0xFFF0F0F0),
                                unfocusedContainerColor = Color(0xFFF0F0F0),
                                disabledContainerColor = Color(0xFFF0F0F0),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }*/


                    Text(
                        "Table View Fields",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1.8f)
                            .padding(start = 8.dp),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))

                bulkItemFields.forEach { excelColumn ->
                    var expanded by remember { mutableStateOf(false) }
                    var selected by remember { mutableStateOf(mappings[excelColumn] ?: "") }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                        .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.8f)
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF4F5F7))
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Text(text = excelColumn, fontSize = 13.sp, color = Color(0xFF1F2937))
                        }


                        val fieldBg = Color(0xFFF4F5F7) // very light gray like the screenshot
                        val fieldShape = RoundedCornerShape(10.dp)

                       /* Box(modifier = Modifier.weight(2.0f)) {
                            var expanded by remember { mutableStateOf(false) }

                            CompactPickerField(
                                value = selected,
                                onClick = { expanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectTapGestures { expanded = true }   // hard capture tap
                                    }
                            )


                            val availableFields = excelColumns.filter { it == selected || !mappings.values.contains(it) }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .widthIn(min = 250.dp, max = 300.dp)
                                    .heightIn(max = 500.dp)
                            ) {
                                availableFields.forEach { dbField ->
                                    DropdownMenuItem(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = { Text(dbField, fontSize = 12.sp, maxLines = 1) },
                                        onClick = {
                                            selected = dbField
                                            mappings[excelColumn] = dbField
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }*/

                        @OptIn(ExperimentalMaterial3Api::class)
                        Box(modifier = Modifier.weight(2f)) {
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.zIndex(1f) // keep popup above neighbors
                            ) {
                                // Anchor: your compact, read-only pill
                                CompactPickerField(
                                    value = selected,
                                    onClick = { expanded = !expanded }, // optional, ExposedDropdown toggles on tap too
                                    modifier = Modifier
                                        .menuAnchor()                   // <<< critical for correct anchoring
                                        .fillMaxWidth()
                                )

                                val availableFields = excelColumns.filter { it == selected || !mappings.values.contains(it) }

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    availableFields.forEach { dbField ->
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
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                GradientButton(
                    text = "Cancel",
                    modifier = Modifier
                        .width(100.dp) // fixed width keeps them even
                        .height(48.dp),
                    onClick = onDismiss
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                GradientButton(
                    text = if (isFromSheet) "Sync" else "Import",
                    modifier = Modifier
                        .width(100.dp) // fixed width keeps them even
                        .height(48.dp),
                    onClick = {
                        if (fileselected) {
                            onImport(mappings)
                        } else {
                            ToastUtils.showToast(context, "Please Select file first")
                        }
                    }
                )




            }
        },
        dismissButton = {}


    )
}

@Composable
fun ExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    TODO("Not yet implemented")
}

@Composable
fun CompactPickerField(
    value: String,
    placeholder: String = "Map Column",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fieldBg = Color(0xFFF4F5F7)
    val textColor = Color(0xFF1F2937)
    val hintColor = Color(0xFF9AA0A6)
    val shape = RoundedCornerShape(10.dp)
    val indSrc = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = {},              // read-only
        readOnly = true,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = textColor),
        modifier = modifier
            .height(40.dp)              // <- exact compact height
            .clip(shape)
            .background(fieldBg)
            .clickable(
                indication = null,
                interactionSource = indSrc
            ) { onClick() }
            .padding(horizontal = 12.dp), // inner padding
        decorationBox = { inner ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 12.sp, color = hintColor)
                } else {
                    inner()
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
    )
}

