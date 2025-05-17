
package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.AddItemDialog
import com.loyalstring.rfid.viewmodel.BulkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkProductScreen(onBack: () -> Unit, navController: NavHostController) {
    val viewModel: BulkViewModel = hiltViewModel()

    // Observe barcode and tag data
    val tags by viewModel.scannedTags.collectAsState()
    val items by viewModel.scannedItems.collectAsState()

    // Dropdown options
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val designs by viewModel.designs.collectAsState()

    var selectedCategory by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf("") }
    var selectedDesign by remember { mutableStateOf("") }

    var showAddDialogFor by remember { mutableStateOf<String?>(null) }
    var firstPress by remember { mutableStateOf(false) }

    // ✅ Set barcode scan callback ONCE
    LaunchedEffect(Unit) {
        viewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            viewModel.onBarcodeScanned(scanned)
        }
    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Add Bulk Products",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            ScanBottomBar(
                onSave = { /* TODO */ },
                onList = { /* TODO */ },
                onScan = { /* TODO */ },
                onGscan = {
                    if (!firstPress) {
                        firstPress = true
                        viewModel.startScanning()
                        viewModel.startBarcodeScanning()
                    } else {
                        viewModel.stopScanning()
                        viewModel.startBarcodeScanning()
                    }
                },
                onReset = {
                    firstPress = false
                    viewModel.resetData()
                    viewModel.stopBarcodeScanner()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = "Category",
                    options = categories.map { it.name },
                    selectedOption = selectedCategory,
                    onOptionSelected = {
                        selectedCategory = it
                    },
                    onAddOption = { showAddDialogFor = "Category" }
                )

                FilterDropdown(
                    label = "Product",
                    options = products.map { it.name },
                    selectedOption = selectedProduct,
                    onOptionSelected = {
                        selectedProduct = it
                    },
                    onAddOption = { showAddDialogFor = "Product" }
                )

                FilterDropdown(
                    label = "Design",
                    options = designs.map { it.name },
                    selectedOption = selectedDesign,
                    onOptionSelected = { selectedDesign = it },
                    onAddOption = { showAddDialogFor = "Design" }
                )
            }

            if (showAddDialogFor != null) {
                AddItemDialog(
                    title = showAddDialogFor!!,
                    onAdd = { newItem ->
                        when (showAddDialogFor) {
                            "Category" -> {
                                selectedCategory = newItem
                                viewModel.saveDropdownCategory(newItem, "Category")
                            }

                            "Product" -> {
                                selectedProduct = newItem
                                viewModel.saveDropdownProduct(newItem, "Product")
                            }

                            "Design" -> {
                                selectedDesign = newItem
                                viewModel.saveDropdownDesign(newItem, "Design")
                            }
                        }
                        showAddDialogFor = null
                    },
                    onDismiss = { showAddDialogFor = null }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Sr No.", "Item Code", "RFID Code").forEach {
                    Text(it, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF0F0F0))
            ) {
                items(items) { item ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.id, Modifier.width(100.dp), color = Color.DarkGray)
                            Text(item.itemCode, Modifier.width(100.dp), color = Color.DarkGray)
                            Text(item.barcode, Modifier.width(100.dp), color = Color.DarkGray)
                        }
                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exist Items: ${items.size}", color = Color.White)
                Text("Total Items: ${items.size}", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onAddOption: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedOption.isEmpty()) "Select $label" else selectedOption,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown arrow"
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text(
                            "➕ Add New",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        expanded = false
                        onAddOption()
                    }
                )
            }
        }
    }
}