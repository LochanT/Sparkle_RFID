// ScanDisplayScreen.kt
package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.MainActivity
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.reader.ScanKeyListener
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.GradientButton
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.ProductListViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import java.math.BigDecimal
import java.math.RoundingMode

// column widths
val colCategoryWidth = 72.dp
val colQtyWidth = 44.dp
val colWeightWidth = 62.dp
val colMatchedQtyWidth = 44.dp
val colMatchedWtWidth = 62.dp
val colStatusWidth = 54.dp
val colDesignNameWidth = 80.dp
val colRfidWidth = 100.dp
val colItemCodeWidth = 90.dp
val colGWtWidth = 50.dp
val colStatusIconWidth = 54.dp

private const val MENU_ALL = "ALL"
private const val MENU_MATCHED = "MATCHED"
private const val MENU_UNMATCHED = "UNMATCHED"
private const val MENU_SEARCH = "SEARCH"

@SuppressLint("UnrememberedMutableState")
@Composable
fun ScanDisplayScreen(onBack: () -> Unit, navController: NavHostController) {
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    val productListViewModel: ProductListViewModel = hiltViewModel()
    val bulkViewModel: BulkViewModel = hiltViewModel()
    val context: Context = LocalContext.current

    val filterTypeName = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("filterType")
    val filterValue = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("filterValue")

    LaunchedEffect(filterTypeName, filterValue) {
        if (!filterTypeName.isNullOrEmpty() && !filterValue.isNullOrEmpty()) {
            bulkViewModel.setFilteredItemsByType(filterTypeName, filterValue)
        }
    }

    var showRfidDialog by remember { mutableStateOf(false) }
    if (showRfidDialog) {
        AlertDialog(
            onDismissRequest = {
                showRfidDialog = false
                navController.popBackStack()
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GradientButton(
                        text = "OK",
                        onClick = {
                            showRfidDialog = false
                            navController.popBackStack()
                        }
                    )
                }
            },
            title = { Text("Missing Data", fontFamily = poppins, fontSize = 18.sp) },
            text = {
                Text(
                    "RFID sheet not uploaded. Please contact administrator.",
                    fontFamily = poppins,
                    fontSize = 14.sp
                )
            }
        )
    }

    val allItems by productListViewModel.productList.collectAsState(initial = emptyList())

    val filteredItems = remember(allItems, filterTypeName, filterValue) {
        if (filterTypeName.isNullOrEmpty() || filterValue.isNullOrEmpty()) {
            allItems
        } else {
            when (filterTypeName.lowercase()) {
                "box" -> allItems.filter { it.boxName == filterValue }
                "counter" -> allItems.filter { it.counterName == filterValue }
                "branch" -> allItems.filter { it.branchName == filterValue }
                "exhibition" -> allItems.filter { it.branchType == filterTypeName && it.branchName == filterValue }
                else -> allItems
            }
        }
    }

    // Multi-select filters
    val selectedCategories = remember { mutableStateListOf<String>() }
    val selectedProducts = remember { mutableStateListOf<String>() }
    val selectedDesigns = remember { mutableStateListOf<String>() }

    // stable snapshot keys
    val selectedCategoriesKey = selectedCategories.toList()
    val selectedProductsKey = selectedProducts.toList()
    val selectedDesignsKey = selectedDesigns.toList()

    // compute available options based on filters
    val allCategories = remember(filteredItems) {
        filteredItems.mapNotNull { it.category }.distinct().sorted()
    }

    val allProducts = remember(filteredItems, selectedCategoriesKey) {
        filteredItems
            .filter { selectedCategoriesKey.isEmpty() || it.category in selectedCategoriesKey }
            .mapNotNull { it.productName }
            .distinct()
            .sorted()
    }

    val allDesigns = remember(filteredItems, selectedCategoriesKey, selectedProductsKey) {
        filteredItems
            .filter { selectedCategoriesKey.isEmpty() || it.category in selectedCategoriesKey }
            .filter { selectedProductsKey.isEmpty() || it.productName in selectedProductsKey }
            .mapNotNull { it.design }
            .distinct()
            .sorted()
    }

    var currentLevel by rememberSaveable { mutableStateOf("Category") }
    var currentCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var currentProduct by rememberSaveable { mutableStateOf<String?>(null) }
    var currentDesign by rememberSaveable { mutableStateOf<String?>(null) }

    var showMenu by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("Category") }
    var selectedMenu by remember { mutableStateOf(MENU_ALL) }

    var selectedItem by remember { mutableStateOf<BulkItem?>(null) }
    var showItemDialog by remember { mutableStateOf(false) }

    var selectedPower by remember { mutableIntStateOf(30) }
    var isScanning by remember { mutableStateOf(false) }

    val scannedFiltered by bulkViewModel.scannedFilteredItems

    // scopeItems overlay scanned status on filtered base set
    val scopeItems by remember(
        filteredItems,
        selectedCategoriesKey,
        selectedProductsKey,
        selectedDesignsKey,
        scannedFiltered
    ) {
        derivedStateOf {
            var baseList = filteredItems
            baseList = baseList.filter { item ->
                (selectedCategoriesKey.isEmpty() || item.category in selectedCategoriesKey) &&
                        (selectedProductsKey.isEmpty() || item.productName in selectedProductsKey) &&
                        (selectedDesignsKey.isEmpty() || item.design in selectedDesignsKey)
            }

            val scanMap = scannedFiltered.associateBy { it.epc?.trim()?.uppercase() }
            baseList.map { original ->
                val key = original.epc?.trim()?.uppercase()
                scanMap[key]?.let { scanned -> original.copy(scannedStatus = scanned.scannedStatus) }
                    ?: original.copy(scannedStatus = "Unmatched")
            }
        }
    }

    // displayItems respects selectedMenu and sticky unmatched ids (existing logic preserved)
    val displayItems = remember(scopeItems, selectedMenu, bulkViewModel.stickyUnmatchedIds) {
        when (selectedMenu) {
            MENU_MATCHED -> scopeItems.filter { it.scannedStatus == "Matched" }
            MENU_UNMATCHED -> {
                val unmatchedNow = scopeItems.filter { it.scannedStatus == "Unmatched" }
                val sticky = scopeItems.filter {
                    val id = it.epc?.trim()?.uppercase()
                    id != null && bulkViewModel.stickyUnmatchedIds.contains(id)
                }
                (unmatchedNow + sticky).distinctBy { it.epc }
            }

            else -> scopeItems
        }
    }

    val allMatched by remember(scopeItems) {
        derivedStateOf { scopeItems.isNotEmpty() && scopeItems.all { it.scannedStatus == "Matched" } }
    }

    val activity = LocalContext.current as MainActivity

    DisposableEffect(Unit) {
        val listener = object : ScanKeyListener {
            override fun onBarcodeKeyPressed() {
                bulkViewModel.startBarcodeScanning(context)
            }

            override fun onRfidKeyPressed() {
                if (!isScanning) {
                    isScanning = true
                    bulkViewModel.setFilteredItems(scopeItems)
                    bulkViewModel.startScanningInventory(selectedPower)
                } else {
                    isScanning = false
                    bulkViewModel.stopScanningAndCompute()
                }
            }
        }
        activity.registerScanKeyListener(listener)
        onDispose { activity.unregisterScanKeyListener() }
    }

    LaunchedEffect(isScanning, allMatched) {
        if (isScanning && allMatched) {
            bulkViewModel.stopScanningAndCompute()
            isScanning = false
            Toast.makeText(context, "All items matched. Scan stopped.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isScanning, scopeItems.size) {
        if (isScanning && scopeItems.isEmpty()) {
            bulkViewModel.stopScanningAndCompute()
            isScanning = false
        }
    }

    DisposableEffect(Unit) { onDispose { bulkViewModel.stopScanningAndCompute() } }

    LaunchedEffect(scopeItems) {
        if (isScanning && scopeItems.isNotEmpty() && scopeItems.all { it.scannedStatus == "Matched" }) {
            bulkViewModel.stopScanningAndCompute()
            isScanning = false
        }
    }

    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    LaunchedEffect(Unit) {
        employee?.clientCode?.let { singleProductViewModel.fetchAllDropdownData(ClientCodeRequest(it)) }
        bulkViewModel.getAllItems()
    }

    LaunchedEffect(scopeItems) {
        if (isScanning) bulkViewModel.setFilteredItems(scopeItems)
    }

    Scaffold(
        topBar = {
            filterValue?.let {
                GradientTopBar(
                    title = it,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    showCounter = true,
                    selectedCount = selectedPower,
                    onCountSelected = { selectedPower = it }
                )
            }
        },
        bottomBar = {
            Column {
                SummaryRow(currentLevel, displayItems, selectedMenu)
                ScanBottomBar(
                    onSave = { /* save */ },
                    onList = { showMenu = true },
                    onScan = {},
                    onGscan = {
                        if (!isScanning) {
                            isScanning = true
                            // bulkViewModel.resetScanResults()
                            bulkViewModel.setFilteredItems(scopeItems)   // ✅ only current scope
                            bulkViewModel.startScanningInventory(selectedPower)
                        } else {
                            isScanning = false
                            bulkViewModel.stopScanningAndCompute()
                        }
                    },
                    onReset = {
                        bulkViewModel.stopScanningAndCompute()
                        isScanning = false

                        selectedCategories.clear()
                        selectedProducts.clear()
                        selectedDesigns.clear()

                        bulkViewModel.setFilteredItems(allItems) // ✅ reset to full DB
                        bulkViewModel.resetScanResults()

                        selectedMenu = MENU_ALL
                        currentLevel = "Category"
                        currentCategory = null
                        currentProduct = null
                        currentDesign = null
                    },
                    isScanning = isScanning

                )
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            FilterRow(
                selectedCategories,
                selectedProducts,
                selectedDesigns,
                onCategoryClick = {
                    filterType = "Category"
                    // entering filter dialog resets drill-down navigation
                    currentLevel = "Category"
                    currentCategory = null
                    currentProduct = null
                    currentDesign = null
                    showDialog = true
                },
                onProductClick = {
                    if (selectedCategories.isNotEmpty() || allCategories.isNotEmpty()) {
                        filterType = "Product"
                        // entering filter dialog resets drill-down navigation
                        currentLevel = "Category"
                        currentCategory = null
                        currentProduct = null
                        currentDesign = null
                        showDialog = true
                    } else Toast.makeText(context, "Select category first", Toast.LENGTH_SHORT)
                        .show()
                },
                onDesignClick = {
                    if (selectedProducts.isNotEmpty() || allProducts.isNotEmpty()) {
                        filterType = "Design"
                        currentLevel = "Category"
                        currentCategory = null
                        currentProduct = null
                        currentDesign = null
                        showDialog = true
                    } else Toast.makeText(context, "Select product first", Toast.LENGTH_SHORT)
                        .show()
                }
            )

            TableHeader(currentLevel)

            // ---------- L A Z Y   C O L U M N   (drill-down + multi-select friendly) ----------
            LazyColumn(modifier = Modifier.weight(1f)) {
                when (currentLevel) {
                    "Category" -> {
                        val grouped = displayItems.groupBy { it.category ?: "Unknown" }
                        grouped.forEach { (label, items) ->
                            item {
                                TableDataRow(TableRow(label, items), currentLevel) {
                                    // drill down to product for the chosen category (single-select drill)
                                    currentCategory = label
                                    selectedCategories.clear()
                                    selectedCategories.add(label)
                                    selectedProducts.clear()
                                    selectedDesigns.clear()
                                    currentLevel = "Product"
                                }
                            }
                        }
                    }

                    "Product" -> {
                        val grouped = displayItems
                            .filter { selectedCategories.isEmpty() || it.category in selectedCategories }
                            .groupBy { it.productName ?: "Unknown" }

                        grouped.forEach { (label, items) ->
                            item {
                                TableDataRow(TableRow(label, items), currentLevel) {
                                    // when clicking product row, drill to design level
                                    currentProduct = label
                                    if (!selectedProducts.contains(label)) selectedProducts.add(
                                        label
                                    )
                                    selectedDesigns.clear()
                                    currentLevel = "Design"
                                }
                            }
                        }
                    }

                    "Design" -> {
                        val grouped = displayItems
                            .filter {
                                (selectedCategories.isEmpty() || it.category in selectedCategories) &&
                                        (selectedProducts.isEmpty() || it.productName in selectedProducts)
                            }
                            .groupBy { it.design ?: "Unknown" }

                        grouped.forEach { (label, items) ->
                            item {
                                TableDataRow(TableRow(label, items), currentLevel) {
                                    currentDesign = label
                                    if (!selectedDesigns.contains(label)) selectedDesigns.add(label)
                                    currentLevel = "DesignItems"
                                }
                            }
                        }
                    }

                    "DesignItems" -> {
                        val itemsList = displayItems.filter {
                            (selectedCategories.isEmpty() || it.category in selectedCategories) &&
                                    (selectedProducts.isEmpty() || it.productName in selectedProducts) &&
                                    (selectedDesigns.isEmpty() || it.design in selectedDesigns)
                        }
                        items(itemsList) { item ->
                            DesignItemRow(item) { clickedItem ->
                                selectedItem = clickedItem
                                showItemDialog = true
                            }
                        }
                    }
                }
            }
            // -----------------------------------------------------------------------------------
        }
    }

    // ---------- Filter Dialog ----------
    if (showDialog) {
        val items = when (filterType) {
            "Category" -> allCategories
            "Product" -> allProducts
            "Design" -> allDesigns
            else -> emptyList()
        }
        val selected = when (filterType) {
            "Category" -> selectedCategories
            "Product" -> selectedProducts
            "Design" -> selectedDesigns
            else -> mutableStateListOf()
        }

        FilterSelectionDialog(
            title = filterType,
            items = items,
            selectedItems = selected,
            onDismiss = { showDialog = false },
            onConfirm = {
                // After confirming selection, always move to the next level so children are visible
                when (filterType) {
                    "Category" -> {
                        // keep multiple categories if chosen; show their products
                        currentLevel = "Product"
                        currentCategory = null
                        // clear dependent lower-level selections
                        selectedProducts.clear()
                        selectedDesigns.clear()
                    }

                    "Product" -> {
                        // keep multiple products; show their designs
                        currentLevel = "Design"
                        currentProduct = null
                        selectedDesigns.clear()
                    }
                    "Design" -> {
                        // show all design items for selected designs
                        currentLevel = "DesignItems"
                        currentDesign = null
                    }
                }
                bulkViewModel.setFilteredItems(scopeItems)
                bulkViewModel.resetScanResults()
                showDialog = false
            }
        )
    }

    // Menu (Matched/Unmatched/etc.)
    if (showMenu) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { showMenu = false }
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 60.dp, bottom = 70.dp)
                    .width(180.dp)
                    .fillMaxHeight()
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                VerticalMenu { menuItem ->
                    when (menuItem.title) {
                        "UnMatched Items" -> {
                            selectedMenu = MENU_UNMATCHED
                            bulkViewModel.rememberUnmatched(scopeItems.filter { it.scannedStatus == "Unmatched" })
                        }

                        "Matched Items" -> {
                            selectedMenu = MENU_MATCHED
                            bulkViewModel.clearStickyUnmatched()
                        }

                        "Unlabelled Items" -> {
                            selectedMenu = MENU_ALL
                            bulkViewModel.clearStickyUnmatched()
                        }
                        "Search" -> {
                            val scanMap =
                                bulkViewModel.scannedFilteredItems.value.associateBy { it.rfid }
                            val latestUnmatched =
                                scopeItems.map { original -> scanMap[original.rfid] ?: original }
                                .filter { it.scannedStatus == "Unmatched" }
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "unmatchedItems",
                                latestUnmatched
                            )
                            navController.navigate(Screens.SearchScreen.route)
                        }
                    }
                    showMenu = false
                }
            }
        }
    }

    if (showItemDialog && selectedItem != null) {
        ItemDetailsDialog(item = selectedItem!!, onDismiss = { showItemDialog = false })
    }
}

