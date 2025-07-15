package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.model.order.CustomOrderItem
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.Customer
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.model.order.Payment
import com.loyalstring.rfid.data.model.order.URDPurchase
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.GradientButtonIcon
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.OrderViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OrderScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    userPreferences: UserPreferences,

    ) {
    val bulkViewModel: BulkViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    val context = LocalContext.current
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    var selectedCustomer by remember { mutableStateOf<EmployeeList?>(null) }
    val itemCodeList by orderViewModel.itemCodeResponse.collectAsState()
    orderViewModel.deleteAllOrders()

    employee?.clientCode?.let { code ->
        orderViewModel.getAllItemCodeList(
            ClientCodeRequest(
                code
            )
        )
    }


    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            orderViewModel.getAllItemCodeList(ClientCodeRequest(it))
        }
    }


    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            orderViewModel.getAllBranchList(ClientCodeRequest(it))
        }
    }

    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            singleProductViewModel.getAllPurity(ClientCodeRequest(it))
        }
    }


    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Customer Order",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
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
        },

        ) { innerPadding -> // <- Apply padding here
        Box(modifier = Modifier.padding(innerPadding)) {
            OrderScreenContent(
                //itemCode,
                // selectedItem,
                navController,
                itemCodeList,
                userPreferences,
                bulkViewModel,
                selectedCustomer = selectedCustomer,
                onCustomerSelected = { selectedCustomer = it })
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun OrderScreenContent(

//    itemCode: TextFieldValue,
    //  selectedItem: ItemCodeResponse?,
    navController: NavHostController,
    itemCodeList: List<ItemCodeResponse>,
    userPreferences: UserPreferences,
    bulkViewModel: BulkViewModel,
    selectedCustomer: EmployeeList?,
    onCustomerSelected: (EmployeeList) -> Unit
) {
    val context = LocalContext.current
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)

    var totalAMt by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf("") }
    var gstApplied by remember { mutableStateOf("") }
    var totalNetAmt by remember { mutableStateOf("") }
    var totalGstAmt by remember { mutableStateOf("") }
    var totalPupaseAmt by remember { mutableStateOf("") }
    var totalStoneAmt by remember { mutableStateOf("") }
    var totalStoneWt by remember { mutableStateOf("") }
    var totalDiamondAMt by remember { mutableStateOf("") }
    var totalDiamondWt by remember { mutableStateOf("") }
    var totalNetWt by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<ItemCodeResponse?>(null) }
    val orderViewModel: OrderViewModel = hiltViewModel()
    var customerName by remember { mutableStateOf(TextFieldValue("")) }
    var customerId by remember { mutableStateOf<Int?>(null) }
    var itemCode by remember { mutableStateOf(TextFieldValue("")) }
    val lastOrder by orderViewModel.lastOrderNoresponse.collectAsState()
    val orderSuccess by orderViewModel.orderResponse.collectAsState()
    var firstPress by remember { mutableStateOf(false) }
    val isLoading by orderViewModel.isItemCodeLoading.collectAsState()

    val filteredList by remember(itemCode.text, itemCodeList, isLoading) {
        derivedStateOf {
            if (itemCode.text.isBlank() || itemCodeList.isEmpty() || isLoading) {
                emptyList()
            } else {
                itemCodeList.filter {
                    val query = itemCode.text.trim()
                    //it.ItemCode?.contains(itemCode.text.trim(), ignoreCase = true) == true
                    it.ItemCode?.contains(query, ignoreCase = true) == true ||
                            it.RFIDCode?.contains(query, ignoreCase = true) == true

                }
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) } // Control dialog visibility
    var customerNameadd by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val addEmpResponse by orderViewModel.addEmpReposnes.observeAsState()
    var expandedCity by remember { mutableStateOf(false) }
    var expandedState by remember { mutableStateOf(false) }
    var expandedCountry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bulkViewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            bulkViewModel.onBarcodeScanned(scanned)
            bulkViewModel.setRfidForAllTags(scanned)
            //  itemCode = TextFieldValue(scanned)
            selectedItem = if (selectedItem?.RFIDCode == scanned) {
                selectedItem // Keep the same item
            } else {
                null // Clear selection
            }

        }
    }

    var refreshKey by remember { mutableStateOf(0) }

    val cityOptions = listOf(
        "Ahmedabad", "Bengaluru", "Chandigarh", "Chennai", "Delhi", "Hyderabad",
        "Jaipur", "Kolkata", "Lucknow", "Mumbai", "Nagpur", "Pune", "Surat", "Vadodara",
        "Bhopal", "Indore", "Coimbatore", "Patna", "Kochi", "Vijayawada", "Agra", "Faridabad",
        "Ghaziabad", "Chennai", "Visakhapatnam", "Rajkot", "Kanpur", "Noida", "Madurai",
        "Nashik", "Ludhiana", "Jodhpur", "Gurugram", "Mysuru", "Bhubaneswar", "Dhanbad",
        "Tiruchirappalli", "Solapur", "Jammu", "Srinagar", "Ranchi", "Kolkata", "Kochi",
        "Aurangabad", "Gwalior", "Vijayawada", "Puducherry", "Mangalore", "Bhubaneshwar",
        "Shillong", "Panaji", "Imphal", "Agartala", "Dehradun", "Kota", "Udaipur", "Navi Mumbai"
    )
    val stateOptions = listOf(
        "Andhra Pradesh",
        "Arunachal Pradesh",
        "Assam",
        "Bihar",
        "Chhattisgarh",
        "Goa",
        "Gujarat",
        "Haryana",
        "Himachal Pradesh",
        "Jharkhand",
        "Karnataka",
        "Kerala",
        "Madhya Pradesh",
        "Maharashtra",
        "Manipur",
        "Meghalaya",
        "Mizoram",
        "Nagaland",
        "Odisha",
        "Punjab",
        "Rajasthan",
        "Sikkim",
        "Tamil Nadu",
        "Telangana",
        "Tripura",
        "Uttar Pradesh",
        "Uttarakhand",
        "West Bengal"
    )

    var showInvoice by remember { mutableStateOf(false) }

    //  var showInvoice by remember { mutableStateOf(false) }
