package com.loyalstring.rfid.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderLisrScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    userPreferences: UserPreferences
) {
    val orderViewModel: OrderViewModel = hiltViewModel()
    val context = LocalContext.current
    val employee =
        remember { UserPreferences.getInstance(context).getEmployee(Employee::class.java) }

    val allItems by orderViewModel.getAllOrderList.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState(false)
    var visibleItems by remember { mutableStateOf(7000) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            orderViewModel.fetchAllOrderListFromApi(ClientCodeRequest(it))
        }
    }

    LaunchedEffect(Unit) {
        employee?.clientCode?.let { clientCode ->

            orderViewModel.getAllItemCodeList(ClientCodeRequest(clientCode))

        }
    }
    val itemCodeList by orderViewModel.itemCodeResponse.collectAsState()

    val filteredData = if (searchQuery.isNotEmpty()) {
        allItems.filter {
            it.OrderNo.contains(searchQuery, true) ||
                    it.Category.contains(searchQuery, true) ||
                    it.Customer.FirstName.contains(searchQuery, true) == true
        }
    } else allItems

    val visibleData = filteredData.take(visibleItems)

    val headerTitles = listOf(
        "Order No",
        "Name",
        "Contact",
        "Product",
        "Branch",
        "Qty",
        "Tot Wt",
        "N.Amt",
        "Total Amt",
        "Order Date",
        "Actions"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(
            title = "OrderList",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {},
            showCounter = false,
            selectedCount = 0,
            onCountSelected = {}
        )

        SearchBar(searchQuery) {
            searchQuery = it
            visibleItems = 10
        }

        OrderTableWithPagination(
            navController = navController,
            headerTitles = headerTitles,
            data = visibleData,
            onLoadMore = {
                if (visibleItems < filteredData.size) {
                    visibleItems += 10
                }
            },
            isLoading = isLoading,
            context = context,
            employee = employee,
            itemCodeList = itemCodeList
        )
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(45.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF2F2F2))
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.padding(start = 12.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.Gray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = if (value.isNotEmpty()) 36.dp else 12.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
            )
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun OrderTableWithPagination(
    navController: NavHostController,
    headerTitles: List<String>,
    data: List<CustomOrderResponse>,
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    context: Context,
    employee: Employee?,
    itemCodeList: List<ItemCodeResponse>
) {
    val sharedScrollState = rememberScrollState()
    val orderViewModel: OrderViewModel = hiltViewModel()

    Column(modifier = Modifier.fillMaxSize()) {

        // Shared header scroll
        Row(
            modifier = Modifier
                .horizontalScroll(sharedScrollState)
                .background(Color.DarkGray)
                .padding(vertical = 8.dp)
        ) {
            headerTitles.forEach {
                Text(
                    text = it,
                    modifier = Modifier
                        .width(120.dp)
                        .padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(data) { row ->
                    Row(
                        modifier = Modifier
                            .horizontalScroll(sharedScrollState)
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            row.OrderNo ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            row.Customer?.FirstName ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            row.Customer?.Mobile ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            row.Category ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            "" ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )

                        Text(
                            row.Qty ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            row.TotalStoneWeight ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )

                        Text(
                            row.TotalNetAmount ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            row.TotalAmount ?: "",
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            formatDate(row.OrderDate),
                            Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            IconButton(onClick = {
                                generateInvoicePdfAndOpen(
                                    context,
                                    row,
                                    employee,
                                    itemCodeList
                                )
                            }) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = "Print",
                                    tint = Color.DarkGray
                                )
                            }

                            IconButton(onClick = {
                                employee?.clientCode?.let {
                                    orderViewModel.deleteOrders(
                                        ClientCodeRequest(it),
                                        row.CustomOrderId
                                    ) { isSuccess ->
                                        Toast.makeText(
                                            context,
                                            if (isSuccess) "Order Deleted Successfully" else "Failed to delete",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        if (isSuccess) {
                                            orderViewModel.removeOrderById(row.CustomOrderId)
                                        }
                                    }
                                }
                            }) {
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
        }
    }
}


fun formatDate(dateString: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}