@Composable
fun FilterRow(
    selectedCategories: List<String>,
    selectedProducts: List<String>,
    selectedDesigns: List<String>,
    onCategoryClick: () -> Unit,
    onProductClick: () -> Unit,
    onDesignClick: () -> Unit
) {
    val catLabel = selectedCategories.joinToString(", ").ifBlank { "Category" }
    val prodLabel = selectedProducts.joinToString(", ").ifBlank { "Product" }
    val designLabel = selectedDesigns.joinToString(", ").ifBlank { "Design" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onCategoryClick,
            modifier = Modifier
                .width(100.dp)
                .height(40.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(Color(0xFF3053F0), Color(0xFFE82E5A)))
            )
        ) {
            Text(
                catLabel,
                color = Color.DarkGray,
                fontFamily = poppins,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        OutlinedButton(
            onClick = onProductClick,
            modifier = Modifier
                .width(100.dp)
                .height(40.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(Color(0xFF3053F0), Color(0xFFE82E5A)))
            )
        ) {
            Text(
                prodLabel,
                color = Color.DarkGray,
                fontFamily = poppins,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        OutlinedButton(
            onClick = onDesignClick,
            modifier = Modifier
                .width(100.dp)
                .height(40.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(Color(0xFF3053F0), Color(0xFFE82E5A)))
            )
        ) {
            Text(
                designLabel,
                color = Color.DarkGray,
                fontFamily = poppins,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DesignItemRow(item: BulkItem, onClick: (BulkItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp)
            .clickable { onClick(item) },
        horizontalArrangement = Arrangement.Start
    ) {
        TableCell(item.design ?: "-", colDesignNameWidth)
        TableCell(item.rfid ?: "-", colRfidWidth)
        TableCell(item.itemCode ?: "-", colItemCodeWidth)
        TableCell(item.grossWeight ?: "-", colGWtWidth)
        StatusIconCell(item.scannedStatus, colStatusIconWidth)
    }
}


/* -----------------------
   Filter selection dialog
   ----------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSelectionDialog(
    title: String,
    items: List<String>,
    selectedItems: SnapshotStateList<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select $title",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = poppins
            )
        },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedItems.contains(item)) selectedItems.remove(item)
                                    else selectedItems.add(item)
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                Checkbox(
                                    checked = selectedItems.contains(item),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedItems.add(item) else selectedItems.remove(
                                            item
                                        )
                                    },
                                    modifier = Modifier.size(20.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF3053F0),
                                        uncheckedColor = Color.Gray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                fontFamily = poppins,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GradientButton(text = "CANCEL", onClick = onDismiss)
                    Spacer(modifier = Modifier.width(12.dp))
                    GradientButton(text = "OK", onClick = onConfirm)
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

/* -----------------------
   Summary row (simplified)
   ----------------------- */
@Composable
fun SummaryRow(currentLevel: String, items: List<BulkItem>, selectedMenu: String) {
    val totalQty = items.size
    val totalGwtBD =
        items.fold(BigDecimal.ZERO) { acc, it -> acc + parseWeightToBigDecimal(it.grossWeight) }

    val matchedItems = items.filter { it.scannedStatus == "Matched" }
    val totalMatchedQty = matchedItems.size
    val totalMatchedWtBD =
        matchedItems.fold(BigDecimal.ZERO) { acc, it -> acc + parseWeightToBigDecimal(it.grossWeight) }

    val unmatchedItems = items.filter { it.scannedStatus == "Unmatched" }
    val unmatchedQty = unmatchedItems.size
    val unmatchedWtBD =
        unmatchedItems.fold(BigDecimal.ZERO) { acc, it -> acc + parseWeightToBigDecimal(it.grossWeight) }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF3B363E))
            .padding(vertical = 4.dp)
    ) {
        when {
            selectedMenu == MENU_UNMATCHED -> {
                TableHeaderCell("Total", colCategoryWidth)
                TableHeaderCell("$unmatchedQty", colQtyWidth)
                TableHeaderCell(formatMatchedUpTo3(unmatchedWtBD), colWeightWidth)
                TableHeaderCell("", colMatchedQtyWidth)
                TableHeaderCell("", colMatchedWtWidth)
                TableHeaderCell("", colStatusWidth)
            }

            currentLevel == "DesignItems" -> {
                TableHeaderCell("Total", colDesignNameWidth)
                TableHeaderCell("$totalQty", colRfidWidth)
                TableHeaderCell("$totalMatchedQty", colItemCodeWidth)
                TableHeaderCell(formatMatchedUpTo3(totalMatchedWtBD), colGWtWidth)
                TableHeaderCell("", colStatusIconWidth)
            }

            else -> {
                TableHeaderCell("Total", colCategoryWidth)
                TableHeaderCell("$totalQty", colQtyWidth)
                TableHeaderCell(formatMatchedUpTo3(totalGwtBD), colWeightWidth)
                TableHeaderCell("$totalMatchedQty", colMatchedQtyWidth)
                TableHeaderCell(formatMatchedUpTo3(totalMatchedWtBD), colMatchedWtWidth)
                TableHeaderCell("", colStatusWidth)
            }
        }
    }
}

