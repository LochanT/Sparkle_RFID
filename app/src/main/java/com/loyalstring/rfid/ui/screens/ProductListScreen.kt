package com.loyalstring.rfid.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.ProductListViewModel
import java.io.File

@Composable
fun ProductListScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
    val viewModel: ProductListViewModel = hiltViewModel()
    val searchQuery = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var selectedCount by remember { mutableStateOf(1) }
    var isGridView by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<BulkItem?>(null) }
    val baseUrl = "https://rrgold.loyalstring.co.in/"

    val allItems by viewModel.productList.collectAsState(initial = emptyList())
    val filteredItems = remember(searchQuery.value, allItems) {
        allItems.filter { item ->
            val query = searchQuery.value.trim().lowercase()
            item.itemCode!!.lowercase().contains(query) ||
                    item.productName!!.lowercase().contains(query) ||
                    item.rfid!!.lowercase().contains(query)
        }
    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Product List",
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
                selectedCount = selectedCount,
                onCountSelected = { selectedCount = it }
            )
        },
        bottomBar = {
            ScanBottomBar(
                onSave = { /* Save logic */ },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan = { /* Scan logic */ },
                onGscan = { /* Gscan logic */ },
                onReset = { /* Reset logic */ }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

                .background(Color.White)
        ) {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Enter RFID / Item code / Product", fontFamily = poppins) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // adds spacing between buttons
            ) {
                ActionButton(
                    text = if (isGridView) "List View" else "Grid View",
                    borderColor = Color.DarkGray,
                    onClick = { isGridView = !isGridView }
                )
                ActionButton(
                    text = "Filter",
                    borderColor = Color.DarkGray,
                    onClick = { }
                )
                ActionButton(
                    text = "Export Pdf",
                    borderColor = Color.DarkGray,
                    onClick = { },
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp) // Or .defaultMinSize(minWidth = 120.dp)
                )
            }


            Spacer(Modifier.height(12.dp))

            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedItem = item
                                    showDialog = true
                                }
                                .height(IntrinsicSize.Min),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp) // Less vertical spacing
                            ) {
                                if (!item.imageUrl.isNullOrEmpty()) {
                                    val stored = item.imageUrl.trim()
                                        .trimEnd(',') // remove any trailing commas/spaces
                                    if (stored.startsWith("/")) {
                                        val file = File(stored)
                                        if (file.exists()) file
                                        else null
                                    } else {
                                        stored.split(",")
                                            .map { it.trim() }
                                            .filter { it.isNotEmpty() }
                                            .lastOrNull()
                                            ?.let {

                                                AsyncImage(
                                                    model = baseUrl + it,
                                                    contentDescription = item.itemCode,
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .align(Alignment.CenterHorizontally)
                                                )
                                            }
                                    }

                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Photo,
                                        contentDescription = item.itemCode,
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }

                                // Row: RFID & Item Code
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp) // Better spacing between the two
                                ) {
                                    Text(
                                        text = "RFID: ${item.rfid}",
                                        fontFamily = poppins,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Item: ${item.itemCode}",
                                        fontFamily = poppins,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }


                                // Row: Gross Wt & Net Wt
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "G.Wt: ${item.grossWeight}",
                                        fontFamily = poppins,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "N.Wt: ${item.netWeight}",
                                        fontFamily = poppins,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }


            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                        .background(Color(0xFF2E2E2E)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "S.No",
                        Modifier.width(60.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "Product Name" to 150.dp,
                            "Item code" to 180.dp,
                            "RFID" to 90.dp,
                            "G.wt" to 90.dp,
                            "S.wt" to 90.dp,
                            "D.wt" to 90.dp,
                            "N.wt" to 90.dp,
                            "Category" to 80.dp,
                            "Design" to 70.dp,
                            "Purity" to 70.dp,
                            "Making/g" to 90.dp,
                            "Making%" to 90.dp,
                            "Fix Making" to 100.dp,
                            "Fix Wastage" to 100.dp,
                            "S Amount" to 100.dp,
                            "D Amount" to 100.dp,
                            "SKU" to 120.dp,
                            "EPC" to 120.dp,
                            "Vendor" to 120.dp,
                            "TID" to 90.dp
                        ).forEach { (label, width) ->
                            Text(
                                label,
                                Modifier.width(width),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontFamily = poppins,
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        "Edit",
                        Modifier.width(35.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = poppins,
                        fontSize = 14.sp
                    )
                    Text(
                        "Delete",
                        Modifier.width(55.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = poppins,
                        fontSize = 14.sp
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filteredItems) { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}",
                                Modifier.width(60.dp),
                                textAlign = TextAlign.Center,
                                fontFamily = poppins,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedItem = item
                                        showDialog = true
                                    }
                                    .horizontalScroll(scrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    item.productName to 150.dp,
                                    item.itemCode to 180.dp,
                                    item.rfid to 90.dp,
                                    item.grossWeight to 90.dp,
                                    item.stoneWeight to 90.dp,
                                    item.dustWeight to 90.dp,
                                    item.netWeight to 90.dp,
                                    item.category to 80.dp,
                                    item.design to 70.dp,
                                    item.purity to 70.dp,
                                    item.makingPerGram to 90.dp,
                                    item.makingPercent to 90.dp,
                                    item.fixMaking to 100.dp,
                                    item.fixWastage to 100.dp,
                                    item.stoneAmount to 100.dp,
                                    item.dustAmount to 100.dp,
                                    item.sku to 120.dp,
                                    (item.uhfTagInfo?.epc ?: item.epc) to 120.dp,
                                    item.vendor to 120.dp,
                                    (item.uhfTagInfo?.epc ?: item.epc) to 90.dp
                                ).forEach { (value, width) ->
                                    Text(
                                        value?.ifBlank { "-" } ?: "-",
                                        Modifier.width(width),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        fontFamily = poppins,
                                        maxLines = 1
                                    )
                                }
                            }

                            IconButton(onClick = {
                                try {
                                    val currentEntry = navController.currentBackStackEntry
                                    currentEntry?.savedStateHandle?.set("item", item)
                                    navController.navigate(Screens.EditProductScreen.route)
                                } catch (e: Exception) {
                                    Log.e("NAVIGATION", "BackStackEntry error: ${e.message}")
                                }
                            }, modifier = Modifier.width(30.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.DarkGray
                                )
                            }
                            IconButton(
                                onClick = { /* Delete */ },
                                modifier = Modifier.width(50.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
            if (showDialog && selectedItem != null) {
                ItemDetailsDialog(item = selectedItem!!, onDismiss = { showDialog = false })
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = borderColor),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text,
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun ItemDetailsDialog(
    item: BulkItem,
    onDismiss: () -> Unit
) {
    val baseUrl = "https://rrgold.loyalstring.co.in/"
    val imageUrl = item.imageUrl?.split(",")
        ?.lastOrNull()
        ?.trim()
        ?.let { "$baseUrl$it" }

    var scale by remember { mutableStateOf(1f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Item Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 14.sp,
                        fontFamily = poppins
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close", fontFamily = poppins)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!imageUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Zoomable Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                InfoRow("Product Name", item.productName)
                InfoRow("Item Code", item.itemCode)
                InfoRow("RFID", item.rfid)
                InfoRow("G.Wt", item.grossWeight)
                InfoRow("S.Wt", item.stoneWeight)
                InfoRow("D.Wt", item.dustWeight)
                InfoRow("N.Wt", item.netWeight)
                InfoRow("Category", item.category)
                InfoRow("Design", item.design)
                InfoRow("Purity", item.purity)
                InfoRow("Making/Gram", item.makingPerGram)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            "$label:",
            modifier = Modifier.weight(1f),
            color = Color.DarkGray,
            fontSize = 12.sp,
            fontFamily = poppins
        )
        Text(value ?: "-", modifier = Modifier.weight(1.5f), fontSize = 12.sp, fontFamily = poppins)
    }
}