package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.loyalstring.rfid.data.local.entity.SearchItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.*
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.*
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.ProductListViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel

// Fixed widths for each column
val colCategoryWidth = 72.dp
val colQtyWidth = 44.dp
val colWeightWidth = 62.dp
val colMatchedQtyWidth = 44.dp
val colMatchedWtWidth = 62.dp
val colStatusWidth = 54.dp // Previously too small for "Status" text

@SuppressLint("UnrememberedMutableState")
@Composable
fun ScanDisplayScreen(onBack: () -> Unit, navController: NavHostController) {
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    val productListViewModel: ProductListViewModel = hiltViewModel()
    val bulkViewModel: BulkViewModel = hiltViewModel()
    val context: Context = LocalContext.current

    val categoryResponse = singleProductViewModel.categoryResponse.observeAsState().value
    val productResponse = singleProductViewModel.productResponse.observeAsState().value
    val designResponse = singleProductViewModel.designResponse.observeAsState().value

    val allCategories =
        (categoryResponse as? Resource.Success<List<CategoryModel>>)?.data ?: emptyList()
    val allProducts =
        (productResponse as? Resource.Success<List<ProductModel>>)?.data ?: emptyList()
    val allDesigns = (designResponse as? Resource.Success<List<DesignModel>>)?.data ?: emptyList()
    val allItems by productListViewModel.productList.collectAsState(initial = emptyList())

    val selectedCategories = remember { mutableStateListOf<String>() }
    val selectedProducts = remember { mutableStateListOf<String>() }
    val selectedDesigns = remember { mutableStateListOf<String>() }

    var currentLevel by rememberSaveable { mutableStateOf("Category") }
    var currentCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var currentProduct by rememberSaveable { mutableStateOf<String?>(null) }

    var showMenu by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("Category") }

    var showSearchScreen by remember { mutableStateOf(false) }

    var selectedMenu by remember { mutableStateOf("All") }


    //   val scannedFilteredItems by bulkViewModel.scannedFilteredItems

    var selectedPower by remember { mutableIntStateOf(10) }
    var firstPress by remember { mutableStateOf(false) }

    val filteredItems by remember(allItems, selectedCategories, selectedProducts, selectedDesigns) {
        derivedStateOf {
            allItems.filter {
                (selectedCategories.isEmpty() || selectedCategories.contains(it.category.orEmpty())) &&
                        (selectedProducts.isEmpty() || selectedProducts.contains(it.productName.orEmpty())) &&
                        (selectedDesigns.isEmpty() || selectedDesigns.contains(it.design.orEmpty()))
            }
        }
    }

// Backing list shown in the UI
    val scannedFilteredItems = bulkViewModel.scannedFilteredItems.value
    val displayItems = when (selectedMenu) {
        "Matched" -> bulkViewModel.scannedFilteredItems.value.filter { it.scannedStatus == "Matched" }
        "Unmatched" -> bulkViewModel.scannedFilteredItems.value.filter { it.scannedStatus == "Unmatched" }
        else -> if (bulkViewModel.scannedFilteredItems.value.isNotEmpty()) {
            bulkViewModel.scannedFilteredItems.value
        } else filteredItems
    }


    LaunchedEffect(currentLevel, currentCategory, currentProduct) {
        println("🧩 Level: $currentLevel, Category: $currentCategory, Product: $currentProduct")
    }

    val tableRows by remember(displayItems, currentLevel, currentCategory, currentProduct) {
        derivedStateOf {
            when (currentLevel) {
                "Category" -> {
                    displayItems.groupBy { it.category ?: "Unknown" }
                        .map { (category, items) -> TableRow(category, items) }
                }

                "Product" -> {
                    displayItems.filter { it.category == currentCategory }
                        .groupBy { it.productName ?: "Unknown" }
                        .map { (product, items) -> TableRow(product, items) }
                }

                "Design" -> {
                    displayItems.filter {
                        it.category == currentCategory && it.productName == currentProduct
                    }
                        .groupBy { it.design ?: "Unknown" }
                        .map { (design, items) -> TableRow(design, items) }
                }

                else -> emptyList()
            }
        }
    }


    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            singleProductViewModel.fetchAllDropdownData(ClientCodeRequest(it))
        }
    }

    Box {
        Scaffold(
            topBar = {
                GradientTopBar(
                    title = "Inventory",
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
                    onCountSelected = {
                        selectedPower = it
                    }
                )
            },
            bottomBar = {
                Column {
                    SummaryRow(tableRows)
                    ScanBottomBar(
                        onSave = { /* Save */ },
                        onList = { showMenu = true },
                        onScan = { },
                        onGscan = {
                            if (!firstPress) {
                                firstPress = true
                                if (selectedCategories.isNotEmpty()) {
                                    bulkViewModel.startScanning(selectedPower)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please select a Category first",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                firstPress = false
                                bulkViewModel.onScanStopped()
                                bulkViewModel.computeScanResults(allItems = filteredItems)

                            }


                        },
                        onReset = {
                            firstPress = false
                            selectedCategories.clear()
                            selectedProducts.clear()
                            selectedDesigns.clear()
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                FilterRow(
                    onCategoryClick = {
                        filterType = "Category"
                        showDialog = true
                    },
                    onProductClick = {
                        filterType = "Product"
                        showDialog = true
                    },
                    onDesignClick = {
                        filterType = "Design"
                        showDialog = true
                    }
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        TableHeader(currentLevel)
                    }
                    items(tableRows) { row ->
                        TableDataRow(row, currentLevel) {
                            when (currentLevel) {
                                "Category" -> {
                                    currentCategory = row.label
                                    currentLevel = "Product"
                                }

                                "Product" -> {
                                    currentProduct = row.label
                                    currentLevel = "Design"
                                }
                            }
                        }
                    }
                }

            }
        }

        if (showDialog) {
            val items = when (filterType) {
                "Category" -> allCategories.map { it.CategoryName }
                "Product" -> allProducts.map { it.ProductName }
                "Design" -> allDesigns.map { it.DesignName }
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
                    // Clear dependent selections
                    if (filterType == "Category") {
                        selectedProducts.clear()
                        selectedDesigns.clear()
                    } else if (filterType == "Product") {
                        selectedDesigns.clear()
                    }
                    showDialog = false
                }
            )
        }

        // Vertical Drawer
        if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable { showMenu = false }
            )

            Surface(
                modifier = Modifier
                    .padding(
                        top = 60.dp,
                        bottom = 70.dp
                    ) // adjust to your topBar & bottomBar height
                    .width(180.dp)
                    .fillMaxHeight()
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                VerticalMenu(
                    onMenuClick = { menuItem ->
                        when (menuItem.title) {
                            "Matched Items" -> selectedMenu = "Matched"
                            "UnMatched Items" -> selectedMenu = "Unmatched"
                            "Search" -> {
                                val displayedUnmatchedItems =
                                    displayItems.filter { it.scannedStatus == "Unmatched" }
                                try {
                                    navController.getBackStackEntry(Screens.SearchScreen.route)
                                        .savedStateHandle["unmatchedItems"] =
                                        displayedUnmatchedItems
                                } catch (e: Exception) {
                                    navController.navigate(Screens.SearchScreen.route)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("unmatchedItems", displayedUnmatchedItems)
                                    return@VerticalMenu
                                }

                                navController.navigate(Screens.SearchScreen.route)
                            }

                            else -> selectedMenu = "All"
                        }
                        showMenu = false
                    }
                )


            }
        }
    }
}

