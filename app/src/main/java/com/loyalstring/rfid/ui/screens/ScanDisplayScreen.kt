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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
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

    val allItems by productListViewModel.productList.collectAsState(initial = emptyList())

    val filteredItems = remember(allItems, filterTypeName, filterValue) {
        if (filterTypeName.isNullOrEmpty() || filterValue.isNullOrEmpty()) {
            allItems
        } else {
            when (filterTypeName.lowercase()) {
                "box" -> allItems.filter { it.boxName == filterValue }
                "counter" -> allItems.filter { it.counterName == filterValue }
                "branch" -> allItems.filter { it.branchName == filterValue }
                "exhibition" -> allItems.filter { it.branchName == filterValue }
                else -> allItems
            }
        }
    }

    val selectedCategories = remember { mutableStateListOf<String>() }
    val selectedProducts  = remember { mutableStateListOf<String>() }
    val selectedDesigns   = remember { mutableStateListOf<String>() }

// === 4) IMPORTANT: stable keys for state-list contents ===
    val selectedCategoriesKey = selectedCategories.toList()
    val selectedProductsKey   = selectedProducts.toList()
    val selectedDesignsKey    = selectedDesigns.toList()

    val allCategories = remember(filteredItems) {
        filteredItems.mapNotNull { it.category }.distinct().sorted()
    }

    val allProducts = remember(filteredItems, selectedCategoriesKey) {
        filteredItems
            .asSequence()
            .filter { selectedCategoriesKey.isEmpty() || it.category in selectedCategoriesKey }
            .mapNotNull { it.productName }
            .distinct()
            .sorted()
            .toList()
    }

    val allDesigns = remember(filteredItems, selectedCategoriesKey, selectedProductsKey) {
        filteredItems
            .asSequence()
            .filter { selectedCategoriesKey.isEmpty() || it.category in selectedCategoriesKey }
            .filter { selectedProductsKey.isEmpty() || it.productName in selectedProductsKey }
            .mapNotNull { it.design }
            .distinct()
            .sorted()
            .toList()
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

    var selectedPower by remember { mutableIntStateOf(10) }
    var isScanning by remember { mutableStateOf(false) }

    val scannedFiltered by bulkViewModel.scannedFilteredItems

    val scopeItems = remember(
        filteredItems,
        selectedCategoriesKey,
        selectedProductsKey,
        selectedDesignsKey,
        currentLevel, currentCategory, currentProduct, currentDesign,
        scannedFiltered
    ) {
        val baseList = filteredItems.filter { item ->
            (selectedCategoriesKey.isEmpty() || selectedCategoriesKey.contains(item.category.orEmpty())) &&
                    (selectedProductsKey.isEmpty()  || selectedProductsKey.contains(item.productName.orEmpty())) &&
                    (selectedDesignsKey.isEmpty()   || selectedDesignsKey.contains(item.design.orEmpty())) &&
                    when (currentLevel) {
                        "Category"     -> true
                        "Product"      -> item.category == currentCategory
                        "Design"       -> item.category == currentCategory && item.productName == currentProduct
                        "DesignItems"  -> item.category == currentCategory && item.productName == currentProduct && item.design == currentDesign
                        else           -> true
                    }
        }

        val scanMap = scannedFiltered.associateBy { it.rfid }
        baseList.map { original ->
            scanMap[original.rfid]?.let { scanned -> original.copy(scannedStatus = scanned.scannedStatus) } ?: original
        }
    }

    val displayItems = remember(scopeItems, selectedMenu) {
        when (selectedMenu) {
            MENU_MATCHED   -> scopeItems.filter { it.scannedStatus == "Matched" }
            MENU_UNMATCHED -> scopeItems.filter { it.scannedStatus == "Unmatched" }
            else           -> scopeItems
        }
    }
    val allMatched by remember(scopeItems) {
        derivedStateOf {
            scopeItems.isNotEmpty() && scopeItems.all { it.scannedStatus == "Matched" }
        }
    }
    LaunchedEffect(isScanning, allMatched) {
        if (isScanning && allMatched) {
            bulkViewModel.stopScanning()
            // Ensure final statuses are computed in case your VM does any post pass
            bulkViewModel.computeScanResults(scopeItems)
            isScanning = false
            Toast.makeText(context, "All items matched. Scan stopped.", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(isScanning, scopeItems.size) {
        if (isScanning && scopeItems.isEmpty()) {
            bulkViewModel.stopScanning()
            isScanning = false
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            bulkViewModel.stopScanning()
        }
    }


    LaunchedEffect(scopeItems) {
        if (isScanning && scopeItems.isNotEmpty() && scopeItems.all { it.scannedStatus == "Matched" }) {
            bulkViewModel.stopScanning()
            isScanning = false
        }
    }

    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            singleProductViewModel.fetchAllDropdownData(ClientCodeRequest(it))
        }
        bulkViewModel.getAllItems()
    }

    LaunchedEffect(scopeItems) {
        if (isScanning) {
            bulkViewModel.setFilteredItems(scopeItems)
        }
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
                            bulkViewModel.setFilteredItems(scopeItems)
                            bulkViewModel.startScanningInventory(selectedPower)
                        } else {
                            isScanning = false
                            bulkViewModel.stopScanning()
                            bulkViewModel.computeScanResults(scopeItems)
                        }
                    },
                    onReset = {
                        bulkViewModel.stopScanning()
                        isScanning = false
                        selectedCategories.clear()
                        selectedProducts.clear()
                        selectedDesigns.clear()
                        bulkViewModel.setFilteredItems(emptyList())
                        bulkViewModel.resetScanResults()
                        selectedMenu = MENU_ALL
                        currentLevel = "Category"
                        currentCategory = null
                        currentProduct = null
                        currentDesign = null
                    }
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
                    currentLevel = "Category" // ✅ Reset drill-down
                    currentCategory = null
                    currentProduct = null
                    currentDesign = null
                    showDialog = true
                },
                onProductClick = {
                    if (selectedCategories.isNotEmpty()) {
                        filterType = "Product"; showDialog = true
                    } else Toast.makeText(context, "Select category first", Toast.LENGTH_SHORT)
                        .show()
                },
                onDesignClick = {
                    if (selectedProducts.isNotEmpty()) {
                        filterType = "Design"; showDialog = true
                    } else Toast.makeText(context, "Select product first", Toast.LENGTH_SHORT)
                        .show()
                }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { TableHeader(currentLevel) }

                if (currentLevel != "DesignItems") {
                    val groupedRows = when (currentLevel) {
                        "Category" -> displayItems.groupBy { it.category ?: "Unknown" }
                        "Product" -> displayItems
                            .filter { it.category == currentCategory } // ✅ filter by selected category
                            .groupBy { it.productName ?: "Unknown" }

                        "Design" -> displayItems
                            .filter { it.category == currentCategory && it.productName == currentProduct } // ✅ filter by selected category + product
                            .groupBy { it.design ?: "Unknown" }

                        else -> emptyMap()
                    }

                    groupedRows.forEach { (label, items) ->
                        item {
                            TableDataRow(TableRow(label, items), currentLevel) {
                                when (currentLevel) {
                                    "Category" -> {
                                        currentCategory = label
                                        currentLevel = "Product"
                                        selectedProducts.clear()
                                        selectedDesigns.clear()
                                    }

                                    "Product" -> {
                                        currentProduct = label
                                        currentLevel = "Design"
                                        selectedDesigns.clear()
                                    }

                                    "Design" -> {
                                        currentDesign = label
                                        currentLevel = "DesignItems"
                                    }
                                }
                            }
                        }
                    }
                }

                if (currentLevel == "DesignItems") {
                    items(displayItems) { item ->
                        DesignItemRow(item) { clickedItem ->
                            selectedItem = clickedItem
                            showItemDialog = true
                        }
                    }
                }
            }
        }
    }

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
                if (filterType == "Category") {
                    selectedProducts.clear()
                    selectedDesigns.clear()
                } else if (filterType == "Product") {
                    selectedDesigns.clear()
                }
                bulkViewModel.setFilteredItems(scopeItems)
                bulkViewModel.resetScanResults()
                showDialog = false
            }
        )
    }

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
                        "Matched Items" -> selectedMenu = MENU_MATCHED
                        "UnMatched Items" -> selectedMenu = MENU_UNMATCHED
                        "Unlabelled Items" -> selectedMenu = MENU_ALL
                        "Search" -> {
                            val scanMap =
                                bulkViewModel.scannedFilteredItems.value.associateBy { it.rfid }
                            val latestUnmatched = scopeItems
                                .map { original -> scanMap[original.rfid] ?: original }
                                .filter { it.scannedStatus == "Unmatched" }

                            navController.currentBackStackEntry?.savedStateHandle
                                ?.set("unmatchedItems", latestUnmatched)
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

@Composable
fun SummaryRow(currentLevel: String, items: List<BulkItem>, selectedMenu: String) {
    val totalQty = items.size
    val totalGwtBD = items.fold(BigDecimal.ZERO) { acc, it ->
        acc + parseWeightToBigDecimal(it.grossWeight)
    }
    val matchedItems = items.filter { it.scannedStatus == "Matched" }
    val totalMatchedQty = matchedItems.size
    val totalMatchedWtBD = matchedItems.fold(BigDecimal.ZERO) { acc, it ->
        acc + parseWeightToBigDecimal(it.grossWeight)
    }

    val unmatchedItems = items.filter { it.scannedStatus == "Unmatched" }
    val unmatchedQty = unmatchedItems.size
    val unmatchedWtBD = unmatchedItems.fold(BigDecimal.ZERO) { acc, it ->
        acc + parseWeightToBigDecimal(it.grossWeight)
    }

    if (selectedMenu == MENU_UNMATCHED) {
        // 🔴 Show only unmatched totals
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF3B363E))
                .padding(vertical = 4.dp)
        ) {
            TableHeaderCell("Total", colCategoryWidth)
            TableHeaderCell("$unmatchedQty", colQtyWidth)
            TableHeaderCell(formatMatchedUpTo3(unmatchedWtBD), colWeightWidth)
            TableHeaderCell("", colMatchedQtyWidth)
            TableHeaderCell("", colMatchedWtWidth)
            TableHeaderCell("", colStatusWidth)
        }
    } else {
        // 🟢 Normal totals
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF3B363E))
                .padding(vertical = 4.dp)
        ) {
            TableHeaderCell("Total", colCategoryWidth)
            TableHeaderCell(if (currentLevel == "DesignItems") "" else "$totalQty", colQtyWidth)
            TableHeaderCell(
                if (currentLevel == "DesignItems") "" else formatMatchedUpTo3(totalGwtBD),
                colWeightWidth
            )
            TableHeaderCell("$totalMatchedQty", colMatchedQtyWidth)
            TableHeaderCell(formatMatchedUpTo3(totalMatchedWtBD), colMatchedWtWidth)
            TableHeaderCell("", colStatusWidth)
        }
    }
}