/* -----------------------
   Remaining helpers (unchanged)
   ----------------------- */
// TableHeaderCell, TableHeader, TableDataRow, TableCell, StatusIconCell, VerticalMenu, MenuCard,
// parseWeightToBigDecimal, formatMatchedUpTo3, TableRow data class
// Paste your existing implementations for these below or keep the ones already in your file.

@Composable
fun TableHeaderCell(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

/* Reused simple TableHeader */
@Composable
fun TableHeader(currentLevel: String) {
    Row(Modifier
        .fillMaxWidth()
        .background(Color(0xFF3B363E))
        .padding(vertical = 2.dp)) {
        if (currentLevel == "DesignItems") {
            TableHeaderCell("Design", colDesignNameWidth)
            TableHeaderCell("RFID No", colRfidWidth)
            TableHeaderCell("Item Code", colItemCodeWidth)
            TableHeaderCell("G.Wt", colGWtWidth)
            TableHeaderCell("Status", colStatusIconWidth)
        } else {
            TableHeaderCell(currentLevel, colCategoryWidth)
            TableHeaderCell("Qty", colQtyWidth)
            TableHeaderCell("G.Wt", colWeightWidth)
            TableHeaderCell("M.Qty", colMatchedQtyWidth)
            TableHeaderCell("M.Wt", colMatchedWtWidth)
            TableHeaderCell("Status", colStatusWidth)
        }
    }
}

@Composable
fun TableDataRow(row: TableRow, currentLevel: String, onRowClick: () -> Unit) {
    val qty = row.items.size
    val matchedItems = row.items.filter { it.scannedStatus == "Matched" }
    val matchedQty = matchedItems.size
    val grossWeight = row.items.sumOf { it.grossWeight?.toDoubleOrNull() ?: 0.0 }
    val matchedWeight = matchedItems.sumOf { it.netWeight?.toDoubleOrNull() ?: 0.0 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 4.dp)
            .clickable { onRowClick() }) {
        TableCell(row.label, colCategoryWidth)
        TableCell("$qty", colQtyWidth)
        TableCell("%.3f".format(grossWeight), colWeightWidth)
        TableCell("$matchedQty", colMatchedQtyWidth)
        TableCell("%.2f".format(matchedWeight), colMatchedWtWidth)
        val status = row.items.firstOrNull()?.scannedStatus ?: "Unmatched"
        StatusIconCell(status, colStatusWidth)
    }
}

