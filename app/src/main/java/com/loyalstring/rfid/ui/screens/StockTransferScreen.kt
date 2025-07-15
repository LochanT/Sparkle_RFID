package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.poppins
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
    val transferTypes = listOf("Select", "Internal", "External")
    val branchList = listOf("Branch A", "Branch B", "Branch C")
    val categories = listOf("Category", "Product", "Design", "Box", "SKU")

    var selectedTransferType by remember { mutableStateOf(transferTypes[0]) }
    var fromBranch by remember { mutableStateOf(branchList[0]) }
    var toBranch by remember { mutableStateOf(branchList[0]) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val items = remember {
        mutableStateListOf(
            Triple("Product 1", 10.5, 9.8),
            Triple("Product 2", 15.2, 14.0),
            Triple("Product 3", 7.6, 6.9)
        )
    }
    val selectedItems = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Stock Transfer",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                showCounter = false,
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
        ) {

            // Dropdowns
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DropdownMenuField("Transfer Type", transferTypes, selectedTransferType) {
                    selectedTransferType = it
                }
                DropdownMenuField("From", branchList, fromBranch) {
                    fromBranch = it
                }
                DropdownMenuField("To", branchList, toBranch) {
                    toBranch = it
                }
            }

            // Filter buttons (scrollable with arrows)
            HorizontalCategoryScroll(
                items = categories,
                onItemClick = { selected ->
                    selectedCategory = selected
                }
            )

            // Table headers
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sr", "Product Name", "Label", "Gross WT", "Net WT", "Transfer").forEach {
                    Text(
                        it,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontFamily = poppins
                    )
                }
            }

            // Item list
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false) // shrink wrap to content
            ) {
                itemsIndexed(items) { index, item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}",
                            Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.first,
                            Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontFamily = poppins
                        )
                        Text(
                            "Label $index",
                            Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.second.toString(),
                            Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.third.toString(),
                            Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontFamily = poppins
                        )
                        Checkbox(
                            checked = selectedItems.contains(index),
                            onCheckedChange = {
                                if (it) selectedItems.add(index) else selectedItems.remove(index)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Total Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Qty: ${selectedItems.size}", fontSize = 13.sp, fontFamily = poppins)
                Text(
                    "T G.WT: ${selectedItems.sumOf { items[it].second }}",
                    fontSize = 13.sp,
                    fontFamily = poppins
                )
                Text(
                    "T N.WT: ${selectedItems.sumOf { items[it].third }}",
                    fontSize = 13.sp,
                    fontFamily = poppins
                )
                Button(
                    onClick = { /* Transfer action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B005D))
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Transfer", tint = Color.White)
                }
            }
        }
    }
}


@Composable
fun DropdownMenuField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption, fontSize = 12.sp, fontFamily = poppins)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FilterChip(label: String) {
    Button(
        onClick = { /* Filter action */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
    ) {
        Text(label, color = Color.Black)
    }
}

@Composable
fun HorizontalCategoryScroll(
    items: List<String>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope() // ✅ FIX: Add this

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 8.dp)
    ) {
        // Left Arrow
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val firstVisible = listState.firstVisibleItemIndex
                    listState.animateScrollToItem((firstVisible - 1).coerceAtLeast(0))
                }
            },
            enabled = listState.firstVisibleItemIndex > 0
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Scroll Left"
            )
        }

        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Button(
                    onClick = { onItemClick(item) },
                    shape = RoundedCornerShape(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text(item, color = Color.DarkGray, fontSize = 12.sp, fontFamily = poppins)
                }
            }
        }

        // Right Arrow
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val nextItem = listState.firstVisibleItemIndex + 1
                    if (nextItem < items.size) {
                        listState.animateScrollToItem(nextItem)
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Scroll Right"
            )
        }
    }
}

