
package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.AddItemDialog
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.viewmodel.BulkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkProductScreen(onBack: () -> Unit, navController: NavHostController) {
    val viewModel: BulkViewModel = hiltViewModel()
    val context = LocalContext.current
    // Observe barcode and tag data
    val tags by viewModel.scannedTags.collectAsState()
    val items by viewModel.scannedItems.collectAsState()
    val rfidMap by viewModel.rfidMap.collectAsState()
    val itemCode = remember { mutableStateOf("") }
    // Dropdown options
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val designs by viewModel.designs.collectAsState()

    var selectedCategory by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf("") }
    var selectedDesign by remember { mutableStateOf("") }

    var showAddDialogFor by remember { mutableStateOf<String?>(null) }
    var firstPress by remember { mutableStateOf(false) }

    var clickedIndex by remember { mutableStateOf<Int?>(null) }

    // ✅ Set barcode scan callback ONCE
    LaunchedEffect(Unit) {
        viewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            viewModel.onBarcodeScanned(scanned)
            clickedIndex?.let { index ->
                viewModel.assignRfidCode(index, scanned)
            }

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
                onSave = {

                    if (selectedCategory.isNotBlank() && selectedProduct.isNotBlank() && selectedDesign.isNotBlank()) {
                        if (itemCode.value.isNotBlank()) {
                            viewModel.saveBulkItems(
                                selectedCategory,
                                itemCode.toString(), selectedProduct, selectedDesign,
                                clickedIndex?.let { tags.get(index = it) }!!
                            )
                        } else {
                            ToastUtils.showToast(context, "please enter item code")
                        }

                    } else {
                        ToastUtils.showToast(context, "Category/Product/Design cannot be Empty")
                    }

                },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan = { /* TODO */ },
                onGscan = {
                    if (!firstPress) {
                        firstPress = true
                        viewModel.startScanning()
                        //   viewModel.startBarcodeScanning()
                    } else {
                        viewModel.stopScanning()
                        //     viewModel.startBarcodeScanning()
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
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = "Category",
                    options = categories.map { it.name },
                    selectedOption = selectedCategory,
                    onOptionSelected = {
                        selectedCategory = it
                    },
                    onAddOption = { showAddDialogFor = "Category" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )

                FilterDropdown(
                    label = "Product",
                    options = products.map { it.name },
                    selectedOption = selectedProduct,
                    onOptionSelected = {
                        selectedProduct = it
                    },
                    onAddOption = { showAddDialogFor = "Product" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )

                FilterDropdown(
                    label = "Design",
                    options = designs.map { it.name },
                    selectedOption = selectedDesign,
                    onOptionSelected = { selectedDesign = it },
                    onAddOption = { showAddDialogFor = "Design" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
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
                itemsIndexed(tags) { index, item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {


                            }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "${index + 1}",
                                Modifier
                                    .width(100.dp)
                                    .background(Color.Transparent),
                                color = Color.DarkGray
                            )
                            TextField(
                                value = itemCode.value,
                                onValueChange = { itemCode.value = it },
                                modifier = Modifier.width(100.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = Color.DarkGray)
                            )
                            val rfid = rfidMap[index]
                            val isScanned = rfid != null
                            val displayText = rfid ?: "scan here"
                            val textColor = if (!isScanned) Color.Blue else Color.DarkGray
                            val style =
                                if (!isScanned) TextDecoration.Underline else TextDecoration.None

                            Text(
                                " $displayText",
                                Modifier
                                    .width(100.dp)
                                    .clickable {
                                        clickedIndex = index
                                        viewModel.startBarcodeScanning()
                                    }, color = textColor, textDecoration = style
                            )
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onAddOption: () -> Unit,
    modifier: Modifier

) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
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
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown arrow",
                        modifier = Modifier.padding(4.dp)
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