// ... (rest of your helpers: TableHeader, TableDataRow, TableCell, StatusIconCell, VerticalMenu, FilterSelectionDialog, MenuCard, parseWeightToBigDecimal, formatMatchedUpTo3, etc.)

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

/* Reworked TableHeader: shows different headers when in DesignItems level */
@Composable
fun TableHeader(currentLevel: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF3B363E))
            .padding(vertical = 2.dp)
    ) {
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
            .clickable { onRowClick() }
    ) {
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
        menuItems.forEach { item ->
            MenuCard(item = item, onClick = { onMenuClick(item) })
        }
    }
}

@Composable
fun FilterSelectionDialog(
    title: String,
    items: List<String>,
    selectedItems: SnapshotStateList<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = "Select $title",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = poppins
        )
    }, text = {
        Column {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedItems.contains(item)) selectedItems.remove(item) else selectedItems.add(
                                    item
                                )
                            }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedItems.contains(item),
                            onCheckedChange = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontFamily = poppins,
                            maxLines = 1
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
                GradientButton(text = "Cancel", onClick = onDismiss)
                Spacer(modifier = Modifier.width(12.dp))
                GradientButton(text = "OK", onClick = onConfirm)
            }
        }
    }, confirmButton = {}, dismissButton = {})
}

data class MenuItem(val title: String, val iconRes: Int)

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

// parse string weights like "1,234.56", "  12.345 ", or maybe "12g" safely to BigDecimal
fun parseWeightToBigDecimal(weight: String?): BigDecimal {
    if (weight.isNullOrBlank()) return BigDecimal.ZERO
    return try {
        // Remove any non-numeric (except . and -)
        val cleaned = weight.replace(Regex("[^0-9.]"), "")
        if (cleaned.isBlank()) BigDecimal.ZERO else BigDecimal(cleaned)
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}

// format total G.Wt (2 decimals)
fun formatTotalGwt(b: BigDecimal): String =
    b.setScale(2, RoundingMode.HALF_UP).toPlainString()

// format matched weight up to 3 decimals, DO NOT ROUND UP (truncate)
fun formatMatchedUpTo3(b: BigDecimal): String {
    val truncated = b.setScale(3, RoundingMode.DOWN)
    return truncated.stripTrailingZeros().toPlainString()
}


data class TableRow(val label: String, val items: List<BulkItem>)