@Composable
fun FilterRow(
    onCategoryClick: () -> Unit,
    onProductClick: () -> Unit,
    onDesignClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterButton("Category", onCategoryClick)
        FilterButton("Product", onProductClick)
        FilterButton("Design", onDesignClick)
    }
}

@Composable
fun SummaryRow(rows: List<TableRow>) {
    val totalQty = rows.sumOf { it.items.size }
    val totalGwt = rows.sumOf { it.items.sumOf { it.grossWeight?.toDoubleOrNull() ?: 0.0 } }
    val totalMatchedQty = rows.sumOf { it.items.count { !it.rfid.isNullOrBlank() } }
    val totalMatchedWt = rows.sumOf {
        it.items.filter { !it.rfid.isNullOrBlank() }
            .sumOf { it.netWeight?.toDoubleOrNull() ?: 0.0 }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF3B363E))
            .padding(vertical = 4.dp)
    ) {
        TableHeaderCell("Total", colCategoryWidth)
        TableHeaderCell("$totalQty", colQtyWidth)
        TableHeaderCell("%.2f".format(totalGwt), colWeightWidth)
        TableHeaderCell("$totalMatchedQty", colMatchedQtyWidth)
        TableHeaderCell("%.2f".format(totalMatchedWt), colMatchedWtWidth)
        TableHeaderCell("", colStatusWidth)
    }
}


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
            fontSize = 10.sp, // Reduced font to fit better
            maxLines = 1,
            overflow = TextOverflow.Clip // No ellipsis
        )
    }
}

@Composable
fun TableHeader(title: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF3B363E))
            .padding(vertical = 2.dp)
    ) {
        TableHeaderCell(title, colCategoryWidth) // shows Category/Product/Design
        TableHeaderCell("Qty", colQtyWidth)
        TableHeaderCell("G.Wt", colWeightWidth)
        TableHeaderCell("M.Qty", colMatchedQtyWidth)
        TableHeaderCell("M.Wt", colMatchedWtWidth)
        TableHeaderCell("Status", colStatusWidth)
    }
}

@Composable
fun FilterButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .height(36.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF3053F0), Color(0xFFE82E5A)))
        )
    ) {
        Text(
            text = label,
            color = Color.DarkGray,
            fontFamily = poppins,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TableDataRow(row: TableRow, currentLevel: String, onRowClick: () -> Unit) {
    val qty = row.items.size
    val matchedItems = row.items.filter { !it.rfid.isNullOrBlank() }
    val matchedQty = matchedItems.size
    val grossWeight = row.items.sumOf { it.grossWeight?.toDoubleOrNull() ?: 0.0 }
    val matchedWeight = matchedItems.sumOf { it.netWeight?.toDoubleOrNull() ?: 0.0 }
    val isMatched = matchedQty == qty && qty != 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 4.dp)
            .clickable {
                onRowClick()
            }
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
        else -> R.drawable.ic_unmatched // optional
    }

    Box(
        modifier = Modifier
            .width(width)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
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
        MenuItem("Unlabeled Items", R.drawable.ic_list_unlabelled),
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
                        .heightIn(max = 280.dp)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedItems.contains(item)) {
                                        selectedItems.remove(item)
                                    } else {
                                        selectedItems.add(item)
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedItems.contains(item),
                                onCheckedChange = null, // Don't override clickable behavior
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

                // ✅ Centered Buttons
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
        },
        confirmButton = {

        }, // Empty because we handled buttons manually
        dismissButton = {}
    )
}


data class MenuItem(val title: String, val iconRes: Int)

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


data class TableRow(val label: String, val items: List<BulkItem>)