// Step 1: Set response and flag
    LaunchedEffect(orderSuccess) {
        if (orderSuccess != null) {
            orderViewModel.setOrderResponse(orderSuccess!!)
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
            generateInvoicePdfAndOpen(context, orderSuccess!!, employee)
            showInvoice = true
        }
    }

    val items by bulkViewModel.scannedItems.collectAsState()
    var isGstChecked by remember { mutableStateOf(false) }

    var totalAmount by remember { mutableStateOf("0.000") }
    val stateorder by orderViewModel.lastOrderNoresponse.collectAsState()
    var order by remember { mutableStateOf("0.000") }

    val scanTrigger by bulkViewModel.scanTrigger.collectAsState()
    var showOrderDialog by remember { mutableStateOf(false) }
    var selectedOrderItemForDialog by remember { mutableStateOf<OrderItem?>(null) }
    var showEditOrderDialog by remember { mutableStateOf(false) }
    val tags by bulkViewModel.scannedTags.collectAsState()

    // Function to handle dialog confirm action
    val onConfirmOrderDetails: (String) -> Unit = { orderDetails ->
        // Handle the order details here
        Log.d("OrderDetails", "Order Details Confirmed: $orderDetails")
    }
    val onConfirmOrderDetailsData: (String) -> Unit = { orderDetails ->
        // Handle the order details here
        Log.d("OrderDetails", "Order Details Confirmed: $orderDetails")
    }

    val productList by orderViewModel.allOrderItems.collectAsState()
    var orderSelectedItem by remember { mutableStateOf<OrderItem?>(null) }

    LaunchedEffect(tags) {
        if (tags.isNotEmpty()) {
            Log.d("RFID", "Tags list: ${tags.map { it.epc }}")
            // Iterate through all scanned EPCs in the `tags` list
            tags.forEachIndexed { index, tag ->
                Log.d("Order Screen", "Scanning tag ${index + 1}: ${tag.epc}")

                // Check if EPC exists before processing
                tag.epc?.let { scannedEpc ->
                    Log.d("Scanned EPC", "Processing EPC: $scannedEpc")

                    // Find the matched item based on TID from itemCodeList
                    val matchedItem = itemCodeList.find { item ->
                        item.TIDNumber.equals(
                            scannedEpc,
                            ignoreCase = true
                        ) // Match based on TID
                    }

                    if (matchedItem != null) {
                        Log.d("Match Found", "Item: ${matchedItem.ItemCode}")

                        // Check if the product already exists in the productList based on TID
                        val existingProduct = productList.find { product ->
                            product.tid == matchedItem.TIDNumber // Match based on TID
                        }

                        if (existingProduct == null) {
                            // If the product doesn't exist, create a new product
                            selectedItem = matchedItem
                            val baseUrl =
                                "https://rrgold.loyalstring.co.in/" // Base URL for images
                            val imageString = selectedItem?.Images.toString()
                            val lastImagePath =
                                imageString.split(",").lastOrNull()?.trim()
                            val fullImageUrl = "$baseUrl$lastImagePath"

                            val netWt: Double = (selectedItem?.GrossWt?.toDoubleOrNull()
                                ?: 0.0) - (selectedItem?.TotalStoneWeight?.toDoubleOrNull() ?: 0.0)

                            val finePercent = selectedItem?.FinePercent?.toDoubleOrNull() ?: 0.0
                            val wastagePercent =
                                selectedItem?.WastagePercent?.toDoubleOrNull() ?: 0.0


                            val finewt: Double =
                                ((finePercent / 100.0) * netWt) + ((wastagePercent / 100.0) * netWt)
                            val metalAmt: Double = (selectedItem?.NetWt?.toDoubleOrNull()
                                ?: 0.0) * (selectedItem?.TodaysRate?.toDoubleOrNull() ?: 0.0)

                            val makingPercentage =
                                selectedItem?.MakingPercentage?.toDoubleOrNull() ?: 0.0
                            val fixMaking = selectedItem?.MakingFixedAmt?.toDoubleOrNull() ?: 0.0
                            val extraMakingPercent =
                                selectedItem?.MakingPercentage?.toDoubleOrNull() ?: 0.0
                            val fixWastage =
                                selectedItem?.MakingFixedWastage?.toDoubleOrNull() ?: 0.0

                            val makingAmt: Double =
                                ((makingPercentage / 100.0) * netWt) +
                                        fixMaking +
                                        ((extraMakingPercent / 100.0) * netWt) +
                                        fixWastage

                            val totalStoneAmount =
                                selectedItem?.TotalStoneAmount?.toDoubleOrNull() ?: 0.0
                            val diamondAmount =
                                selectedItem?.DiamondPurchaseAmount?.toDoubleOrNull() ?: 0.0
                            val safeMetalAmt = metalAmt ?: 0.0
                            val safeMakingAmt = makingAmt ?: 0.0

                            val itemAmt: Double =
                                totalStoneAmount + diamondAmount + safeMetalAmt + safeMakingAmt

                            // Create new OrderItem with necessary details
                            val newProduct = OrderItem(
                                branchId = selectedItem?.BranchId.toString(),
                                branchName = selectedItem?.BranchName.toString(),
                                exhibition = "",
                                remark = "",
                                purity = selectedItem?.PurityName.toString(),
                                size = selectedItem?.Size.toString(),
                                length = "",
                                typeOfColor = selectedItem?.Colour.toString(),
                                screwType = "",
                                polishType = "",
                                finePer = selectedItem?.FinePercent.toString(),
                                wastage = selectedItem?.WastagePercent.toString(),
                                orderDate = "",
                                deliverDate = "",
                                productName = selectedItem?.ProductName.toString(),
                                itemCode = selectedItem?.ItemCode.toString(),
                                rfidCode = selectedItem?.RFIDCode.toString(),
                                itemAmt = itemAmt.toString(),
                                grWt = selectedItem?.GrossWt,
                                nWt = selectedItem?.NetWt,
                                stoneAmt = selectedItem?.TotalStoneAmount,
                                finePlusWt = "",
                                packingWt = selectedItem?.PackingWeight.toString(),
                                totalWt = selectedItem?.TotalWeight.toString(),
                                stoneWt = selectedItem?.TotalStoneWeight.toString(),
                                dimondWt = selectedItem?.DiamondWeight.toString(),
                                sku = selectedItem?.SKU.toString(),
                                qty = selectedItem?.ClipQuantity.toString(),
                                hallmarkAmt = selectedItem?.HallmarkAmount.toString(),
                                mrp = selectedItem?.MRP.toString(),
                                image = fullImageUrl.toString(),
                                netAmt = "",
                                diamondAmt = selectedItem?.TotalDiamondAmount.toString(),
                                categoryId = selectedItem?.CategoryId!!,
                                categoryName = selectedItem?.CategoryName!!,
                                productId = selectedItem?.ProductId!!,
                                productCode = selectedItem?.ProductCode!!,
                                skuId = selectedItem?.SKUId!!,
                                designid = selectedItem?.DesignId!!,
                                designName = selectedItem?.DesignName!!,
                                purityid = selectedItem?.PurityId!!,
                                counterId = selectedItem?.CounterId!!,
                                counterName = "",
                                companyId = 0,
                                epc = selectedItem?.TIDNumber!!,
                                tid = selectedItem?.TIDNumber!!,
                                todaysRate = selectedItem?.TodaysRate.toString(),
                                makingPercentage = selectedItem?.MakingPercentage.toString(),
                                makingFixedAmt = selectedItem?.MakingFixedAmt.toString(),
                                makingFixedWastage = selectedItem?.MakingFixedWastage.toString(),
                                makingPerGram = selectedItem?.MakingPerGram.toString()
                            )

                            // Add the new product to the product list (if not added already)
                            // productList.add(newProduct) // Ensure that this line is not commented out

                            Log.d(
                                "Added to Product List",
                                "Product added: ${newProduct.productName}"
                            )

                            // Insert the new product into the database
                            orderViewModel.insertOrderItemToRoom(newProduct)
                        } else {
                            Log.d(
                                "Already Exists",
                                "Product already exists in the list: ${existingProduct.productName}"
                            )
                        }

                    } else {
                        Log.d("No Match", "No item matched with scanned TID")
                    }
                }
            }
        }
    }


    LaunchedEffect(scanTrigger) {
        scanTrigger?.let { type ->
            when (type) {
                "scan" -> if (items.size != 1) bulkViewModel.startScanning(20)
                "barcode" -> bulkViewModel.startBarcodeScanning()
            }
            bulkViewModel.clearScanTrigger()
        }
    }


    // ✅ Set barcode scan callback ONCE
    LaunchedEffect(Unit) {
        bulkViewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            bulkViewModel.onBarcodeScanned(scanned)
            bulkViewModel.setRfidForAllTags(scanned)
            Log.d("RFID Code", scanned)
        }
    }



    val customerSuggestions by orderViewModel.empListResponse.observeAsState(initial = emptyList())
    val countryOptions = listOf("USA", "Canada", "Mexico", "UK", "India")
    var showDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            orderViewModel.getAllEmpList(ClientCodeRequest(it))
        }
    }

    LaunchedEffect(addEmpResponse) {
        when (val response = addEmpResponse) {
            is Resource.Success -> {
                Toast.makeText(context, "Employee added Successfully", Toast.LENGTH_SHORT).show()
                showAddCustomerDialog = false
            }

            is Resource.Error -> {
                Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }


    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Customer Name Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(35.dp)
                        .gradientBorderBox() // Custom modifier for border and gradient
                        .padding(start = 8.dp, end = 4.dp) // Padding for the text field
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // BasicTextField for customer name input
                        BasicTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                expanded = it.text.isNotEmpty() && customerSuggestions.isNotEmpty()
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (customerName.text.isEmpty()) {
                                        Text("Customer Name", fontSize = 13.sp, color = Color.Gray)
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier.weight(1f) // This makes the BasicTextField take most of the space
                        )

                        // Show Add icon when there is no text in the BasicTextField
                        if (customerName.text.isEmpty()) {
                            IconButton(
                                onClick = {
                                    // Show Add Customer dialog or logic for adding customer
                                    Log.d("order screen", "Add button clicked")
                                    customerNameadd = ""
                                    mobileNumber = ""
                                    email = ""
                                    panNumber = ""
                                    gstNumber = ""
                                    street = ""
                                    city = ""
                                    state = ""
                                    country = ""
                                    showAddCustomerDialog = true
                                },
                                modifier = Modifier.size(28.dp) // Add icon size
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.vector_add),
                                    contentDescription = "Add",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified // keeps original colors of the vector
                                )
                            }
                        } else {
                            // Show Cancel icon when there is text in the BasicTextField
                            IconButton(
                                onClick = {
                                    customerName = TextFieldValue("") // Clear input
                                    expanded = false
                                },
                                modifier = Modifier.size(28.dp) // Cancel icon size
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }


                    // Dropdown for customer suggestions
                    if (expanded) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f) // Limit the dropdown width to 90% of the screen
                                .offset(y = 4.dp) // Position the dropdown just below the text field
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp
                                ) // Left and right padding for margins
                        ) {
                            customerSuggestions.forEach { customer ->
                                DropdownMenuItem(onClick = {
                                    customerName = TextFieldValue(customer.FirstName)
                                    customerId = customer.Id
                                    onCustomerSelected(customer)
                                    expanded = false
                                }) {
                                    Text(customer.FirstName)
                                }
                            }
                        }
                    }
                }


            }


            Spacer(modifier = Modifier.height(5.dp))

            // 2. RFID / Itemcode Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(35.dp)
                        .gradientBorderBox() // ⬅️ your custom modifier
                        .padding(start = 8.dp, end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BasicTextField(
                            value = itemCode,
                            onValueChange = {
                                itemCode = it
                                showDropdown = it.text.length >= 1

                            },

                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (itemCode.text.isEmpty()) {
                                        Text(
                                            "Enter RFID / Itemcode",  // Placeholder when no value is entered
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    LaunchedEffect(itemCode.text) {
                                        if (itemCode.text.length >= 1) {
                                            delay(300) // ✅ Now allowed here
                                            Log.d("@@", "first call")
                                            /*employee?.clientCode?.let { code ->
                                                orderViewModel.getAllItemCodeList(
                                                    ClientCodeRequest(
                                                        code
                                                    )
                                                )
                                            }*/
                                            Log.d("@@", "after call")
                                        }
                                    }

                                    innerTextField()
                                }
                            }
                        )

                        if (itemCode.text.isNotEmpty()) {
                            IconButton(
                                onClick = {

                                    // Clear the input when the Cancel icon is clicked
                                    itemCode = TextFieldValue("")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close, // Replace with actual cancel icon
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified // Keeps original colors of the vector
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    // Trigger barcode scanning
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (!Environment.isExternalStorageManager()) {
                                            val intent =
                                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                            intent.data =
                                                Uri.parse("package:" + context.packageName)
                                            context.startActivity(intent)
                                            //return@setOnClickListener
                                        }
                                    }
                                    bulkViewModel.startBarcodeScanning()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.svg_qr),
                                    contentDescription = "Scan",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified // Keeps original colors of the vector
                                )
                            }


                        }

                    }


                }


                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(35.dp)
                        .gradientBorderBox() // ⬅️ your custom modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* Order Details action */ }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order Details", fontSize = 13.sp, color = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.vector_add),
                            contentDescription = "Add",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    // Show the dialog on click
                                    if (validateBeforeShowingDialog(selectedCustomer,productList,context)) {
                                        showOrderDialog = true
                                    }
                                },
                            tint = Color.Unspecified // keeps original colors of the vector
                        )
                    }
                }
            }

            // Show the OrderDetailsDialog when showDialog is true
            if (showOrderDialog) {
                val branchList by orderViewModel.branchResponse.collectAsState()
                OrderDetailsDialog(
                    customerId,
                    selectedCustomer,
                    selectedItem,
                    branchList,
                    onDismiss = { showOrderDialog = false },
                  //  onConfirm = onConfirmOrderDetails,
                    onSave = onConfirmOrderDetails as (OrderDetails) -> Unit

                )
            }
            if (showDropdown) {
                if (isLoading) {
                    // Show loading indicator instead of dropdown
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else if (filteredList.isNotEmpty()) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .offset(y = 4.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        filteredList.forEach { item ->
                            DropdownMenuItem(onClick = {
                                itemCode = TextFieldValue(item.ItemCode.toString())
                                showDropdown = false
                                selectedItem = item

                                val netWt = (item.GrossWt?.toDoubleOrNull() ?: 0.0) -
                                        (item.TotalStoneWeight?.toDoubleOrNull() ?: 0.0)

                                val finePercent = item.FinePercent?.toDoubleOrNull() ?: 0.0
                                val wastagePercent = item.WastagePercent?.toDoubleOrNull() ?: 0.0
                                val finewt =
                                    ((finePercent / 100.0) * netWt) + ((wastagePercent / 100.0) * netWt)

                                val metalAmt = (item.NetWt?.toDoubleOrNull() ?: 0.0) *
                                        (item.TodaysRate?.toDoubleOrNull() ?: 0.0)

                                val makingPercentage =
                                    item.MakingPercentage?.toDoubleOrNull() ?: 0.0
                                val fixMaking = item.MakingFixedAmt?.toDoubleOrNull() ?: 0.0
                                val extraMakingPercent =
                                    item.MakingPercentage?.toDoubleOrNull() ?: 0.0
                                val fixWastage = item.MakingFixedWastage?.toDoubleOrNull() ?: 0.0

                                val makingAmt = ((makingPercentage / 100.0) * netWt) +
                                        fixMaking +
                                        ((extraMakingPercent / 100.0) * netWt) +
                                        fixWastage

                                val totalStoneAmount =
                                    item.TotalStoneAmount?.toDoubleOrNull() ?: 0.0
                                val diamondAmount =
                                    item.DiamondPurchaseAmount?.toDoubleOrNull() ?: 0.0
                                val itemAmt =
                                    totalStoneAmount + diamondAmount + metalAmt + makingAmt

                                val baseUrl = "https://rrgold.loyalstring.co.in/"
                                val imageString = selectedItem?.Images.toString()
                                val lastImagePath = imageString.split(",").lastOrNull()?.trim()
                                val fullImageUrl = "$baseUrl$lastImagePath"

                                val orderItem = OrderItem(
                                    branchId = selectedItem?.BranchId.toString(),
                                    branchName = selectedItem?.BranchName.toString(),
                                    exhibition = "",
                                    remark = "",
                                    purity = selectedItem?.PurityName.toString(),
                                    size = selectedItem?.Size.toString(),
                                    length = "",
                                    typeOfColor = selectedItem?.Colour.toString(),
                                    screwType = "",
                                    polishType = "",
                                    finePer = selectedItem?.FinePercent.toString(),
                                    wastage = selectedItem?.WastagePercent.toString(),
                                    orderDate = "",
                                    deliverDate = "",
                                    productName = selectedItem?.ProductName.toString(),
                                    itemCode = selectedItem?.ItemCode.toString(),
                                    rfidCode = selectedItem?.RFIDCode.toString(),
                                    itemAmt = itemAmt.toString(),
                                    grWt = selectedItem?.GrossWt,
                                    nWt = selectedItem?.NetWt,
                                    stoneAmt = selectedItem?.TotalStoneAmount,
                                    finePlusWt = finewt.toString(),
                                    packingWt = selectedItem?.PackingWeight.toString(),
                                    totalWt = selectedItem?.TotalWeight.toString(),
                                    stoneWt = selectedItem?.TotalStoneWeight.toString(),
                                    dimondWt = selectedItem?.DiamondWeight.toString(),
                                    sku = selectedItem?.SKU.toString(),
                                    qty = selectedItem?.ClipQuantity.toString(),
                                    hallmarkAmt = selectedItem?.HallmarkAmount.toString(),
                                    mrp = selectedItem?.MRP.toString(),
                                    image = fullImageUrl,
                                    netAmt = "",
                                    diamondAmt = selectedItem?.TotalDiamondAmount.toString(),
                                    categoryId = selectedItem?.CategoryId!!,
                                    categoryName = selectedItem?.CategoryName!!,
                                    productId = selectedItem?.ProductId!!,
                                    productCode = selectedItem?.ProductCode!!,
                                    skuId = selectedItem?.SKUId!!,
                                    designid = selectedItem?.DesignId!!,
                                    designName = selectedItem?.DesignName!!,
                                    purityid = selectedItem?.PurityId!!,
                                    counterId = selectedItem?.CounterId!!,
                                    counterName = "",
                                    companyId = 0,
                                    epc = selectedItem?.TIDNumber!!,
                                    tid = selectedItem?.TIDNumber!!,
                                    todaysRate = selectedItem?.TodaysRate.toString(),
                                    makingPercentage = selectedItem?.MakingPercentage.toString(),
                                    makingFixedAmt = selectedItem?.MakingFixedAmt.toString(),
                                    makingFixedWastage = selectedItem?.MakingFixedWastage.toString(),
                                    makingPerGram = selectedItem?.MakingPerGram.toString()
                                )

                                orderViewModel.insertOrderItemToRoom(orderItem)
                                refreshKey++
                            }) {
                                val query = itemCode.text.trim()
                                val displayCode = when {
                                    item.ItemCode?.contains(
                                        query,
                                        ignoreCase = true
                                    ) == true -> item.ItemCode ?: "N/A"

                                    item.RFIDCode?.contains(
                                        query,
                                        ignoreCase = true
                                    ) == true -> item.RFIDCode ?: "N/A"

                                    else -> "N/A"
                                }
                                Text(text = displayCode, fontSize = 14.sp)
                            }
                        }
                    }

                } else {
                    Log.d("Item list", "No Data")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))



            Spacer(modifier = Modifier.width(8.dp))
            // Header Row

            //  val scrollState = rememberScrollState()
            val scrollState = rememberScrollState()
// HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    // .horizontalScroll(scrollState)
                    .background(Color.DarkGray)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Product Name" to 160.dp,
                    "Item Code" to 80.dp,
                    "Gr. Wt" to 80.dp,
                    "N. Wt" to 80.dp,
                    "F+W Wt" to 80.dp,
                    "S.Amt" to 80.dp,
                    "Item.Amt" to 80.dp,
                    "RFID Code" to 80.dp
                ).forEach { (label, width) ->
                    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = poppins
                        )
                    }
                }
            }

            LaunchedEffect(refreshKey) {
                // Force recomposition
            }

            LaunchedEffect(Unit) {
                orderViewModel.getAllOrderItemsFromRoom()
            }

            // Observe the state flow
            val selectedIndex = remember { mutableStateOf(-1) }
            // DATA ROWS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                // Use same scrollState to align
            ) {
                productList.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .padding(vertical = 2.dp, horizontal = 8.dp)
                            .clickable {
                                selectedOrderItemForDialog = item
                                showEditOrderDialog = true
                                orderSelectedItem = item;
                            },

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .width(160.dp)

                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedIndex.value == index,
                                    onClick = {
                                        selectedIndex.value = index
                                         selectedItem = ItemCodeResponse(
                                            Id = 0,
                                            SKUId = item.skuId,
                                            ProductTitle =item.productName,
                                            ClipWeight = "",
                                            ClipQuantity = "",
                                            ItemCode = item.itemCode ?: "",
                                            HSNCode = "",
                                            Description = "",
                                            ProductCode = item.productCode,
                                            MetalName = "",
                                            CategoryId = item.categoryId,
                                            ProductId = item.productId,
                                            DesignId = item.designid,
                                            PurityId = item.purityid,
                                            Colour = item.typeOfColor ?: "",
                                            Size = item.size ?: "",
                                            WeightCategory = "",
                                            GrossWt = item.grWt ?: "",
                                            NetWt = item.nWt ?: "",
                                            CollectionName = "",
                                            OccassionName = "",
                                            Gender = "",
                                            MakingFixedAmt = item.itemAmt ?: "",
                                            MakingPerGram = item.makingPerGram,
                                            MakingFixedWastage =item.makingFixedWastage,
                                            MakingPercentage = item.makingPercentage,
                                            TotalStoneWeight = item.stoneWt,
                                            TotalStoneAmount = item.stoneAmt ?: "",
                                            TotalStonePieces = "",
                                            TotalDiamondWeight = "",
                                            TotalDiamondPieces = "",
                                            TotalDiamondAmount = "",
                                            Featured = "",
                                            Pieces = "",
                                            HallmarkAmount = "",
                                            HUIDCode = "",
                                            MRP = "",
                                            VendorId = 0,
                                            VendorName = "",
                                            FirmName = "",
                                            BoxId = 0,
                                            TIDNumber = item.tid,
                                            RFIDCode = item.rfidCode ?: "",
                                            FinePercent = item.finePlusWt ?: "",
                                            WastagePercent = item.wastage ?: "",
                                            Images = "",
                                            BlackBeads = "",
                                            Height = "",
                                            Width = "",
                                            OrderedItemId = "",
                                            CuttingGrossWt = "",
                                            CuttingNetWt = "",
                                            MetalRate = "",
                                            LotNumber = "",
                                            DeptId = 0,
                                            PurchaseCost = "",
                                            Margin = "",
                                            BranchName = item.branchName ?: "",
                                            BoxName = "",
                                            EstimatedDays = "",
                                            OfferPrice = "",
                                            Rating = "",
                                            SKU = item.sku,
                                            Ranking = "",
                                            CompanyId = item.companyId,
                                            CounterId = item.counterId,
                                            BranchId = item.branchId?.toIntOrNull() ?: 0,
                                            EmployeeId = 0,
                                            Status = "",
                                            ClientCode = employee?.clientCode,
                                            UpdatedFrom = "",
                                            count = 0,
                                            MetalId = 0,
                                            WarehouseId = 0,
                                            CreatedOn = "",
                                            LastUpdated = "",
                                            TaxId = 0,
                                            TaxPercentage = "",
                                            OtherWeight = "",
                                            PouchWeight = "",
                                            CategoryName = item.categoryName,
                                            PurityName = item.purity ?: "",
                                            TodaysRate = item.todaysRate,
                                            ProductName = item.productName ?: "",
                                            DesignName = item.designName,
                                            DiamondSize = "",
                                            DiamondWeight = "",
                                            DiamondPurchaseRate = "",
                                            DiamondSellRate = "",
                                            DiamondClarity = "",
                                            DiamondColour = "",
                                            DiamondShape = "",
                                            DiamondCut = "",
                                            DiamondSettingType = "",
                                            DiamondCertificate = "",
                                            DiamondPieces = "",
                                            DiamondPurchaseAmount = "",
                                            DiamondSellAmount = "",
                                            DiamondDescription = "",
                                            TagWeight = "",
                                            FindingWeight = "",
                                            LanyardWeight = "",
                                            PacketId = 0,
                                            PacketName = "",
                                            CollectionId = 0,
                                            CollectionNameSKU = "",
                                            PackingWeight = 0,
                                            TotalWeight = 0.0,
                                            Stones = emptyList(),
                                            Diamonds = emptyList()
                                        )

                                    })



                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = item.productName ?: "-",
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }
                        }


                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.itemCode, fontSize = 13.sp)
                        }
                        /* Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                             Text(item.branchName, fontSize = 13.sp)
                         }*/
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.grWt.toString(), fontSize = 13.sp)
                        }
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.nWt.toString(), fontSize = 13.sp)
                        }
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.finePlusWt.toString(), fontSize = 13.sp)
                        }
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.stoneAmt.toString(), fontSize = 13.sp)
                        }
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.itemAmt.toString(), fontSize = 13.sp)
                        }
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            Text(item.rfidCode.toString(), fontSize = 13.sp)
                        }
                    }
                }
            }



            LazyColumn(
                contentPadding = PaddingValues(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF0F0F0))
            ) {

            }
            val totalGrWt = productList.sumOf { it.grWt?.toDoubleOrNull() ?: 0.0 }
            var totalNetWt = productList.sumOf { it.nWt?.toDoubleOrNull() ?: 0.0 }
            val totalStomeAmt = productList.sumOf { it.stoneAmt?.toDoubleOrNull() ?: 0.0 }
            val totalItemAmt = productList.sumOf { it.itemAmt?.toDoubleOrNull() ?: 0.0 }
            val totalQty = productList.size


            var gst by remember { mutableStateOf("") }
            var gstApplied by remember { mutableStateOf("") }
            var totalNetAmt by remember { mutableStateOf("") }
            var totalGstAmt by remember { mutableStateOf("") }
            var totalPupaseAmt by remember { mutableStateOf("") }
            var totalDiamondAMt by remember { mutableStateOf("") }



            totalAMt = productList.sumOf { it.itemAmt?.toDoubleOrNull() ?: 0.0 }.toString()
            quantity = productList.size.toString()
            totalStoneAmt = productList.sumOf { it.stoneAmt?.toDoubleOrNull() ?: 0.0 }.toString()
            //  totalNetWt=productList.sumOf { it.nWt?.toDoubleOrNull() ?: 0.0 }.toString()
            totalStoneWt = productList.sumOf { it.stoneWt?.toDoubleOrNull() ?: 0.0 }.toString()
            totalDiamondWt = productList.sumOf { it.dimondWt?.toDoubleOrNull() ?: 0.0 }.toString()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .background(Color.DarkGray)
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Total" to 160.dp,
                    "$totalQty" to 80.dp, // item code column
                    String.format("%.3f", totalGrWt) to 80.dp,
                    String.format("%.3f", totalNetWt) to 80.dp,
                    "" to 80.dp, // F+W Wt
                    "$totalStomeAmt" to 80.dp, // Stone Amt
                    "$totalItemAmt" to 80.dp, // Item Amt
                    "" to 80.dp // RFID Code or Quantity column
                ).forEach { (text, width) ->
                    Box(
                        modifier = Modifier.width(width),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = poppins
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(12.dp))
            GstRowView(
                gstPercent = 3.0, // optional because of default value
                totalAmount = totalAMt, // required
                onTotalAmountChange = { totalAMt = it }, // required
                isGstChecked = isGstChecked, // optional but you're overriding it
                onGstCheckedChange = { isGstChecked = it } // optional but you're overriding it
            )
            Spacer(modifier = Modifier.height(12.dp))
            val coroutineScope = rememberCoroutineScope()
            ScanBottomBar(
                onSave = run@{

                    if (selectedCustomer == null) {
                        Toast.makeText(context, "Please select a customer.", Toast.LENGTH_SHORT).show()
                        return@run
                    }

                    if (productList.isEmpty()) {
                        Toast.makeText(context, "Please add at least one product.", Toast.LENGTH_SHORT).show()
                        return@run
                    }
                    val cientcodereq = ClientCodeRequest(employee?.clientCode.toString())
                    orderViewModel.fetchLastOrderNo(cientcodereq)
                    val nextOrderNo = lastOrder.LastOrderNo.toIntOrNull()?.plus(1) ?: 1

                    Log.d(
                        "@@", "nextOrderNo" + nextOrderNo
                    )

                    coroutineScope.launch {

                        val gstApplied = "true" // or get this from checkbox/input
                        val gstPercent = 3.0

                        val taxableAmt = totalAMt.toDoubleOrNull() ?: 0.0
                        val isGstApplied: Boolean


                        val gstAmt: Double
                        val calculatedTotalAmount: Double

                        if (gstApplied == "true") {
                            gstAmt = taxableAmt * gstPercent / 100
                            calculatedTotalAmount = taxableAmt + gstAmt
                            isGstApplied = true
                        } else {
                            gstAmt = 0.0
                            calculatedTotalAmount = taxableAmt
                            isGstApplied = false
                        }
                        val request = CustomOrderRequest(
                            CustomOrderId = 0,
                            CustomerId = selectedCustomer?.Id.toString(),
                            ClientCode = employee?.clientCode.orEmpty(),
                            OrderId = 14,
                            TotalAmount = calculatedTotalAmount.toString(),
                            PaymentMode = "",
                            Offer = null,
                            Qty = quantity,
                            GST = "",
                            OrderStatus = "",
                            MRP = "",
                            VendorId = 12,
                            TDS = null,
                            PurchaseStatus = null,
                            GSTApplied = isGstApplied.toString(),
                            Discount = "",
                            TotalNetAmount = totalNetAmt,
                            TotalGSTAmount = gstAmt.toString(),
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
                            OrderNo = nextOrderNo.toString(),
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
                            TotalStoneWeight = totalStoneWt,
                            TotalStoneAmount = totalStoneAmt,
                            TotalStonePieces = "3",
                            TotalDiamondWeight = totalDiamondWt,
                            TotalDiamondPieces = "2",
                            TotalDiamondAmount = totalDiamondAMt,
                            FineSilver = "0",
                            FineGold = "5.0",
                            DebitSilver = null,
                            DebitGold = null,
                            PaidMetal = "0.0",
                            PaidAmount = "",
                            TotalAdvanceAmt = null,
                            TaxableAmount = "",
                            TDSAmount = null,
                            CreatedOn = "2025-07-08",
                         //   LastUpdated = "2025-07-08",
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

                            CustomOrderItem = productList.map { product ->
                                CustomOrderItem(
                                    CustomOrderId = 0,
                                    // OrderDate = product.orderDate,
                                   // DeliverDate = product.deliverDate,
                                    SKUId = 0,
                                    SKU = product.sku,
                                    CategoryId = product.categoryId,
                                    VendorId = 0,
                                    CategoryName = product.categoryName,
                                    CustomerName = selectedCustomer?.FirstName,
                                    VendorName = "",
                                    ProductId = product.productId,
                                    ProductName = product.productName,
                                    DesignId = product.designid,
                                    DesignName = product.designName,
                                    PurityId = product.purityid,
                                    PurityName = product.purity,
                                    GrossWt = product.grWt.toString(),
                                    StoneWt = product.stoneWt,
                                    DiamondWt = product.dimondWt,
                                    NetWt = "",
                                    Size = product.size,
                                    Length = product.length,
                                    TypesOdColors = product.typeOfColor,
                                    Quantity = product.qty,
                                    RatePerGram = "",
                                    MakingPerGram = "",
                                    MakingFixed = "",
                                    FixedWt = "",
                                    MakingPercentage = "",
                                    DiamondPieces = "",
                                    DiamondRate = "",
                                    DiamondAmount = product.diamondAmt,
                                    StoneAmount = product.stoneAmt.toString(),
                                    ScrewType = product.screwType,
                                    Polish = product.polishType,
                                    Rhodium = "",
                                    SampleWt = "",
                                    Image = product.image,
                                    ItemCode = product.itemCode,
                                    CustomerId = selectedCustomer?.Id ?: 0,
                                    MRP = product.mrp,
                                    HSNCode = "",
                                    UnlProductId = 0,
                                    OrderBy = "",
                                    StoneLessPercent = "",
                                    ProductCode = product.productCode,
                                    TotalWt = product.totalWt,
                                    BillType = "",
                                    FinePercentage = product.finePer,
                                    ClientCode = employee?.clientCode,
                                    OrderId = "",
                                    // CreatedOn = "",
                                    // LastUpdated = "",
                                    StatusType = true,
                                    PackingWeight = product.packingWt,
                                    MetalAmount = "",
                                    OldGoldPurchase = true,
                                    Amount = product.itemAmt.toString(),
                                    totalGstAmount = "",
                                    finalPrice = product.itemAmt.toString(),
                                    MakingFixedWastage = "",
                                    Description = product.remark,
                                    CompanyId = 0,
                                    LabelledStockId = 0,
                                    TotalStoneWeight = product.stoneWt,
                                    BranchId = 0,
                                    BranchName = product.branchName,
                                    Exhibition = product.exhibition,
                                    CounterId = product.counterId,
                                    EmployeeId = 0,
                                    OrderNo = nextOrderNo.toString(),
                                    OrderStatus = "",
                                    DueDate = "",
                                    Remark = product.remark,
                                    Id = product.id,
                                    PurchaseInvoiceNo = "",
                                    Purity = product.purity,
                                    Status = "",
                                    URDNo = "",
                                    Stones = emptyList(),
                                    Diamond = emptyList()
                                )
                            },

                            Payments = listOf(Payment("")),
                            uRDPurchases = listOf(URDPurchase("")),
                            Customer = Customer(
                                FirstName = selectedCustomer?.FirstName.orEmpty(),
                                LastName = selectedCustomer?.LastName.orEmpty(),
                                PerAddStreet = "",
                                CurrAddStreet = "",
                                Mobile = selectedCustomer?.Mobile.orEmpty(),
                                Email = selectedCustomer?.Email.orEmpty(),
                                Password = "",
                                CustomerLoginId = selectedCustomer?.Email.orEmpty(),
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
                                CreatedOn = "2025-07-08",
                                LastUpdated = "2025-07-08",
                                StatusType = true
                            )
                        )

                        orderViewModel.addOrderCustomer(request)
                    }
                },
                onList = {},
                onScan = {
                    bulkViewModel.startSingleScan(20) { tag ->
                        tag.epc?.let {
                            Log.d("Scanned EPC", it)

                            // Find the product that matches the scanned TID from itemList
                            val matchedItem = itemCodeList.find { item ->
                                item.TIDNumber.equals(it, ignoreCase = true) // Match based on TID
                            }

                            if (matchedItem != null) {
                                Log.d("Match Found", "Item: ${matchedItem.ItemCode}")

                                // Check if the product already exists in the database based on TID (or SKU)
                                val existingProduct = productList.find { product ->
                                    product.tid == matchedItem.TIDNumber // Match based on TID
                                }

                                if (existingProduct == null) {
                                    selectedItem = matchedItem
                                    val netWt: Double = (selectedItem?.GrossWt?.toDoubleOrNull()
                                        ?: 0.0) - (selectedItem?.TotalStoneWeight?.toDoubleOrNull()
                                        ?: 0.0)

                                    val finePercent =
                                        selectedItem?.FinePercent?.toDoubleOrNull() ?: 0.0
                                    val wastagePercent =
                                        selectedItem?.WastagePercent?.toDoubleOrNull() ?: 0.0


                                    val finewt: Double =
                                        ((finePercent / 100.0) * netWt) + ((wastagePercent / 100.0) * netWt)
                                    val metalAmt: Double = (selectedItem?.NetWt?.toDoubleOrNull()
                                        ?: 0.0) * (selectedItem?.TodaysRate?.toDoubleOrNull()
                                        ?: 0.0)

                                    val makingPercentage =
                                        selectedItem?.MakingPercentage?.toDoubleOrNull() ?: 0.0
                                    val fixMaking =
                                        selectedItem?.MakingFixedAmt?.toDoubleOrNull() ?: 0.0
                                    val extraMakingPercent =
                                        selectedItem?.MakingPercentage?.toDoubleOrNull() ?: 0.0
                                    val fixWastage =
                                        selectedItem?.MakingFixedWastage?.toDoubleOrNull() ?: 0.0

                                    val makingAmt: Double =
                                        ((makingPercentage / 100.0) * netWt) +
                                                fixMaking +
                                                ((extraMakingPercent / 100.0) * netWt) +
                                                fixWastage

                                    val totalStoneAmount =
                                        selectedItem?.TotalStoneAmount?.toDoubleOrNull() ?: 0.0
                                    val diamondAmount =
                                        selectedItem?.DiamondPurchaseAmount?.toDoubleOrNull() ?: 0.0
                                    val safeMetalAmt = metalAmt ?: 0.0
                                    val safeMakingAmt = makingAmt ?: 0.0

                                    val itemAmt: Double =
                                        totalStoneAmount + diamondAmount + safeMetalAmt + safeMakingAmt

                                    val baseUrl =
                                        "https://rrgold.loyalstring.co.in/" // Replace with actual base URL
                                    val imageString = selectedItem?.Images.toString()
                                    val lastImagePath = imageString.split(",").lastOrNull()?.trim()
                                    val fullImageUrl = "$baseUrl$lastImagePath"
                                    // If the product doesn't exist in productList, add it and insert into database
                                    val newProduct = OrderItem(
                                        branchId = selectedItem?.BranchId.toString(),
                                        branchName = selectedItem?.BranchName.toString(),
                                        exhibition = "",
                                        remark = "",
                                        purity = selectedItem?.PurityName.toString(),
                                        size = selectedItem?.Size.toString(),
                                        length = "",
                                        typeOfColor = selectedItem?.Colour.toString(),
                                        screwType = "",
                                        polishType = "",
                                        finePer = selectedItem?.FinePercent.toString(),
                                        wastage = selectedItem?.WastagePercent.toString(),
                                        orderDate = "",
                                        deliverDate = "",
                                        productName = selectedItem?.ProductName.toString(),
                                        itemCode = selectedItem?.ItemCode.toString(),
                                        rfidCode = selectedItem?.RFIDCode.toString(),
                                        itemAmt = itemAmt.toString(),
                                        grWt = selectedItem?.GrossWt,
                                        nWt = selectedItem?.NetWt,
                                        stoneAmt = selectedItem?.TotalStoneAmount,
                                        finePlusWt = "",
                                        packingWt = selectedItem?.PackingWeight.toString(),
                                        totalWt = selectedItem?.TotalWeight.toString(),
                                        stoneWt = selectedItem?.TotalStoneWeight.toString(),
                                        dimondWt = selectedItem?.DiamondWeight.toString(),
                                        sku = selectedItem?.SKU.toString(),
                                        qty = selectedItem?.ClipQuantity.toString(),
                                        hallmarkAmt = selectedItem?.HallmarkAmount.toString(),
                                        mrp = selectedItem?.MRP.toString(),
                                        image = fullImageUrl.toString(),
                                        netAmt = "",
                                        diamondAmt = selectedItem?.TotalDiamondAmount.toString(),
                                        categoryId = selectedItem?.CategoryId!!,
                                        categoryName = selectedItem?.CategoryName!!,
                                        productId = selectedItem?.ProductId!!,
                                        productCode = selectedItem?.ProductCode!!,
                                        skuId = selectedItem?.SKUId!!,
                                        designid = selectedItem?.DesignId!!,
                                        designName = selectedItem?.DesignName!!,
                                        purityid = selectedItem?.PurityId!!,
                                        counterId = selectedItem?.CounterId!!,
                                        counterName = "",
                                        companyId = 0,
                                        epc = selectedItem?.TIDNumber!!,
                                        tid = selectedItem?.TIDNumber!!,
                                        todaysRate = selectedItem?.TodaysRate.toString(),
                                        makingPercentage = selectedItem?.MakingPercentage.toString(),
                                        makingFixedAmt = selectedItem?.MakingFixedAmt.toString(),
                                        makingFixedWastage = selectedItem?.MakingFixedWastage.toString(),
                                        makingPerGram = selectedItem?.MakingPerGram.toString()


                                    )
                                    //   productList.add(newProduct) // Add to productList if it doesn't already exist
                                    Log.d(
                                        "Added to Product List",
                                        "Product added: ${newProduct.productName}"
                                    )

                                    // Insert the new product into the database
                                    orderViewModel.insertOrderItemToRoom(newProduct)
                                } else {
                                    Log.d(
                                        "Already Exists",
                                        "Product already exists in the list: ${existingProduct.productName}"
                                    )
                                }

                            } else {
                                Log.d("No Match", "No item matched with scanned TID")
                            }
                        }
                    }
                },
                onGscan = {
                    if (!firstPress) {
                        firstPress = true
                        bulkViewModel.startScanning(20)

                    } else {
                        bulkViewModel.stopScanning() // Stop scanning after the first press
                    }
                },
                onReset = {
                    firstPress = false
                    bulkViewModel.resetData()
                    bulkViewModel.stopBarcodeScanner()
                }
            )

        }


    }
    if (showEditOrderDialog) {
        val branchList by orderViewModel.branchResponse.collectAsState()
        OrderDetailsDialogEditAndDisplay(
            customerId,
            selectedCustomer,
            orderSelectedItem,
            branchList,
            onDismiss = { showEditOrderDialog = false },
            //  onConfirm = onConfirmOrderDetails,
            onSave = onConfirmOrderDetailsData as (OrderDetailsData) -> Unit

        )
    }

    // Show the dialog when the customer name field is empty
    if (showAddCustomerDialog) {

        fun isValidEmail(email: String): Boolean {
            val emailRegex =
                "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
            return email.matches(emailRegex.toRegex())
        }

        fun isValidPhoneNumber(phone: String): Boolean {
            val phoneRegex = "^[0-9]{10}$"  // Only digits, 10 characters
            return phone.matches(phoneRegex.toRegex())
        }

        fun isValidPan(pan: String): Boolean {
            val panRegex = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$" // PAN format (ABCDE1234F)
            return pan.matches(panRegex.toRegex())
        }

        fun isValidGstNumber(gst: String): Boolean {
            val gstRegex =
                "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}[A-Z]{1}[0-9]{1}$" // GST format
            return gst.matches(gstRegex.toRegex())
        }
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Background dim
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(8.dp)
                        ) // Rounded corners for the whole container
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)) // Ensure everything has rounded corners
                    ) {
                        // Title Section (Header) - Toolbar-like header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.DarkGray)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp
                                    )
                                ) // Rounded top corners for the header
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ) // Padding around the header area
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd, // Icon before text
                                contentDescription = "Add Customer",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White // Icon color
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                            Text(
                                text = "Customer Profile",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {

                                // Customer Name
                                BasicTextField(
                                    value = customerNameadd,
                                    onValueChange = { customerNameadd = it },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    decorationBox = { innerTextField ->
                                        if (customerNameadd.isEmpty()) {
                                            Text(
                                                text = "Customer Name",
                                                style = TextStyle(
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.height(5.dp))

                                // Mobile Number
                                BasicTextField(
                                    value = mobileNumber,
                                    onValueChange = { newValue ->
                                        // Ensure only digits are entered and limit input to 10 characters
                                        if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                                            mobileNumber = newValue
                                        }
                                    },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    decorationBox = { innerTextField ->
                                        if (mobileNumber.isEmpty()) {
                                            Text(
                                                text = "Mobile No",
                                                style = TextStyle(
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.height(5.dp))

                                // Email
                                BasicTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    decorationBox = { innerTextField ->
                                        if (email.isEmpty()) {
                                            Text(
                                                text = "Email",
                                                style = TextStyle(
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                // PAN Number
                                BasicTextField(
                                    value = panNumber,
                                    onValueChange = { panNumber = it },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    decorationBox = { innerTextField ->
                                        if (panNumber.isEmpty()) {
                                            Text(
                                                text = "PAN Number",
                                                style = TextStyle(
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.height(5.dp))

                                // GST Number
                                BasicTextField(
                                    value = gstNumber,
                                    onValueChange = { gstNumber = it },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    decorationBox = { innerTextField ->
                                        if (gstNumber.isEmpty()) {
                                            Text(
                                                text = "GST Number",
                                                style = TextStyle(
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.height(5.dp))

                                // Street
                                BasicTextField(
                                    value = street,
                                    onValueChange = { street = it },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp),
                                    decorationBox = { innerTextField ->
                                        if (street.isEmpty()) {
                                            Text(
                                                text = "Street",
                                                style = TextStyle(
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.height(5.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    Box(modifier = Modifier.weight(1f)) {
                                        // Column to hold the text field and dropdown menu
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Country Input Field (BasicTextField)
                                            BasicTextField(
                                                value = country,
                                                onValueChange = {
                                                    country = it
                                                    expandedCountry =
                                                        it.isNotEmpty() && countryOptions.any { option ->
                                                            option.contains(it, ignoreCase = true)
                                                        }
                                                },
                                                textStyle = TextStyle(fontSize = 16.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.Gray.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                decorationBox = { innerTextField ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            if (country.isEmpty()) {
                                                                Text(
                                                                    "Country",
                                                                    color = Color.Gray,
                                                                    fontSize = 14.sp
                                                                )
                                                            }
                                                            innerTextField()
                                                        }

                                                        if (country.isNotEmpty()) {
                                                            IconButton(
                                                                onClick = {
                                                                    country = ""; expandedCountry =
                                                                    false
                                                                },
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Clear Country",
                                                                    tint = Color.Gray
                                                                )
                                                            }
                                                        } else {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowDropDown,
                                                                contentDescription = "Dropdown Icon",
                                                                tint = Color.Gray
                                                            )
                                                        }
                                                    }
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(8.dp)) // Space between text field and dropdown

                                            // Country Dropdown (Position it below the BasicTextField)
                                            if (expandedCountry) {
                                                DropdownMenu(
                                                    expanded = expandedCountry,
                                                    onDismissRequest = { expandedCountry = false },
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.7f)  // Limit the dropdown width to 90% of the screen
                                                        .padding(start = 16.dp, end = 16.dp)

                                                        .align(Alignment.Start) // Align the dropdown to the start (left side)
                                                        .offset(y = 4.dp) // Position the dropdown just below the text field
                                                ) {
                                                    // Filter the country options based on the input value
                                                    countryOptions.filter {
                                                        it.contains(
                                                            country,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                        .forEach { suggestion ->
                                                            DropdownMenuItem(onClick = {
                                                                country = suggestion
                                                                expandedCountry = false
                                                            }) {
                                                                Text(suggestion, fontSize = 14.sp)
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                    }


                                    // State Input
                                    Box(modifier = Modifier.weight(1f)) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // State Input Field (BasicTextField)
                                            BasicTextField(
                                                value = state,
                                                onValueChange = {
                                                    state = it
                                                    expandedState =
                                                        it.isNotEmpty() && stateOptions.any { option ->
                                                            option.contains(it, ignoreCase = true)
                                                        }
                                                },
                                                textStyle = TextStyle(fontSize = 16.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color.Gray.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                decorationBox = { innerTextField ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            if (state.isEmpty()) {
                                                                Text(
                                                                    "State",
                                                                    color = Color.Gray,
                                                                    fontSize = 14.sp
                                                                )
                                                            }
                                                            innerTextField()
                                                        }

                                                        if (state.isNotEmpty()) {
                                                            IconButton(
                                                                onClick = {
                                                                    state = ""; expandedState =
                                                                    false
                                                                },
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Clear State",
                                                                    tint = Color.Gray
                                                                )
                                                            }
                                                        } else {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowDropDown,
                                                                contentDescription = "Dropdown Icon",
                                                                tint = Color.Gray
                                                            )
                                                        }
                                                    }
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(8.dp)) // Space between text field and dropdown

                                            // State Dropdown (Position it below the BasicTextField)
                                            if (expandedState) {
                                                DropdownMenu(
                                                    expanded = expandedState,
                                                    onDismissRequest = { expandedState = false },
                                                    modifier = Modifier
                                                        .padding(start = 16.dp, end = 16.dp)
                                                        .fillMaxWidth(0.7f)  // Limit the dropdown width to 90% of the screen
                                                        .align(Alignment.Start) // Align the dropdown to the start (left side)
                                                        .offset(
                                                            y = 4.dp,
                                                            x = 4.dp
                                                        ) // Position the dropdown just below the text field
                                                ) {
                                                    // Filter the state options based on the input value
                                                    stateOptions.filter {
                                                        it.contains(
                                                            state,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                        .forEach { suggestion ->
                                                            DropdownMenuItem(onClick = {
                                                                state = suggestion
                                                                expandedState = false
                                                            }) {
                                                                Text(suggestion, fontSize = 14.sp)
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                    }

                                }
                                // City Input Field with Dropdown
                                Box {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // State Input Field (BasicTextField)
                                        BasicTextField(
                                            value = city,
                                            onValueChange = {
                                                city = it
                                                expandedCity =
                                                    it.isNotEmpty() && cityOptions.any { option ->
                                                        option.contains(it, ignoreCase = true)
                                                    }
                                            },
                                            textStyle = TextStyle(fontSize = 16.sp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color.Gray.copy(alpha = 0.1f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
                                            decorationBox = { innerTextField ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        if (city.isEmpty()) {
                                                            Text(
                                                                "City",
                                                                color = Color.Gray,
                                                                fontSize = 14.sp
                                                            )
                                                        }
                                                        innerTextField()
                                                    }

                                                    if (city.isNotEmpty()) {
                                                        IconButton(
                                                            onClick = {
                                                                city = ""; expandedCity = false
                                                            },
                                                            modifier = Modifier.size(20.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Clear City",
                                                                tint = Color.Gray
                                                            )
                                                        }
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.ArrowDropDown,
                                                            contentDescription = "Dropdown Icon",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp)) // Space between text field and dropdown

                                        // State Dropdown (Position it below the BasicTextField)
                                        if (expandedCity) {
                                            DropdownMenu(
                                                expanded = expandedCity,
                                                onDismissRequest = { expandedCity = false },
                                                modifier = Modifier
                                                    .padding(start = 16.dp, end = 16.dp)
                                                    .fillMaxWidth(0.7f)  // Limit the dropdown width to 90% of the screen
                                                    .align(Alignment.Start) // Align the dropdown to the start (left side)
                                                    .offset(
                                                        y = 4.dp,
                                                        x = 4.dp
                                                    ) // Position the dropdown just below the text field
                                            ) {
                                                // Filter the state options based on the input value
                                                cityOptions.filter {
                                                    it.contains(
                                                        city,
                                                        ignoreCase = true
                                                    )
                                                }
                                                    .forEach { suggestion ->
                                                        DropdownMenuItem(onClick = {
                                                            city = suggestion
                                                            expandedCity = false
                                                        }) {
                                                            Text(suggestion, fontSize = 14.sp)
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(5.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    GradientButtonIcon(
                                        text = "Cancel",
                                        onClick = {
                                            println("Form Reset")
                                            showAddCustomerDialog = false
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
                                        onClick = {
                                            // Validate fields
                                            val selectedCountry = country
                                            val selectedState = state
                                            val selectedCity = city

                                            // Debug logs for the input values
                                            Log.d(
                                                "@@",
                                                "customerNameadd: $customerNameadd, email: $email, mobileNumber: $mobileNumber, panNumber: $panNumber"
                                            )

                                            when {

                                                customerNameadd.isEmpty() -> {
                                                    errorMessage = "Name cannot be empty"
                                                    Log.d("@@", "Name is empty")
                                                    Toast.makeText(
                                                        context,
                                                        "Name is empty",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                !isValidPhoneNumber(mobileNumber) -> {
                                                    errorMessage = "Phone number must be 10 digits"
                                                    Log.d("@@", "Invalid phone number")
                                                    Toast.makeText(
                                                        context,
                                                        "Invalid phone number",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                email.isNotEmpty() && !isValidEmail(email) -> {
                                                    errorMessage =
                                                        "Please enter a valid email address"
                                                    Log.d("@@", "Invalid email")
                                                    Toast.makeText(
                                                        context,
                                                        "Invalid email",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                panNumber.isNotEmpty() && !isValidPan(panNumber) -> {
                                                    errorMessage = "Invalid PAN number"
                                                    Log.d("@@", "Invalid PAN")
                                                    Toast.makeText(
                                                        context,
                                                        "Invalid PAN",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                gstNumber.isNotEmpty() && !isValidGstNumber(
                                                    gstNumber
                                                ) -> {
                                                    errorMessage = "Invalid GST number"
                                                    Log.d("@@", "Invalid GST")
                                                    Toast.makeText(
                                                        context,
                                                        "Invalid GST",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                else -> {
                                                    // Clear any previous errors
                                                    errorMessage = ""

                                                    val addEmployee = AddEmployeeRequest(
                                                        customerNameadd,
                                                        "",
                                                        "",
                                                        email,
                                                        "",
                                                        "",
                                                        "",
                                                        0,
                                                        0,
                                                        0,
                                                        mobileNumber,
                                                        "Active",
                                                        "",
                                                        "0",
                                                        "0",
                                                        street,
                                                        "",
                                                        "",
                                                        selectedCity,
                                                        selectedState,
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        selectedCountry,
                                                        "",
                                                        "",
                                                        "0",
                                                        "0",
                                                        panNumber,
                                                        "0",
                                                        "0",
                                                        gstNumber,
                                                        employee?.clientCode,
                                                        0,
                                                        "",
                                                        false,
                                                        employee?.employeeId?.toString(),
                                                    )


                                                    // Log the request object
                                                    Log.d("@@", "AddEmployee: $addEmployee")

                                                    // Call the ViewModel to add the employee
                                                    orderViewModel.addEmployee(addEmployee)

                                                }
                                            }
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


        }

    }
}

fun validateBeforeShowingDialog(
    selectedCustomer: EmployeeList?,
    productList: List<OrderItem>,
    context: Context
): Boolean {
    return when {
        selectedCustomer == null -> {
            Toast.makeText(context, "Please select a customer.", Toast.LENGTH_SHORT).show()
            false
        }
        productList.isEmpty() -> {
            Toast.makeText(context, "Please add at least one product.", Toast.LENGTH_SHORT).show()
            false
        }
        else -> true
    }
}


fun generateInvoicePdfAndOpen(context: Context, order: CustomOrderResponse, employee: Employee?) {
    val document = PdfDocument()
    val paint = Paint()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    var y = 40
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Proforma Invoice", 220f, y.toFloat(), paint)

    y += 50
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText("Date: ${order.OrderDate}", 20f, y.toFloat(), paint)
    canvas.drawText("KT: 18KT", 450f, y.toFloat(), paint)
    y += 20
    canvas.drawText(
        "Client Name: ${order.Customer?.FirstName ?: ""} ${order.Customer?.LastName ?: ""}",
        20f,
        y.toFloat(),
        paint
    )
    canvas.drawText("Screw: 88NS", 450f, y.toFloat(), paint)
    y += 20
    canvas.drawText("Separate Tags: YES", 20f, y.toFloat(), paint)
    canvas.drawText("Wastage: 0.0", 450f, y.toFloat(), paint)

    y += 30

    // Header Setup - tighter column spacing
    val headers =
        listOf("SNO", "TAG", "ITEM", "DESIGN", "STAMP", "GWT", "SWT", "NWT", "FINE", "STN VAL")
    val colX = listOf(10, 50, 100, 150, 245, 295, 345, 395, 445, 500)
    val colWidth = listOf(40, 50, 50, 95, 50, 50, 50, 50, 55, 85)
    val rowHeight = 22

    paint.textSize = 9f
    paint.isFakeBoldText = true

    for (i in headers.indices) {
        val left = colX[i].toFloat()
        val top = y.toFloat()
        val right = (colX[i] + colWidth[i]).toFloat()
        val bottom = (y + rowHeight).toFloat()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(left, top, right, bottom, paint)

        paint.style = Paint.Style.FILL
        canvas.drawText(headers[i], left + 2f, bottom - 6f, paint)
    }

    y += rowHeight
    paint.isFakeBoldText = false

    for ((index, item) in order.CustomOrderItem.withIndex()) {
        if (y > 750) break

        val netWeight =
            (item.GrossWt?.toDoubleOrNull() ?: 0.0) - (item.StoneWt?.toDoubleOrNull() ?: 0.0)
        val row = listOf(
            "${index + 1}",
            item.ItemCode ?: "",
            item.SKU ?: "",
            item.DesignName ?: "",
            item.Purity ?: "",
            item.GrossWt ?: "0.000",
            item.StoneWt ?: "0.000",
            "%.3f".format(netWeight),
            item.FinePercentage ?: "0.000",
            item.StoneAmount ?: "0.000"
        )

        for (i in row.indices) {
            val left = colX[i].toFloat()
            val top = y.toFloat()
            val right = (colX[i] + colWidth[i]).toFloat()
            val bottom = (y + rowHeight).toFloat()

            paint.style = Paint.Style.STROKE
            canvas.drawRect(left, top, right, bottom, paint)

            paint.style = Paint.Style.FILL
            canvas.drawText(row[i], left + 2f, bottom - 6f, paint)
        }

        y += rowHeight
    }

    // Totals
    val totalGross = order.CustomOrderItem.sumOf { it.GrossWt?.toDoubleOrNull() ?: 0.0 }
    val totalStone = order.CustomOrderItem.sumOf { it.StoneWt?.toDoubleOrNull() ?: 0.0 }
    val totalNet = totalGross - totalStone
    val totalFine = order.CustomOrderItem.sumOf { it.FinePercentage?.toDoubleOrNull() ?: 0.0 }
    val totalStnValue = order.CustomOrderItem.sumOf { it.StoneAmount?.toDoubleOrNull() ?: 0.0 }

    val totalRow = listOf(
        "TOTAL", "", "", "", "",
        "%.3f".format(totalGross),
        "%.3f".format(totalStone),
        "%.3f".format(totalNet),
        "%.3f".format(totalFine),
        "%.3f".format(totalStnValue)
    )

    for (i in totalRow.indices) {
        val left = colX[i].toFloat()
        val top = y.toFloat()
        val right = (colX[i] + colWidth[i]).toFloat()
        val bottom = (y + rowHeight).toFloat()

        paint.style = Paint.Style.STROKE
        canvas.drawRect(left, top, right, bottom, paint)

        paint.style = Paint.Style.FILL
        canvas.drawText(totalRow[i], left + 2f, bottom - 6f, paint)
    }

    y += rowHeight + 50
    paint.isFakeBoldText = true


    canvas.drawText(employee?.clients!!?.organisationName.toString(), 20f, y.toFloat(), paint)
    y += 15
    paint.isFakeBoldText = false
    canvas.drawText(
        "ADDRESS - " + employee?.clients!!?.streetAddress.toString() + " , " + employee?.clients!!?.city.toString() + " - " + employee?.clients!!?.postalCode.toString(),
        20f,
        y.toFloat(),
        paint
    )
    y += 15
    canvas.drawText("GST - " + employee?.clients!!?.gstNo.toString(), 20f, y.toFloat(), paint)
    y += 15
    //  canvas.drawText("BANK NAME - "+employee?.clients!!?..toString(), 20f, y.toFloat(), paint)
    //y += 15
    //  canvas.drawText("A/C - "+employee?.clients!!?.ban.toString()+ ""+ "| IFSC - ICIC0000650", 20f, y.toFloat(), paint)
    // y += 15
    //  paint.color = Color.RED
    canvas.drawText("Note - This is not a Tax Invoice", 20f, y.toFloat(), paint)

    document.finishPage(page)

    try {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "Invoice_${System.currentTimeMillis()}.pdf"
        )
        document.writeTo(FileOutputStream(file))
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Open PDF with..."))
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun GstRowView(
    gstPercent: Double = 3.0,
    totalAmount: String,
    onTotalAmountChange: (String) -> Unit,
    isGstChecked: Boolean = false,
    onGstCheckedChange: (Boolean) -> Unit = {}
) {
    // Calculate GST-adjusted total
    val baseAmount = totalAmount.toDoubleOrNull() ?: 0.0
    val finalAmount = if (isGstChecked) {
        baseAmount + (baseAmount * gstPercent / 100)
    } else {
        baseAmount
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // GST Checkbox and label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 2.dp, vertical = 0.dp)
        ) {
            Checkbox(
                checked = isGstChecked,
                onCheckedChange = onGstCheckedChange
            )

            Text(
                text = "GST ${gstPercent}%",
                fontSize = 12.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(40.dp))

        // Total Amount Section
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Amount",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )

            BasicTextField(
                value = "%.2f".format(finalAmount),
                onValueChange = { newText ->
                    // Accept only digits and dot
                    if (newText.all { it.isDigit() || it == '.' }) {
                        onTotalAmountChange(newText)
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (totalAmount.isEmpty()) {
                            Text("₹ 0", color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}


fun Modifier.gradientBorderBox(
    borderRadius: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    gradientColors: List<Color> = listOf(Color(0xFF3053F0), Color(0xFFE82E5A))
): Modifier {
    return this
        .border(
            width = borderWidth,
            brush = Brush.horizontalGradient(gradientColors),
            shape = RoundedCornerShape(borderRadius)
        )
        .clip(RoundedCornerShape(borderRadius))
}



