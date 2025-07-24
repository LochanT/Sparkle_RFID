package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.BackgroundGradient
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import com.loyalstring.rfid.viewmodel.StockTransferViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@Composable
fun StockTransferScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: StockTransferViewModel = hiltViewModel()
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()

    val transferTypes by viewModel.transferTypes.collectAsState()
    val counterList by viewModel.counterNames.collectAsState()
    val boxList by viewModel.boxNames.collectAsState()
    val branchList by viewModel.branchNames.collectAsState()
    val filteredItems by viewModel.filteredBulkItems.collectAsState()

    val selectedItems = remember { mutableStateListOf<Int>() }

    var selectedTransferType by remember { mutableStateOf("Transfer Type") }
    var selectedFrom by remember { mutableStateOf("From") }
    var selectedTo by remember { mutableStateOf("To") }

    val counters by remember { derivedStateOf { singleProductViewModel.counters } }
    val branches by remember { derivedStateOf { singleProductViewModel.branches } }
    val boxes by remember { derivedStateOf { singleProductViewModel.boxes } }
    val packets by remember { derivedStateOf { singleProductViewModel.packets } }

    val (fromType, toType) = remember(selectedTransferType) {
        selectedTransferType.split(" to ", ignoreCase = true).map { it.trim().lowercase() }.let {
            it.getOrNull(0) to it.getOrNull(1)
        }
    }

    val columnWidths = listOf(
        40.dp,   // Sr
        100.dp,  // Product Name
        80.dp,   // Label
        80.dp,   // Gross WT
        80.dp,   // Net WT
        40.dp    // Checkbox
    )


    val fromOptions = when (fromType) {
        "counter" -> counters.mapNotNull { it.CounterName }
        "box" -> boxes.mapNotNull { it.BoxName }
        "branch" -> branches.mapNotNull { it.BranchName }
        "packet" -> packets.mapNotNull { it.PacketName }
        else -> emptyList()
    }

    val toOptions = when (toType) {
        "counter" -> counters.mapNotNull { it.CounterName }
        "box" -> boxes.mapNotNull { it.BoxName }
        "branch" -> branches.mapNotNull { it.BranchName }
        "packet" -> packets.mapNotNull { it.PacketName }
        else -> emptyList()
    }.filter { it != selectedFrom || fromType != toType }


    val selectAllChecked by derivedStateOf {
        selectedItems.size == filteredItems.size && filteredItems.isNotEmpty()
    }

    val employee =
        remember { UserPreferences.getInstance(context).getEmployee(Employee::class.java) }

    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            viewModel.loadTransferTypes(ClientCodeRequest(it))
            viewModel.fetchCounterNames()
            viewModel.fetchBoxNames()
            viewModel.fetchBranchNames()
        }
    }
    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            singleProductViewModel.fetchAllStockTransferData(ClientCodeRequest(it))
        }
    }

    val scope = rememberCoroutineScope()

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
                showCounter = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                GradientDropdownButton(
                    label = "Transfer Type",
                    selectedOption = selectedTransferType,
                    options = transferTypes.mapNotNull { it.TransferType },
                    onSelect = {
                        selectedTransferType = it
                        viewModel.onTransferTypeSelected(it)
                        viewModel.extractFromAndToOptions(it)
                        selectedFrom = "From"
                        selectedTo = "To"
                        selectedItems.clear()
                    }
                )

                GradientDropdownButton(
                    label = "From",
                    selectedOption = selectedFrom,
                    options = fromOptions,
                    onSelect = {
                        selectedFrom = it
                        viewModel.filterBulkItemsByFrom(viewModel.currentFrom.value, it)
                        selectedItems.clear()
                    }
                )

                GradientDropdownButton(
                    label = "To",
                    selectedOption = selectedTo,
                    options = toOptions,
                    onSelect = { selectedTo = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Sr", "Product Name", "Label", "Gross WT", "Net WT").forEach {
                    Text(
                        it,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = selectAllChecked, onCheckedChange = { checked ->
                            selectedItems.clear()
                            if (checked) selectedItems.addAll(filteredItems.indices)
                        })
                        Text(
                            "All",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }


            LazyColumn(modifier = Modifier.weight(1f, false)) {
                itemsIndexed(filteredItems) { index, item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}",
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.productName.orEmpty(),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.rfid.orEmpty(),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.grossWeight.orEmpty(),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontFamily = poppins
                        )
                        Text(
                            item.netWeight.orEmpty(),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontFamily = poppins
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        ) {
                            Checkbox(
                                checked = selectedItems.contains(index),
                                onCheckedChange = {
                                    if (it) selectedItems.add(index) else selectedItems.remove(index)
                                }
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalGrossWeight = selectedItems.sumOf {
                filteredItems.getOrNull(it)?.grossWeight?.toDoubleOrNull() ?: 0.0
            }
            val totalNetWeight = selectedItems.sumOf {
                filteredItems.getOrNull(it)?.netWeight?.toDoubleOrNull() ?: 0.0
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Qty: ${selectedItems.size}", fontSize = 13.sp, fontFamily = poppins)
                Text("T G.WT: $totalGrossWeight", fontSize = 13.sp, fontFamily = poppins)
                Text("T N.WT: $totalNetWeight", fontSize = 13.sp, fontFamily = poppins)


                GradientButton(
                    onClick = {
                        scope.launch {
                            val stockIds =
                                selectedItems.mapNotNull { filteredItems.getOrNull(it)?.id }
                            val fromId =
                                fromType?.let { viewModel.getEntityIdByName(it, selectedFrom) }
                            val toId = toType?.let { viewModel.getEntityIdByName(it, selectedTo) }
                            val transferTypeId =
                                viewModel.transferTypes.value.find { it.TransferType == selectedTransferType }?.Id
                                    ?: 0

                            employee?.let {
                                it.clientCode?.let { it1 ->
                                    fromId?.let { it2 ->
                                        toId?.let { it3 ->
                                            viewModel.submitStockTransfer(
                                                it1,
                                                stockIds,
                                                transferTypeId,
                                                it.employeeId.toString(),
                                                it2,
                                                it3
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    icon = painterResource(id = R.drawable.stock_transfer_svg),
                    text = "",
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }
    }
}



@Composable
fun GradientDropdownButton(
    label: String,
    selectedOption: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(horizontal = 4.dp)) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2F1EFA), Color(0xFFE5203F))
                    ),
                    shape = RoundedCornerShape(10)
                )
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = selectedOption,
                color = Color.DarkGray,
                fontSize = 12.sp,
                fontFamily = poppins
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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
fun HorizontalCategoryScroll(
    items: List<String>,
    onItemClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        IconButton(onClick = {
            scope.launch {
                val first = listState.firstVisibleItemIndex
                listState.animateScrollToItem((first - 1).coerceAtLeast(0))
            }
        }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Scroll Left")
        }

        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items) { item ->
                Button(
                    onClick = { onItemClick(item) },
                    shape = RoundedCornerShape(10),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text(item, color = Color.Black, fontSize = 12.sp, fontFamily = poppins)
                }
            }
        }

        IconButton(onClick = {
            scope.launch {
                val next = listState.firstVisibleItemIndex + 1
                if (next < items.size) listState.animateScrollToItem(next)
            }
        }) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Scroll Right")
        }
    }
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    text: String
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(BackgroundGradient, shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = null,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 12.dp)
                )
            }
            Text(text, color = Color.White, fontSize = 14.sp, fontFamily = poppins)
        }
    }
}
