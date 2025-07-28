package com.loyalstring.rfid.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.viewmodel.OrderViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
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
    var visibleItems by remember { mutableStateOf(20) }
    var searchQuery by remember { mutableStateOf("") }




    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            orderViewModel.fetchAllOrderListFromApi(ClientCodeRequest(it))
        }
    }

    val filteredData = if (searchQuery.isNotEmpty()) {
        allItems.filter {

            it.OrderNo?.contains(searchQuery, ignoreCase = true) == true ||
                    it.Category?.contains(searchQuery, ignoreCase = true) == true ||
                    it.Customer?.FirstName?.contains(searchQuery, ignoreCase = true) == true
        }
    } else allItems

    val visibleData = filteredData.take(visibleItems)

    val headerTitles = listOf(
        "Order No", "Name", "Contact", "Product",
        "N.Amt", "Total Amt", "Order Date", "Status"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(
            title = "OrderList",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
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
            visibleItems = 20
        }

        // ✅ Always show table, even if loading
        OrderTableWithPagination(
            navController = navController,
            headerTitles = headerTitles,
            data = visibleData,
            onLoadMore = {
                if (visibleItems < filteredData.size) {
                    visibleItems += 20
                }
            },
            isLoading = isLoading,
            context = context,
            employee = employee
        )
    }
}


@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
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
            imageVector = Icons.Default.Search,
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
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
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
    employee: Employee?
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    var showOrderDialog by remember { mutableStateOf(false) }
    var showEditOrderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // 🔒 Header Row with Actions
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .background(Color.DarkGray)
                .padding(vertical = 8.dp)
        ) {
            headerTitles.forEach { title ->
                Text(
                    text = title,
                    modifier = Modifier
                        .width(120.dp)
                        .padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // ✅ Add "Actions" column header
            Text(
                text = "Actions",
                modifier = Modifier
                    .width(100.dp)
                    .padding(horizontal = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 🧾 Scrollable Data
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val singleProductViewModel: SingleProductViewModel = hiltViewModel()
                val branchList = singleProductViewModel.branches
                Column(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    data.forEachIndexed { index, row ->
                        Row(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                row.OrderNo ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )
                            Text(
                                row.Customer?.FirstName ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )
                            Text(
                                row.Customer?.Mobile ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )
                            Text(
                                row.Category ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )
                            Text(
                                row.TotalNetAmount ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )
                            Text(
                                row.TotalAmount ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )
                            Text(
                                formatDate(row.OrderDate ?: ""),
                               // row.OrderDate ?: "",
                                Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 8.dp),
                                fontSize = 14.sp
                            )

                            // ✅ Action Buttons Column
                            Row(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    navController.navigate("order")
                                    // TODO: Handle Edit Action

                                    /*          OrderDetailsDialogEditAndDisplay(

                                                  row.CustomOrderItem.get(0),
                                                  branchList,
                                                  onDismiss = { showEditOrderDialog = false },
                                                  //  onConfirm = onConfirmOrderDetails,
                                                  onSave = {
                                                      // handle saved data
                                                      showOrderDialog = false
                                                  },
                                                  edit = 2

                                              )*/
                                }, modifier = Modifier.size(25.dp)) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.Blue
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = {
                                    // TODO: Handle Print Action
                                    generateInvoicePdfAndOpen(context, row, employee)
                                }, modifier = Modifier.size(25.dp)) {
                                    Icon(
                                        Icons.Default.Print,
                                        contentDescription = "Print",
                                        tint = Color.DarkGray
                                    )
                                }
                            }
                        }

                        // 🔄 Pagination Trigger
                        if (index == data.lastIndex && index % 20 == 19) {
                            LaunchedEffect(Unit) {
                                onLoadMore()
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
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Assuming the date is in "yyyy-MM-dd" format
        val date = format.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        outputFormat.format(date ?: Date()) // Default to current date if parsing fails
    } catch (e: Exception) {
        dateString // Return the original string if there is an error
    }
}