@Composable
fun TableCell(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, color = Color.DarkGray, fontFamily = poppins)
    }
}

@Composable
fun StatusIconCell(status: String?, width: Dp) {
    val iconRes = when (status) {
        "Matched" -> R.drawable.ic_matched
        "Unmatched" -> R.drawable.ic_unmatched
        else -> R.drawable.ic_unmatched
    }
    Box(modifier = Modifier
        .width(width)
        .padding(2.dp), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun VerticalMenu(onMenuClick: (MenuItem) -> Unit) {
    val menuItems = listOf(
        MenuItem("Matched Items", R.drawable.ic_list_matched),
        MenuItem("UnMatched Items", R.drawable.ic_list_unmatched),
        MenuItem("Unlabelled Items", R.drawable.ic_list_unlabelled),
        MenuItem("Resume Scan", R.drawable.ic_resume_scan),
        MenuItem("Search", R.drawable.search_gr_svg)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        menuItems.forEach { item -> MenuCard(item = item, onClick = { onMenuClick(item) }) }
    }
}

@Composable
fun MenuCard(item: MenuItem, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF3053F0), Color(0xFFE82E5A)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                fontFamily = poppins
            )
        }
    }
}

fun parseWeightToBigDecimal(weight: String?): BigDecimal {
    if (weight.isNullOrBlank()) return BigDecimal.ZERO
    return try {
        val cleaned = weight.replace(Regex("[^0-9.]"), "")
        if (cleaned.isBlank()) BigDecimal.ZERO else BigDecimal(cleaned)
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}

fun formatMatchedUpTo3(b: BigDecimal): String {
    val truncated = b.setScale(3, RoundingMode.DOWN)
    return truncated.stripTrailingZeros().toPlainString()
}

data class MenuItem(val title: String, val iconRes: Int)
data class TableRow(val label: String, val items: List<BulkItem>)
