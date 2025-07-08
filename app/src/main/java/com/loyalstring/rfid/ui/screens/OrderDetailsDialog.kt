package com.loyalstring.rfid.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.model.order.CustomOrderItem
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.Customer
import com.loyalstring.rfid.data.model.order.Payment
import com.loyalstring.rfid.data.model.order.URDPurchase
import com.loyalstring.rfid.ui.utils.GradientButtonIcon
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.viewmodel.OrderViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// Sample OrderDetails data class
data class OrderDetails(
    val branch: String,
    val exhibition: String,
    val remark: String,
    val purity: String,
    val size: String,
    val length: String,
    val typeOfColors: String,
    val screwType: String,
    val polishType: String,
    val finePercentage: String,
    val wastage: String
)

@Composable
fun OrderDetailsDialog(
    selectedCustomerId: Int?,
    selectedCustomer: EmployeeList?,
    onDismiss: () -> Unit,
    onSave: (OrderDetails) -> Unit,
    viewModel: SingleProductViewModel = hiltViewModel(),

    ) {

    val orderViewModel: OrderViewModel = hiltViewModel()
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    // Form state
    var branch by remember { mutableStateOf("") }
    var exhibition by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var purity by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var typeOfColors by remember { mutableStateOf("") }
    var screwType by remember { mutableStateOf("") }
    var polishType by remember { mutableStateOf("") }
    var finePercentage by remember { mutableStateOf("") }
    var wastage by remember { mutableStateOf("") }
    var orderDate by remember { mutableStateOf("") }
    var deliverDate by remember { mutableStateOf("") }

    //val branchList = listOf("Branch 1", "Branch 2", "Branch 3")

    val branchList by orderViewModel.branchResponse.collectAsState()
    val exhibitionList by orderViewModel.branchResponse.collectAsState()
    //   val purityList = listOf("Purity 1", "Purity 2")
    //  val purityList = (viewModel.purityResponse.observeAsState().value as? Resource.Success)?.data
    val purityList by singleProductViewModel.purityResponse1.collectAsState()
    val sizeList = listOf("Size 1", "Size 2")
    val lengthList = listOf("Length 1", "Length 2")
    val colorsList = listOf(
        "Yellow Gold",
        "White Gold",
        "Rose Gold",
        "Green Gold",
        "Black Gold",
        "Blue Gold",
        "Purple Gold"
    )
    val screwList = listOf("Type 1", "Type 2", "Type 3")
    val polishList = listOf("High Polish", "Matte Finish", "Satin Finish", "Hammered")

    var expandedBranch by remember { mutableStateOf(false) }
    var expandedExhibition by remember { mutableStateOf(false) }
    var expandedPurity by remember { mutableStateOf(false) }
    var expandedSize by remember { mutableStateOf(false) }
    var expandedLength by remember { mutableStateOf(false) }
    var expandedColors by remember { mutableStateOf(false) }
    var expandedScrew by remember { mutableStateOf(false) }
    var expandedPolish by remember { mutableStateOf(false) }

    // Inside @Composable

    val calendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    /*  LaunchedEffect(Unit) {
          employee?.clientCode?.let {
              orderViewModel.getAllBranchList(ClientCodeRequest(it))
          }
      }*/


    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp) // Toolbar-like height
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.CenterStart // or Alignment.Center for centered text
                ) {
                    Text(
                        text = "Order Details",
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp) // Add start padding if using CenterStart
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                    // .verticalScroll(rememberScrollState())
                ) {
                    // Title
                    /* Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp) // Toolbar-like height
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.CenterStart // or Alignment.Center for centered text
                ) {
                    Text(
                        text = "Order Details",
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp) // Add start padding if using CenterStart
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))*/

                    // Use your DropdownMenuField & input rows here
                    // Example: Branch dropdown
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (branchList.isEmpty()) {
                                //Text("Loading branches...", modifier = Modifier.padding(8.dp))
                            } else {
                                DropdownMenuField(
                                    label = "Branch",
                                    options = branchList,
                                    selectedValue = branch,
                                    expanded = expandedBranch,
                                    onValueChange = { branch = it },
                                    onExpandedChange = { expandedBranch = it },
                                    labelColor = Color.Black,
                                    getOptionLabel = { it.BranchName }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exhibition",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (exhibition.isEmpty()) {
                                Text(
                                    text = "Enter exhibition",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = exhibition,
                                onValueChange = { exhibition = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remark",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (remark.isEmpty()) {
                                Text(
                                    text = "Enter remark",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = remark,
                                onValueChange = { remark = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuField(
                                "Purity",
                                purityList,
                                purity,
                                expandedPurity,
                                { purity = it },
                                { expandedPurity = it },
                                labelColor = Color.Black,
                                getOptionLabel = { it.PurityName.toString() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Size",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (exhibition.isEmpty()) {
                                Text(
                                    text = "Enter size",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = size,
                                onValueChange = { size = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuField(
                                "Colors",
                                colorsList,
                                typeOfColors,
                                expandedColors,
                                { typeOfColors = it },
                                { expandedColors = it },
                                labelColor = Color.Black,
                                getOptionLabel = { it.toString() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuField(
                                "Screw Type",
                                screwList,
                                screwType,
                                expandedScrew,
                                { screwType = it },
                                { expandedScrew = it },
                                labelColor = Color.Black,
                                getOptionLabel = { it.toString() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuField(
                                "Polish Type",
                                polishList,
                                polishType,
                                expandedPolish,
                                { polishType = it },
                                { expandedPolish = it },
                                labelColor = Color.Black,
                                getOptionLabel = { it.toString() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fine %",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (finePercentage.isEmpty()) {
                                Text(
                                    text = "Enter Fine %",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = finePercentage,
                                onValueChange = { finePercentage = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wastage",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (wastage.isEmpty()) {
                                Text(
                                    text = "Enter wastage",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = wastage,
                                onValueChange = { wastage = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))


                    /*    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order Date",
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(start = 2.dp),
                                fontSize = 12.sp,
                                color = Color.Black
                            )

                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                    .height(32.dp)

                                    .background(Color.White, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (orderDate.isEmpty()) {
                                    Text(
                                        text = "Enter Order Date",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                BasicTextField(
                                    value = orderDate,
                                    onValueChange = { orderDate = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp) // minimal inner padding for cursor spacing
                                )
                            }
                        }*/
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Date",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedDate = Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth)
                                            }
                                            orderDate = dateFormatter.format(selectedDate.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (orderDate.isEmpty()) "Enter Order Date" else orderDate,
                                    fontSize = 13.sp,
                                    color = if (orderDate.isEmpty()) Color.Gray else Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calender), // replace with your calendar icon
                                    contentDescription = "Calendar",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deliver Date",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedDate = Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth)
                                            }
                                            deliverDate = dateFormatter.format(selectedDate.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (deliverDate.isEmpty()) "Enter Deliver Date" else orderDate,
                                    fontSize = 13.sp,
                                    color = if (deliverDate.isEmpty()) Color.Gray else Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calender), // replace with your calendar icon
                                    contentDescription = "Calendar",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))





                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        GradientButtonIcon(
                            text = "Cancel",
                            onClick = {
                                println("Form Reset")
                                onDismiss()
                                // showAddCustomerDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp) // reduce height here
                                .padding(start = 8.dp, bottom = 16.dp),
                            icon = painterResource(id = R.drawable.ic_cancel),
                            iconDescription = "Check Icon",
                            fontSize = 12
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons
                        GradientButtonIcon(
                            text = "OK",
                         /*   onClick = {
                                *//*  OrderDetails(
                                      branch, exhibition, remark, purity, size, length,
                                      typeOfColors, screwType, polishType, finePercentage, wastage
                                  )*//*
                                val request = CustomOrderRequest(
                                    CustomOrderId = 0,
                                    CustomerId = selectedCustomerId.toString(),
                                    ClientCode = employee?.clientCode.toString(),
                                    OrderId = 0,
                                    TotalAmount = "",
                                    PaymentMode = "",
                                    Offer = null,
                                    Qty = "",
                                    GST = "",
                                    OrderStatus = "",
                                    MRP = "",
                                    VendorId = 12,
                                    TDS = null,
                                    PurchaseStatus = null,
                                    GSTApplied = "",
                                    Discount = "",
                                    TotalNetAmount = "",
                                    TotalGSTAmount = "",
                                    TotalPurchaseAmount = "",
                                    ReceivedAmount = "",
                                    TotalBalanceMetal = "",
                                    BalanceAmount = "",
                                    TotalFineMetal = "",
                                    CourierCharge = null,
                                    SaleType = null,
                                    OrderDate = "2025-07-08",
                                    OrderCount = "1",
                                    AdditionTaxApplied = "0",
                                    CategoryId = 2,
                                    OrderNo = "ORD123456",
                                    DeliveryAddress = "123 Street, Mumbai",
                                    BillType = "Retail",
                                    UrdPurchaseAmt = null,
                                    BilledBy = "Employee1",
                                    SoldBy = "Employee1",
                                    CreditSilver = null,
                                    CreditGold = null,
                                    CreditAmount = null,
                                    BalanceAmt = "25000",
                                    BalanceSilver = null,
                                    BalanceGold = null,
                                    TotalSaleGold = null,
                                    TotalSaleSilver = null,
                                    TotalSaleUrdGold = null,
                                    TotalSaleUrdSilver = null,
                                    FinancialYear = "2024-25",
                                    BaseCurrency = "INR",
                                    TotalStoneWeight = "0.5",
                                    TotalStoneAmount = "2000",
                                    TotalStonePieces = "3",
                                    TotalDiamondWeight = "0.3",
                                    TotalDiamondPieces = "2",
                                    TotalDiamondAmount = "1500",
                                    FineSilver = "0",
                                    FineGold = "5.0",
                                    DebitSilver = null,
                                    DebitGold = null,
                                    PaidMetal = "5.0",
                                    PaidAmount = "25000",
                                    TotalAdvanceAmt = null,
                                    TaxableAmount = "47000",
                                    TDSAmount = null,
                                    CreatedOn = "2025-07-08",
                                    LastUpdated = "2025-07-08",
                                    StatusType = true,
                                    FineMetal = "5.0",
                                    BalanceMetal = "0.0",
                                    AdvanceAmt = "0",
                                    PaidAmt = "25000",
                                    TaxableAmt = "47000",
                                    GstAmount = "2500",
                                    GstCheck = "true",
                                    Category = "Ring",
                                    TDSCheck = "false",
                                    Remark = "Urgent order",
                                    OrderItemId = null,
                                    StoneStatus = null,
                                    DiamondStatus = null,
                                    BulkOrderId = null,
                                    CustomOrderItem = listOf(
                                        CustomOrderItem(
                                            CustomOrderId = 1,
                                            OrderDate = "2025-07-08",
                                            DeliverDate = "2025-07-10",
                                            SKUId = 101,
                                            SKU = "SKU123",
                                            CategoryId = 5,
                                            VendorId = null,
                                            CategoryName = "Rings",
                                            CustomerName = "John Doe",
                                            VendorName = null,
                                            ProductId = 10,
                                            ProductName = "Gold Ring",
                                            DesignId = 8,
                                            DesignName = "Classic",
                                            PurityId = 22,
                                            PurityName = "22KT",
                                            GrossWt = "15.0",
                                            StoneWt = "1.0",
                                            DiamondWt = "0.5",
                                            NetWt = "13.5",
                                            Size = "14",
                                            Length = "0",
                                            TypesOdColors = "Yellow",
                                            Quantity = "1",
                                            RatePerGram = "5500",
                                            MakingPerGram = "300",
                                            MakingFixed = "0",
                                            FixedWt = "0",
                                            MakingPercentage = "12",
                                            DiamondPieces = "2",
                                            DiamondRate = "50000",
                                            DiamondAmount = "25000",
                                            StoneAmount = "1000",
                                            ScrewType = "Normal",
                                            Polish = "Mirror",
                                            Rhodium = "No",
                                            SampleWt = "0",
                                            Image = "",
                                            ItemCode = "ITEM123",
                                            CustomerId = 201,
                                            MRP = "95000",
                                            HSNCode = "7113",
                                            UnlProductId = 0,
                                            OrderBy = "Admin",
                                            StoneLessPercent = "0",
                                            ProductCode = "PRD123",
                                            TotalWt = "15.0",
                                            BillType = "Retail",
                                            FinePercentage = "91.6",
                                            ClientCode = "LS000241",
                                            OrederId = null,
                                            CreatedOn = "2025-07-08",
                                            LastUpdated = "2025-07-08",
                                            StatusType = true,
                                            PackingWeight = "0.2",
                                            MetalAmount = "74250",
                                            OldGoldPurchase = null,
                                            Amount = "100000",
                                            totalGstAmount = "5000",
                                            finalPrice = "105000",
                                            MakingFixedWastage = "200",
                                            Description = "Gold Ring with Diamonds",
                                            CompanyId = 1,
                                            LabelledStockId = null,
                                            TotalStoneWeight = "1.0",
                                            BranchId = 2,
                                            BranchName = "Mumbai",
                                            Exhibition = "No",
                                            CounterId = 3,
                                            EmployeeId = 4,
                                            OrderNo = "ORD20250708",
                                            OrderStatus = "Pending",
                                            DueDate = "2025-07-15",
                                            Remark = "Urgent delivery",
                                            Id = 0,
                                            PurchaseInvoiceNo = null,
                                            Purity = "22KT",
                                            Status = null,
                                            URDNo = null,
                                            Stones = emptyList(),
                                            Diamond = emptyList()
                                        )
                                    ),
                                    Payments = listOf(
                                        Payment("")
                                    ),
                                    uRDPurchases = listOf(
                                        URDPurchase("")
                                    ),
                                    Customer = Customer(
                                        FirstName = selectedCustomer?.FirstName.orEmpty(),
                                        LastName = selectedCustomer?.LastName.orEmpty(),
                                        PerAddStreet = "",
                                        CurrAddStreet = "",
                                        Mobile = selectedCustomer?.Mobile.orEmpty(),
                                        Email = selectedCustomer?.Email.orEmpty(),
                                        Password = "",
                                        CustomerLoginId = selectedCustomer?.Email.orEmpty(), // usually same as Email
                                        DateOfBirth = "",
                                        MiddleName = "",
                                        PerAddPincode = "",
                                        Gender = "",
                                        OnlineStatus = "",
                                        CurrAddTown = selectedCustomer?.CurrAddTown.orEmpty(),
                                        CurrAddPincode = "",
                                        CurrAddState = selectedCustomer?.CurrAddState.orEmpty(),
                                        PerAddTown = "",
                                        PerAddState = "",
                                        GstNo = selectedCustomer?.GstNo.orEmpty(),
                                        PanNo = selectedCustomer?.PanNo.orEmpty(),
                                        AadharNo = "",
                                        BalanceAmount = "0",
                                        AdvanceAmount = "0",
                                        Discount = "0",
                                        CreditPeriod = "",
                                        FineGold = "0",
                                        FineSilver = "0",
                                        ClientCode = selectedCustomer?.ClientCode.orEmpty(),
                                        VendorId = 0,
                                        AddToVendor = false,
                                        CustomerSlabId = 0,
                                        CreditPeriodId = 0,
                                        RateOfInterestId = 0,
                                        Remark = "",
                                        Area = "",
                                        City = selectedCustomer?.City.orEmpty(),
                                        Country = selectedCustomer?.Country.orEmpty(),
                                        Id = selectedCustomer?.Id ?: 0,
                                        CreatedOn = "2025-07-08",     // as per your fixed value
                                        LastUpdated = "2025-07-08",   // as per your fixed value
                                        StatusType = true
                                    )
                                )


                                orderViewModel.addOrderCustomer(request)
                                onDismiss
                            },

*/
                            onClick = {
                                val orderItem = OrderItem(
                                    branchId = "1",
                                    branchName = branch,
                                    exhibition = exhibition,
                                    remark = remark,
                                    purity = purity,
                                    size = size,
                                    length = length,
                                    typeOfColor = typeOfColors,
                                    screwType = screwType,
                                    polishType = polishType,
                                    finePer = finePercentage,
                                    wastage = wastage,
                                    orderDate = orderDate,
                                    deliverDate = deliverDate
                                )
                                orderViewModel.insertOrderItemToRoom(orderItem)
                                onDismiss()

                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp) // Adjust height as needed
                                .padding(end = 8.dp, bottom = 16.dp),
                            icon = painterResource(id = R.drawable.check_circle),
                            iconDescription = "Check Icon",
                            fontSize = 12
                        )

                        // Observe the ViewModel's response
                        // Handle response from the ViewModel

                    }
                }
            }
        }
    }
}


/*@Composable
fun DropdownMenuField(
    label: String,
    options: List<BranchResponse>,
    selectedValue: String,
    expanded: Boolean,
    onValueChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    labelColor: Color = Color.Black
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp) // ⬅️ reduced vertical padding
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .weight(0.3f)
                    .padding(start = 8.dp),
                fontSize = 12.sp,
                color = Color.Black
            )
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(start = 4.dp) // ⬅️ reduced horizontal padding
                    .clickable { onExpandedChange(true) }
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(4.dp)
                    ) // ⬅️ slightly smaller corners
                    .padding(horizontal = 8.dp, vertical = 2.dp) // ⬅️ reduced inner padding
            ) {
                Text(
                    text = if (selectedValue.isEmpty()) "Select $label" else selectedValue,
                    style = TextStyle(fontSize = 14.sp, color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.BranchName) },
                    onClick = {
                        onValueChange(option.BranchName)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}*/

@Composable
fun <T> DropdownMenuField(
    label: String,
    options: List<T>,
    selectedValue: String,
    expanded: Boolean,
    onValueChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    labelColor: Color = Color.Black,
    getOptionLabel: (T) -> String
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .weight(0.4f)
                    .padding(start = 8.dp),
                fontSize = 12.sp,
                color = labelColor
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 2.dp, end = 0.dp)
                    .height(50.dp)
                    .clickable { onExpandedChange(true) }
                    .padding(horizontal = 10.dp, vertical = 0.dp)
                    .background(Color.White, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedValue.isEmpty()) "Select $label" else selectedValue,
                        style = TextStyle(fontSize = 12.sp, color = Color.Black),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(getOptionLabel(option)) },
                        onClick = {
                            onValueChange(getOptionLabel(option))
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}


