package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.google.gson.Gson
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.local.entity.Product
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.model.order.CustomOrderItem
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.Customer
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.model.order.Payment
import com.loyalstring.rfid.data.model.order.URDPurchase
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.GradientButtonIcon
import com.loyalstring.rfid.ui.utils.NetworkUtils
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.OrderViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import com.loyalstring.rfid.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OrderScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    userPreferences: UserPreferences
) {

    val context = LocalContext.current
    val employee =
        remember { UserPreferences.getInstance(context).getEmployee(Employee::class.java) }

    val bulkViewModel: BulkViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    var selectedPower by remember { mutableStateOf(10) }

    var selectedCustomer by remember { mutableStateOf<EmployeeList?>(null) }

    val itemCodeList by orderViewModel.itemCodeResponse.collectAsState()
    val customerSuggestions by orderViewModel.empListFlow.collectAsState(UiState.Loading)

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        employee?.clientCode?.let { clientCode ->
            orderViewModel.getAllEmpList(clientCode)
            orderViewModel.getAllItemCodeList(ClientCodeRequest(clientCode))
            singleProductViewModel.getAllBranches(ClientCodeRequest(clientCode))
            singleProductViewModel.getAllPurity(ClientCodeRequest(clientCode))
            singleProductViewModel.getAllSKU(ClientCodeRequest(clientCode))
        }
    }

    LaunchedEffect(customerSuggestions) {
        if (customerSuggestions is UiState.Success) {
            val data = (customerSuggestions as UiState.Success<List<EmployeeList>>).data
            Log.d("CustomerList", Gson().toJson(data))
        }
    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Customer Order",
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
                showCounter = true,
                selectedCount = selectedPower,
                onCountSelected = {
                    selectedPower = it

                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            OrderScreenContent(
                navController = navController,
                itemCodeList = itemCodeList,
                userPreferences = userPreferences,
                bulkViewModel = bulkViewModel,
                selectedCustomer = selectedCustomer,
                onCustomerSelected = { selectedCustomer = it },
                selectedPower
            )
        }
    }
}

@Composable
fun OrderScreenContent(
    navController: NavHostController,
    itemCodeList: List<ItemCodeResponse>,
    userPreferences: UserPreferences,
    bulkViewModel: BulkViewModel,
    selectedCustomer: EmployeeList?,
    onCustomerSelected: (EmployeeList) -> Unit,
    selectedPower: Int
) {
    val context = LocalContext.current
    val isOnline = remember {
        NetworkUtils.isNetworkAvailable(context)
    }

// Retrieve logged-in employee from preferences
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)

// ViewModels
    val orderViewModel: OrderViewModel = hiltViewModel()
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()

// Basic state fields for totals, calculations, item selections
    var selectedItem by remember { mutableStateOf<ItemCodeResponse?>(null) }

    var orderSelectedItem by remember { mutableStateOf<OrderItem?>(null) }
    var firstPress by remember { mutableStateOf(false) }
    var isGstChecked by remember { mutableStateOf(false) }
    var totalAmount by remember { mutableStateOf("0.000") }
    var order by remember { mutableStateOf("0.000") }
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
    var totalGrWt by remember { mutableStateOf("") }

// Customer input fields
    var customerName by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf<Int?>(null) }
    var itemCode by remember { mutableStateOf(TextFieldValue("")) }

// Collecting states from ViewModel
    val isLoading by orderViewModel.isItemCodeLoading.collectAsState()
    val isLoadingEmp by orderViewModel.isEmpListLoading.collectAsState()
    val lastOrder by orderViewModel.lastOrderNoresponse.collectAsState()
    val orderSuccess by orderViewModel.orderResponse.collectAsState()
    val items by bulkViewModel.scannedItems.collectAsState()
    val tags by bulkViewModel.scannedTags.collectAsState()
    val scanTrigger by bulkViewModel.scanTrigger.collectAsState()
    val productList by orderViewModel.allOrderItems.collectAsState()
    val stateorder by orderViewModel.lastOrderNoresponse.collectAsState()
    val orderRequest by orderViewModel.insertOrderOffline.collectAsState()



// Trigger for refreshing components like dropdowns
    var refreshKey by remember { mutableStateOf(0) }

// Scroll and coroutine scope
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

// Handle Order Confirmation
    val onConfirmOrderDetails: (String) -> Unit = { orderDetails ->
        Log.d("OrderDetails", "Order Details Confirmed: $orderDetails")
    }
    val onConfirmOrderDetailsData: (String) -> Unit = { orderDetails ->
        Log.d("OrderDetails", "Order Details Confirmed: $orderDetails")
    }

// --------------------------
// Customer Suggestions Logic
// --------------------------

    val customerSuggestions by orderViewModel.empListFlow.collectAsState(UiState.Loading)
/*
    val filteredCustomers = remember(customerName, customerSuggestions) {
        when (customerSuggestions) {
            is UiState.Success<*> -> {
                val items = (customerSuggestions as UiState.Success<Any?>)!!.data as List<EmployeeList>
                items.filter {
                    val fullName = "${it.FirstName} ${it.LastName}".trim().lowercase()
                    fullName.contains(customerName.trim().lowercase())
                }
            }

            else -> emptyList()
        }
    }*/

    val filteredCustomers = remember(customerName, customerSuggestions) {
        when (customerSuggestions) {
            is UiState.Success<*> -> {
                val items = (customerSuggestions as UiState.Success<Any?>).data as List<EmployeeList>
                items.filter {
                    val fullName = "${it.FirstName} ${it.LastName}".trim().lowercase()
                    fullName.contains(customerName.trim().lowercase())
                }.take(20) // ✅ LIMIT to 20
            }
            else -> emptyList()
        }
    }

    LaunchedEffect(customerSuggestions) {
        if (customerSuggestions is UiState.Success) {
            val data = (customerSuggestions as UiState.Success<List<EmployeeList>>).data
            Log.d("CustomerList", Gson().toJson(data))

        }
    }

// ---------------------------
// Customer Add Dialog Control
// ---------------------------
    var showAddCustomerDialog by remember { mutableStateOf(false) }
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

// Dropdown lists for address
    val cityOptions = listOf(
        "Ahmedabad", "Bengaluru", "Chandigarh", "Chennai", "Delhi", "Hyderabad",
        "Jaipur", "Kolkata", "Lucknow", "Mumbai", "Nagpur", "Pune", "Surat", "Vadodara",
        "Bhopal", "Indore", "Coimbatore", "Patna", "Kochi", "Vijayawada", "Agra", "Faridabad",
        "Ghaziabad", "Visakhapatnam", "Rajkot", "Kanpur", "Noida", "Madurai", "Nashik",
        "Ludhiana", "Jodhpur", "Gurugram", "Mysuru", "Bhubaneswar", "Dhanbad",
        "Tiruchirappalli", "Solapur", "Jammu", "Srinagar", "Ranchi", "Aurangabad",
        "Gwalior", "Puducherry", "Mangalore", "Shillong", "Panaji", "Imphal",
        "Agartala", "Dehradun", "Kota", "Udaipur", "Navi Mumbai"
    )

    val stateOptions = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
        "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
        "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana",
        "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )

    val countryOptions = listOf("USA", "Canada", "Mexico", "UK", "India")
    var expandedCustomer by remember { mutableStateOf(false) }
    var showDropdownItemcode by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var showEditOrderDialog by remember { mutableStateOf(false) }


// -------------------------
// Invoice Generation Trigger
// -------------------------

    var showInvoice by remember { mutableStateOf(false) }


    LaunchedEffect(orderRequest) {

        /* Log.d("orderRequest", Gson().toJson(orderRequest))
         Toast.makeText(context, "Customer order added successfully", Toast.LENGTH_SHORT).show()
         generateInvoicePdfAndOpen(context, it, employee)
         showInvoice = true*/

        orderRequest?.let {
            val orderResponse = it.toCustomOrderResponse()
            orderViewModel.setOrderResponse(orderResponse)

            orderViewModel.setOrderResponse(orderResponse)
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
            generateInvoicePdfAndOpen(context, orderResponse, employee,itemCodeList)
            showInvoice = true
            orderViewModel.clearOrderItems()
            /* itemCode.text=""
             customerName.text=""*/
        }


    }

    LaunchedEffect(orderSuccess) {
        orderSuccess?.let {
            orderViewModel.setOrderResponse(it)
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
            generateInvoicePdfAndOpen(context, it, employee,itemCodeList)
            showInvoice = true
            orderViewModel.clearOrderItems()
            /* itemCode.text=""
             customerName.text=""*/

        }
    }

    LaunchedEffect(addEmpResponse) {
        if (addEmpResponse != null) {
            Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT).show()
        }
    }


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
                            Log.d("existingProduct", "Item: ${matchedItem.ItemCode}")
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
                            val safeMetalAmt = metalAmt
                            val safeMakingAmt = makingAmt

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
                                categoryId = selectedItem?.CategoryId?.toString(),
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
                "scan" -> if (items.size != 1) bulkViewModel.startScanning(30)
                "barcode" -> bulkViewModel.startBarcodeScanning()
            }
            bulkViewModel.clearScanTrigger()
        }
    }


    // ✅ Set barcode scan callback ONCE
    // ✅ This is where you reactively compute matchedItem
    val matchedItem by remember(itemCode, itemCodeList) {
        derivedStateOf {

            itemCodeList.find { it.RFIDCode == itemCode.text }
        }
    }

// ✅ Automatically update selectedItem whenever matchedItem changes
    LaunchedEffect(matchedItem) {
        selectedItem = matchedItem
        if (itemCode.text.isNotEmpty()) {

            val baseUrl =
                "https://rrgold.loyalstring.co.in/" // Base URL for images
            val imageString = selectedItem?.Images.toString()
            val lastImagePath =
                imageString.split(",").lastOrNull()?.trim()
            val fullImageUrl = "$baseUrl$lastImagePath"
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
                itemAmt = "",
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
                categoryId = selectedItem?.CategoryId?.toString(),

                categoryName = selectedItem?.CategoryName ?: "",
                productId = selectedItem?.ProductId ?: 0,
                productCode = selectedItem?.ProductCode ?: "",
                skuId = selectedItem?.SKUId ?: 0,
                designid = selectedItem?.DesignId ?: 0,
                designName = selectedItem?.DesignName ?: "",
                purityid = selectedItem?.PurityId ?: 0,
                counterId = selectedItem?.CounterId ?: 0,
                counterName = "",
                companyId = 0,
                epc = selectedItem?.TIDNumber ?: "",
                tid = selectedItem?.TIDNumber ?: "",
                todaysRate = selectedItem?.TodaysRate?.toString() ?: "0",
                makingPercentage = selectedItem?.MakingPercentage?.toString() ?: "0",
                makingFixedAmt = selectedItem?.MakingFixedAmt?.toString() ?: "0",
                makingFixedWastage = selectedItem?.MakingFixedWastage?.toString() ?: "0",
                makingPerGram = selectedItem?.MakingPerGram?.toString() ?: "0"


            )
            //   productList.add(newProduct) // Add to productList if it doesn't already exist
            Log.d(
                "Added to Product List",
                "Product added: ${newProduct.productName}"
            )

            // Insert the new product into the database
            orderViewModel.insertOrderItemToRoom(newProduct)
        }

    }

// ✅ This is your barcode scanner logic
    LaunchedEffect(Unit) {
        bulkViewModel.barcodeReader.openIfNeeded()
        bulkViewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            bulkViewModel.onBarcodeScanned(scanned)
            bulkViewModel.setRfidForAllTags(scanned)
            Log.d("RFID Code", scanned)
            itemCode = TextFieldValue(scanned) // triggers recomposition
        }
    }
    var nextOrderNo = remember { mutableStateOf(0) }
    LaunchedEffect(lastOrder) {
        lastOrder.LastOrderNo?.toIntOrNull()?.let { last ->
            nextOrderNo.value = last + 1
            Log.d("Order", "Last order number: $last")
            Log.d("Order", "Next order number: ${nextOrderNo.value}")
        }
    }
    LaunchedEffect(productList) {
        totalStoneAmt = productList.sumOf { it.stoneAmt?.toDoubleOrNull() ?: 0.0 }.toString()
        totalNetAmt = productList.sumOf { it.netAmt.toDoubleOrNull() ?: 0.0 }.toString()
        // totalGstAmt= productList.sumOf { it.to?.toDoubleOrNull() ?: 0.0 }.toString()
        totalPupaseAmt = productList.sumOf { it.itemAmt?.toDoubleOrNull() ?: 0.0 }.toString()
        // totalStoneAmt = productList.sumOf { it.stoneAmt?.toDoubleOrNull() ?: 0.0 }.toString()
        totalStoneWt = productList.sumOf { it.stoneWt.toDoubleOrNull() ?: 0.0 }.toString()
        totalDiamondAMt = productList.sumOf { it.diamondAmt.toDoubleOrNull() ?: 0.0 }.toString()
        totalDiamondWt = productList.sumOf { it.dimondWt.toDoubleOrNull() ?: 0.0 }.toString()
        totalAMt = productList.sumOf { it.itemAmt?.toDoubleOrNull() ?: 0.0 }.toString()
        totalGrWt = productList.sumOf { it.grWt?.toDoubleOrNull() ?: 0.0 }.toString()
        quantity=productList.sumOf { it.qty?.toDoubleOrNull() ?: 0.0 }.toString()
    }

    Scaffold(
        bottomBar = {
            Spacer(modifier = Modifier.height(4.dp))
            ScanBottomBar(
                onSave = run@{
                    bulkViewModel.barcodeReader.close()

                    if (selectedCustomer == null) {
                        Toast.makeText(context, "Please select a customer.", Toast.LENGTH_SHORT)
                            .show()
                        return@run
                    }

                    if (productList.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Please add at least one product.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@run
                    }


                    //val nextOrderNo = lastOrder.LastOrderNo.toIntOrNull()?.plus(1) ?: 1
                    coroutineScope.launch {
                        val clientCode = employee?.clientCode.orEmpty()

                        // Fetch last order number from API
                        val lastOrderResponse = orderViewModel.fetchLastOrderNo(ClientCodeRequest(clientCode))

                        // Parse response safely
                        var attempts = 0
                        var lastOrderNo: Int? = null
                        while (attempts < 10 && lastOrderNo == null) {
                            delay(300)
                            lastOrderNo = orderViewModel.lastOrderNoresponse.value.LastOrderNo?.toIntOrNull()
                            attempts++
                        }

                        val nextOrderNo = (lastOrderNo ?: 0) + 1

                        Log.d("Order", "Fetched Last Order: $lastOrderNo")
                        Log.d("Order", "Next Order Number: $nextOrderNo")

                        if (nextOrderNo == 0) {
                            Toast.makeText(context, "Failed to generate order number.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (nextOrderNo != 0) {


                        val gstPercent = 3.0
                        //val gstApplied = "true"
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
                            CustomerId = selectedCustomer.Id.toString(),
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
                            TaxableAmt = taxableAmt.toString(),
                            GstAmount = gstAmt.toString(),
                            GstCheck = isGstChecked.toString(),
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
                                    CategoryId = product.categoryId?.toString(),
                                    VendorId = 0,
                                    CategoryName = product.categoryName,
                                    CustomerName = selectedCustomer.FirstName,
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
                                    CustomerId = selectedCustomer.Id ?: 0,
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
                                    CounterId = product.counterId.toString(),
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
                                FirstName = selectedCustomer.FirstName.orEmpty(),
                                LastName = selectedCustomer.LastName.orEmpty(),
                                PerAddStreet = "",
                                CurrAddStreet = "",
                                Mobile = selectedCustomer.Mobile.orEmpty(),
                                Email = selectedCustomer.Email.orEmpty(),
                                Password = "",
                                CustomerLoginId = selectedCustomer.Email.orEmpty(),
                                DateOfBirth = "",
                                MiddleName = "",
                                PerAddPincode = "",
                                Gender = "",
                                OnlineStatus = "",
                                CurrAddTown = selectedCustomer.CurrAddTown.orEmpty(),
                                CurrAddPincode = "",
                                CurrAddState = selectedCustomer.CurrAddState.orEmpty(),
                                PerAddTown = "",
                                PerAddState = "",
                                GstNo = selectedCustomer.GstNo.orEmpty(),
                                PanNo = selectedCustomer.PanNo.orEmpty(),
                                AadharNo = "",
                                BalanceAmount = "0",
                                AdvanceAmount = "0",
                                Discount = "0",
                                CreditPeriod = "",
                                FineGold = "0",
                                FineSilver = "0",
                                ClientCode = selectedCustomer.ClientCode.orEmpty(),
                                VendorId = 0,
                                AddToVendor = false,
                                CustomerSlabId = 0,
                                CreditPeriodId = 0,
                                RateOfInterestId = 0,
                                Remark = "",
                                Area = "",
                                City = selectedCustomer.City.orEmpty(),
                                Country = selectedCustomer.Country.orEmpty(),
                                Id = selectedCustomer.Id ?: 0,
                                CreatedOn = "2025-07-08",
                                LastUpdated = "2025-07-08",
                                StatusType = true,

                            )
                        )
                        if (isOnline) {
                            orderViewModel.addOrderCustomer(request)
                        } else {
                            orderViewModel.saveOrder(request)
                        }
                    }
                    }
                },
                onList = {
                    navController.navigate("order_list")
                },
                onScan = {
                   // resetScan(bulkViewModel,firstPress)
                    bulkViewModel.startSingleScan(30) { tag ->
                        tag.epc?.let {
                            Log.d("Scanned EPC", it)

                            // Find the product that matches the scanned TID from itemList
                            val matchedItem = itemCodeList.find { item ->
                                item.TIDNumber.equals(
                                    it,
                                    ignoreCase = true
                                ) // Match based on TID
                            }

                            if (matchedItem != null) {
                                Log.d("Match Found", "Item: ${matchedItem.ItemCode}")

                                // Check if the product already exists in the database based on TID (or SKU)
                                val existingProduct = productList.find { product ->
                                    product.tid == matchedItem.TIDNumber // Match based on TID
                                }

                                if (existingProduct == null) {
                                     selectedItem= matchedItem
                                    val netWt: Double = (selectedItem?.GrossWt?.toDoubleOrNull()
                                        ?: 0.0) - (selectedItem?.TotalStoneWeight?.toDoubleOrNull()
                                        ?: 0.0)

                                    val finePercent =
                                        selectedItem?.FinePercent?.toDoubleOrNull() ?: 0.0
                                    val wastagePercent =
                                        selectedItem?.WastagePercent?.toDoubleOrNull() ?: 0.0


                                    val finewt: Double =
                                        ((finePercent / 100.0) * netWt) + ((wastagePercent / 100.0) * netWt)
                                    val metalAmt: Double =
                                        (selectedItem?.NetWt?.toDoubleOrNull()
                                            ?: 0.0) * (selectedItem?.TodaysRate?.toDoubleOrNull()
                                            ?: 0.0)

                                    val makingPercentage =
                                        selectedItem?.MakingPercentage?.toDoubleOrNull() ?: 0.0
                                    val fixMaking =
                                        selectedItem?.MakingFixedAmt?.toDoubleOrNull() ?: 0.0
                                    val extraMakingPercent =
                                        selectedItem?.MakingPercentage?.toDoubleOrNull() ?: 0.0
                                    val fixWastage =
                                        selectedItem?.MakingFixedWastage?.toDoubleOrNull()
                                            ?: 0.0

                                    val makingAmt: Double =
                                        ((makingPercentage / 100.0) * netWt) +
                                                fixMaking +
                                                ((extraMakingPercent / 100.0) * netWt) +
                                                fixWastage

                                    val totalStoneAmount =
                                        selectedItem?.TotalStoneAmount?.toDoubleOrNull() ?: 0.0
                                    val diamondAmount =
                                        selectedItem?.DiamondPurchaseAmount?.toDoubleOrNull()
                                            ?: 0.0
                                    val safeMetalAmt = metalAmt
                                    val safeMakingAmt = makingAmt

                                    val itemAmt: Double =
                                        totalStoneAmount + diamondAmount + safeMetalAmt + safeMakingAmt

                                    val baseUrl =
                                        "https://rrgold.loyalstring.co.in/" // Replace with actual base URL
                                    val imageString = selectedItem?.Images.toString()
                                    val lastImagePath =
                                        imageString.split(",").lastOrNull()?.trim()
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
                                        categoryId = selectedItem?.CategoryId?.toString(),

                                        categoryName = selectedItem?.CategoryName ?: "",
                                        productId = selectedItem?.ProductId ?: 0,
                                        productCode = selectedItem?.ProductCode ?: "",
                                        skuId = selectedItem?.SKUId ?: 0,
                                        designid = selectedItem?.DesignId ?: 0,
                                        designName = selectedItem?.DesignName ?: "",
                                        purityid = selectedItem?.PurityId ?: 0,
                                        counterId = selectedItem?.CounterId ?: 0,
                                        counterName = "",
                                        companyId = 0,
                                        epc = selectedItem?.TIDNumber ?: "",
                                        tid = selectedItem?.TIDNumber ?: "",
                                        todaysRate = selectedItem?.TodaysRate?.toString() ?: "0",
                                        makingPercentage = selectedItem?.MakingPercentage?.toString() ?: "0",
                                        makingFixedAmt = selectedItem?.MakingFixedAmt?.toString() ?: "0",
                                        makingFixedWastage = selectedItem?.MakingFixedWastage?.toString() ?: "0",
                                        makingPerGram = selectedItem?.MakingPerGram?.toString() ?: "0"



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
                 //   resetScan(bulkViewModel,firstPress)
                    if (!firstPress) {
                        firstPress = true
                        bulkViewModel.startScanning(selectedPower)

                    } else {
                        bulkViewModel.stopScanning() // Stop scanning after the first press
                    }
                },
                onReset = {

                    bulkViewModel.resetData()
                    bulkViewModel.stopBarcodeScanner()
                    orderViewModel.clearOrderItems()


                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            /*CustomerNameInput(
                customerName = customerName,
                onCustomerNameChange = { customerName = it },
                onClear = {
                    customerName = ""
                    expandedCustomer = false
                },
                onAddCustomerClick = {
                    // reset all fields
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
                filteredCustomers = filteredCustomers,
                isLoading = isLoadingEmp,
                onCustomerSelected = {
                    customerName = it.FirstName.toString()
                    customerId = it.Id?.toInt()
                    onCustomerSelected(it)
                },
                coroutineScope = coroutineScope,
                fetchSuggestions = {
                    orderViewModel.getAllEmpList(employee?.clientCode!!)
                },
                expanded = false
            )*/
            val coroutineScope = rememberCoroutineScope()

            CustomerNameInput(
                customerName = customerName,
                onCustomerNameChange = { customerName = it },
                onClear = {
                    customerName = ""
                    expandedCustomer = false
                },
                onAddCustomerClick = {
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
                filteredCustomers = filteredCustomers,
                isLoading = false,
                onCustomerSelected = {
                    customerName = "${it.FirstName.orEmpty()} ${it.LastName.orEmpty()}".trim()
                    customerId = it.Id ?: 0
                    onCustomerSelected(it)
                },
                coroutineScope = coroutineScope, // ✅ Required argument
                fetchSuggestions = {
                 //   orderViewModel.getAllEmpList(employee?.clientCode ?: "")
                },

                expanded = false,

            )



            // Spacer(modifier = Modifier.height(5.dp))

            // 2. RFID / Itemcode Row
            ItemCodeInputRow(
                itemCode = itemCode,
                onItemCodeChange = { itemCode = it },
                showDropdown = showDropdownItemcode,
                setShowDropdown = { showDropdownItemcode = it },
                context = context,
                onScanClicked = { /* scanner logic */ },
                onClearClicked = { itemCode = TextFieldValue("") },
                onAddOrderClicked = {/* showOrderDialog = true*/ },
                validateBeforeShowingDialog = {
                    validateBeforeShowingDialog(selectedCustomer, productList, context)
                },
                filteredList = filteredList,
                isLoading = isLoading,
                onItemSelected = { selectedItem = it },

                saveToDb = {
                    val orderItem = mapItemCodeToOrderItem(it)

                    orderViewModel.insertOrderItemToRoom(orderItem)

                },
                selectedCustomer = selectedCustomer,

                productList = productList,
                customerId = customerId,
                selectedItem = selectedItem
            )
            Spacer(modifier = Modifier.height(4.dp))
            //table row
            OrderItemTableScreen(
                productList = productList,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                showEditOrderDialog = showEditOrderDialog,
                onEditOrderClicked = { item ->
                    // Handle edit logic here
                    showEditOrderDialog = true
                    // e.g., selectedOrderItem.value = item
                },
                employee = employee,
                orderViewModel = orderViewModel,

                refreshKey = refreshKey,
                orderSelectedItem = orderSelectedItem,
                onOrderSelectedItemChange = { orderSelectedItem = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            GstRowView(
                gstPercent = 3.0, // optional because of default value
                totalAmount = totalAMt, // required
                onTotalAmountChange = { totalAMt = it }, // required
                isGstChecked = isGstChecked, // optional but you're overriding it
                onGstCheckedChange = { isGstChecked = it } // optional but you're overriding it
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
    if (showAddCustomerDialog) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .fillMaxWidth(0.95f)
                        .heightIn(min = 300.dp, max = 600.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.DarkGray)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Customer Profile",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            // Dropdown control states
                            var expandedCountry by remember { mutableStateOf(false) }
                            var expandedState by remember { mutableStateOf(false) }
                            var expandedCity by remember { mutableStateOf(false) }

                            @Composable
                            fun textInput(
                                value: String,
                                onChange: (String) -> Unit,
                                label: String,
                                keyboardType: KeyboardType = KeyboardType.Text,
                                maxLength: Int = Int.MAX_VALUE
                            ) {
                                BasicTextField(
                                    value = value,
                                    onValueChange = {
                                        if (it.length <= maxLength) onChange(it)
                                    },
                                    textStyle = TextStyle(fontSize = 16.sp),
                                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    decorationBox = { innerTextField ->
                                        Box(Modifier.fillMaxWidth()) {
                                            if (value.isEmpty()) {
                                                Text(label, color = Color.Gray, fontSize = 14.sp)
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            @Composable
                            fun dropdownInput(
                                value: String,
                                onValueChange: (String) -> Unit,
                                label: String,
                                options: List<String>,
                                expanded: Boolean,
                                onExpandedChange: (Boolean) -> Unit,
                                modifier: Modifier = Modifier
                            ) {
                                Column (modifier = modifier) {
                                    BasicTextField(
                                        value = value,
                                        onValueChange = {
                                            onValueChange(it)
                                            onExpandedChange(true)
                                        },
                                        textStyle = TextStyle(fontSize = 16.sp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color.Gray.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        decorationBox = { innerTextField ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    if (value.isEmpty()) {
                                                        Text(
                                                            label,
                                                            color = Color.Gray,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (value.isNotEmpty()) {
                                                            onValueChange("")
                                                            onExpandedChange(false)
                                                        } else {
                                                            onExpandedChange(true)
                                                        }
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (value.isNotEmpty()) Icons.Default.Close else Icons.Default.ArrowDropDown,
                                                        contentDescription = null,
                                                        tint = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    )

                                    if (expanded) {
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { onExpandedChange(false) }
                                        ) {
                                            options.filter {
                                                it.contains(value, ignoreCase = true)
                                            }.forEach { suggestion ->
                                                DropdownMenuItem(onClick = {
                                                    onValueChange(suggestion)
                                                    onExpandedChange(false)
                                                }) {
                                                    Text(suggestion)
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Text Fields
                            textInput(customerNameadd, { customerNameadd = it }, "Customer Name")
                            textInput(
                                mobileNumber,
                                { if (it.all(Char::isDigit)) mobileNumber = it },
                                "Mobile Number",
                                KeyboardType.Phone,
                                10
                            )
                            textInput(email, { email = it }, "Email")
                            textInput(panNumber, { panNumber = it }, "PAN Number")
                            textInput(gstNumber, { gstNumber = it }, "GST Number")
                            textInput(street, { street = it }, "Street")


                            // Dropdowns
                            Row(modifier = Modifier.fillMaxWidth()) {
                                dropdownInput(
                                    value = country,
                                    onValueChange = { country = it },
                                    label = "Country",
                                    options = countryOptions,
                                    expanded = expandedCountry,
                                    onExpandedChange = { expandedCountry = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                dropdownInput(
                                    value = state,
                                    onValueChange = { state = it },
                                    label = "State",
                                    options = stateOptions,
                                    expanded = expandedState,
                                    onExpandedChange = { expandedState = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            /*  dropdownInput(
                                  country,
                                  { country = it },
                                  "Country",
                                  countryOptions,
                                  expandedCountry,
                                  { expandedCountry = it })
                              dropdownInput(
                                  state,
                                  { state = it },
                                  "State",
                                  stateOptions,
                                  expandedState,
                                  { expandedState = it })*/
                            dropdownInput(
                                city,
                                { city = it },
                                "City",
                                cityOptions,
                                expandedCity,
                                { expandedCity = it },
                                modifier = Modifier.fillMaxWidth())

                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GradientButtonIcon(
                                text = "Cancel",
                                onClick = { showAddCustomerDialog = false },
                                icon = painterResource(id = R.drawable.ic_cancel),
                                iconDescription = "Cancel Icon",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp)
                            )

                            GradientButtonIcon(
                                text = "OK",
                                onClick = {
                                    fun isValidEmail(email: String) =
                                        email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$".toRegex())

                                    fun isValidPhone(phone: String) =
                                        phone.matches("^[0-9]{10}$".toRegex())

                                    fun isValidPan(pan: String) =
                                        pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$".toRegex())

                                    fun isValidGst(gst: String) =
                                        gst.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}[A-Z]{1}[0-9]{1}$".toRegex())

                                    when {
                                        customerNameadd.isEmpty() -> Toast.makeText(
                                            context,
                                            "Enter name",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        /*   !isValidPhone(mobileNumber) -> Toast.makeText(
                                               context,
                                               "Invalid phone",
                                               Toast.LENGTH_SHORT
                                           ).show()*/

                                        email.isNotEmpty() && !isValidEmail(email) -> Toast.makeText(
                                            context,
                                            "Invalid email",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        panNumber.isNotEmpty() && !isValidPan(panNumber) -> Toast.makeText(
                                            context,
                                            "Invalid PAN",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        gstNumber.isNotEmpty() && !isValidGst(gstNumber) -> Toast.makeText(
                                            context,
                                            "Invalid GST",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        else -> {
                                            val request = AddEmployeeRequest(
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
                                                city,
                                                state,
                                                "",
                                                "",
                                                "",
                                                "",
                                                country,
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
                                                employee?.employeeId?.toString()
                                            )
                                            orderViewModel.addEmployee(request)
                                            showAddCustomerDialog = false
                                        }
                                    }
                                },
                                icon = painterResource(id = R.drawable.check_circle),
                                iconDescription = "OK Icon",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditOrderDialog) {
        val branchList = singleProductViewModel.branches
        Log.d(
            "@@ vasanti",
            "custId" + customerId + " , custdata " + selectedCustomer + "   .selecetditem " + selectedItem + " ,branchlist" + branchList
        )
        Log.d("@@", "@@ vasanti,branchlist" + branchList)
        OrderDetailsDialogEditAndDisplay(

            orderSelectedItem,
            branchList,
            onDismiss = { showEditOrderDialog = false },
            //  onConfirm = onConfirmOrderDetails,
            onSave = {
                // handle saved data
                showOrderDialog = false
            },
            edit = 1

        )
    }

}


fun CustomOrderRequest.toCustomOrderResponse(): CustomOrderResponse {
    return CustomOrderResponse(
        CustomOrderId = this.CustomOrderId,
        CustomerId = this.CustomerId.toIntOrNull() ?: 0,
        ClientCode = this.ClientCode,
        OrderId = this.OrderId,
        TotalAmount = this.TotalAmount,
        PaymentMode = this.PaymentMode,
        Offer = this.Offer,
        Qty = this.Qty,
        GST = this.GST,
        OrderStatus = this.OrderStatus,
        MRP = this.MRP,
        VendorId = this.VendorId,
        TDS = this.TDS,
        PurchaseStatus = this.PurchaseStatus,
        GSTApplied = this.GSTApplied,
        Discount = this.Discount,
        TotalNetAmount = this.TotalNetAmount,
        TotalGSTAmount = this.TotalGSTAmount,
        TotalPurchaseAmount = this.TotalPurchaseAmount,
        ReceivedAmount = this.ReceivedAmount,
        TotalBalanceMetal = this.TotalBalanceMetal,
        BalanceAmount = this.BalanceAmount,
        TotalFineMetal = this.TotalFineMetal,
        CourierCharge = this.CourierCharge,
        SaleType = this.SaleType,
        OrderDate = this.OrderDate,
        OrderCount = this.OrderCount,
        AdditionTaxApplied = this.AdditionTaxApplied,
        CategoryId = this.CategoryId,
        OrderNo = this.OrderNo,
        DeliveryAddress = this.DeliveryAddress,
        BillType = this.BillType,
        UrdPurchaseAmt = this.UrdPurchaseAmt,
        BilledBy = this.BilledBy,
        SoldBy = this.SoldBy,
        CreditSilver = this.CreditSilver,
        CreditGold = this.CreditGold,
        CreditAmount = this.CreditAmount,
        BalanceAmt = this.BalanceAmt,
        BalanceSilver = this.BalanceSilver,
        BalanceGold = this.BalanceGold,
        TotalSaleGold = this.TotalSaleGold,
        TotalSaleSilver = this.TotalSaleSilver,
        TotalSaleUrdGold = this.TotalSaleUrdGold,
        TotalSaleUrdSilver = this.TotalSaleUrdSilver,
        FinancialYear = this.FinancialYear,
        BaseCurrency = this.BaseCurrency,
        TotalStoneWeight = this.TotalStoneWeight,
        TotalStoneAmount = this.TotalStoneAmount,
        TotalStonePieces = this.TotalStonePieces,
        TotalDiamondWeight = this.TotalDiamondWeight,
        TotalDiamondPieces = this.TotalDiamondPieces,
        TotalDiamondAmount = this.TotalDiamondAmount,
        FineSilver = this.FineSilver,
        FineGold = this.FineGold,
        DebitSilver = this.DebitSilver,
        DebitGold = this.DebitGold,
        PaidMetal = this.PaidMetal,
        PaidAmount = this.PaidAmount,
        TotalAdvanceAmt = this.TotalAdvanceAmt,
        TaxableAmount = this.TaxableAmount,
        TDSAmount = this.TDSAmount ?: "",
        CreatedOn = this.CreatedOn ?: "",
        LastUpdated = this.LastUpdated ?: "",
        StatusType = this.StatusType ?: true,
        FineMetal = this.FineMetal.toString(),
        BalanceMetal = this.BalanceMetal,
        AdvanceAmt = this.AdvanceAmt,
        PaidAmt = this.PaidAmt,
        TaxableAmt = this.TaxableAmt,
        GstAmount = this.GstAmount,
        GstCheck = this.GstCheck,
        Category = this.Category,

        TDSCheck = this.TDSCheck,
        Remark = this.Remark,
        OrderItemId = this.OrderItemId ?: 0,
        StoneStatus = this.StoneStatus,
        DiamondStatus = this.DiamondStatus,
        BulkOrderId = this.BulkOrderId,
        CustomOrderItem = this.CustomOrderItem,
        Payments = this.Payments,
        Customer = this.Customer,
        syncStatus = this.syncStatus,
        ProductName = "",
    )
}


fun resetScan(model: BulkViewModel, firstPress: Boolean) {
    // Add logic to stop or clear scanning state
    model.resetData()
    model.stopBarcodeScanner()
    model.stopScanning()  // Make sure scanning stops properly
     // Reset any data that might be lingering
  //  firstPress=false;
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
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .heightIn(min = 48.dp), // Ensure enough height for checkbox
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // GST Checkbox Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Checkbox(
                checked = isGstChecked,
                onCheckedChange = onGstCheckedChange
            )

            Text(
                text = "GST ${gstPercent}%",
                fontSize = 14.sp,
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
                            Text("", color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
        }

    }

}

fun mapItemCodeToOrderItem(item: ItemCodeResponse): OrderItem {
    return OrderItem(
        id = 0, // Auto-generated
        branchId = item.BranchId?.toString() ?: "",
        branchName = item.BranchName ?: "",
        exhibition = "", // Not present in ItemCodeResponse
        remark = "", // Not present in ItemCodeResponse
        purity = item.PurityName ?: "",
        size = item.Size ?: "",
        length = "", // Not present in ItemCodeResponse
        typeOfColor = item.Colour ?: "",
        screwType = "", // Not present in ItemCodeResponse
        polishType = "", // Not present in ItemCodeResponse
        finePer = item.FinePercent ?: "",
        wastage = item.WastagePercent ?: "",
        orderDate = "", // To be filled separately (e.g., current date)
        deliverDate = "", // To be filled separately
        productName = item.ProductName ?: "",
        itemCode = item.ItemCode ?: "",
        rfidCode = item.RFIDCode ?: "",
        grWt = item.GrossWt,
        nWt = item.NetWt,
        stoneAmt = item.TotalStoneAmount,
        finePlusWt = item.FinePercent,
        itemAmt = item.MakingFixedAmt,
        packingWt = "", // Not in ItemCodeResponse
        totalWt = "",   // Not in ItemCodeResponse
        stoneWt = item.TotalStoneWeight ?: "",
        dimondWt = item.TotalDiamondWeight ?: "",
        sku = item.SKU ?: "",
        qty = "", // Not in ItemCodeResponse
        hallmarkAmt = item.HallmarkAmount ?: "",
        mrp = item.MRP ?: "",
        image = item.Images ?: "",
        netAmt = "", // To be calculated?
        diamondAmt = item.TotalDiamondAmount ?: "",
        categoryId = item.CategoryId.toString(),
        categoryName = item.CategoryName ?: "",
        productId = item.ProductId ?: 0,
        productCode = item.ProductCode ?: "",
        skuId = item.SKUId ?: 0,
        designid = item.DesignId ?: 0,
        designName = item.DesignName ?: "",
        purityid = item.PurityId ?: 0,
        counterId = item.CounterId ?: 0,
        counterName = "", // Not present
        companyId = item.CompanyId ?: 0,
        epc = "", // Not present
        tid = item.TIDNumber ?: "",
        todaysRate = item.TodaysRate ?: "",
        makingPercentage = item.MakingPercentage ?: "",
        makingFixedAmt = item.MakingFixedAmt ?: "",
        makingFixedWastage = item.MakingFixedWastage ?: "",
        makingPerGram = item.MakingPerGram ?: ""
    )
}

@Composable
fun OrderItemTableScreen(
    productList: List<OrderItem>,
    selectedItem: ItemCodeResponse?,
    onItemSelected: (ItemCodeResponse) -> Unit,
    showEditOrderDialog: Boolean,
    onEditOrderClicked: (OrderItem) -> Unit,
    employee: Employee?,
    orderViewModel: OrderViewModel,
    refreshKey: Int,
    orderSelectedItem: OrderItem?,
    onOrderSelectedItemChange: (OrderItem) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()
    val selectedIndex = remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        orderViewModel.getAllOrderItemsFromRoom()
    }
    val totalGrWt = productList.sumOf { it.grWt?.toDoubleOrNull() ?: 0.0 }
    val totalNetWt = productList.sumOf { it.nWt?.toDoubleOrNull() ?: 0.0 }
    val totalStoneAmt = productList.sumOf { it.stoneAmt?.toDoubleOrNull() ?: 0.0 }
    val totalItemAmt = productList.sumOf { it.itemAmt?.toDoubleOrNull() ?: 0.0 }
    val totalQty = productList.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ✅ Shared horizontal scroll for all content
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .fillMaxWidth()
        ) {
            Column {
                // ✅ Header Row
                Row(
                    modifier = Modifier
                        .background(Color.DarkGray)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "Product Name" to 160.dp,
                        "Item Code" to 80.dp,
                        "Qty" to 80.dp,
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

                // ✅ Data Rows in LazyColumn
                LazyColumn(
                    state = verticalScrollState,
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(productList) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .padding(vertical = 2.dp, horizontal = 8.dp)
                                .clickable {
                                    onEditOrderClicked(item)
                                    onOrderSelectedItemChange(item)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Name + Radio
                            Box(
                                modifier = Modifier.width(160.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedIndex.value == index,
                                        onClick = {
                                            selectedIndex.value = index
                                            onItemSelected(
                                                ItemCodeResponse(
                                                    Id = 0,
                                                    SKUId = item.skuId,
                                                    ProductTitle = item.productName,
                                                    ClipWeight = "",
                                                    ClipQuantity = "",
                                                    ItemCode = item.itemCode,
                                                    HSNCode = "",
                                                    Description = "",
                                                    ProductCode = item.productCode,
                                                    MetalName = "",
                                                    CategoryId = item.categoryId.toString(),
                                                    ProductId = item.productId,
                                                    DesignId = item.designid,
                                                    PurityId = item.purityid,
                                                    Colour = item.typeOfColor,
                                                    Size = item.size,
                                                    WeightCategory = "",
                                                    GrossWt = item.grWt ?: "",
                                                    NetWt = item.nWt ?: "",
                                                    CollectionName = "",
                                                    OccassionName = "",
                                                    Gender = "",
                                                    MakingFixedAmt = item.itemAmt ?: "",
                                                    MakingPerGram = item.makingPerGram,
                                                    MakingFixedWastage = item.makingFixedWastage,
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
                                                    RFIDCode = item.rfidCode,
                                                    FinePercent = item.finePlusWt ?: "",
                                                    WastagePercent = item.wastage,
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
                                                    BranchName = item.branchName,
                                                    BoxName = "",
                                                    EstimatedDays = "",
                                                    OfferPrice = "",
                                                    Rating = "",
                                                    SKU = item.sku,
                                                    Ranking = "",
                                                    CompanyId = item.companyId,
                                                    CounterId = item.counterId,
                                                    BranchId = item.branchId.toIntOrNull() ?: 0,
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
                                                    PurityName = item.purity,
                                                    TodaysRate = item.todaysRate,
                                                    ProductName = item.productName,
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
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        item.productName,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            // Rest of the columns
                            listOf(
                                item.itemCode,
                                item.grWt,
                                item.nWt,
                                item.finePlusWt,
                                item.stoneAmt,
                                item.itemAmt,
                                item.rfidCode,
                                item.qty
                            ).forEach { value ->
                                Box(
                                    modifier = Modifier.width(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(value.toString(), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // ✅ Total Row
                Row(
                    modifier = Modifier
                        .background(Color.DarkGray)
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    listOf(


                        "Total" to 160.dp,
                        "$totalQty" to 80.dp,
                        String.format("%.3f", totalGrWt) to 80.dp,
                        String.format("%.3f", totalNetWt) to 80.dp,
                        "" to 80.dp,
                        "$totalStoneAmt" to 80.dp,
                        "$totalItemAmt" to 80.dp,
                        "" to 80.dp
                    ).forEach { (text, width) ->
                        Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
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
                totalNetWt

            }
        }
    }


}



/*@Composable
fun OrderItemTableScreen(
    productList: List<OrderItem>,
    selectedItem: ItemCodeResponse?,
    onItemSelected: (ItemCodeResponse) -> Unit,
    showEditOrderDialog: Boolean,
    onEditOrderClicked: (OrderItem) -> Unit,
    employee: Employee?,
    orderViewModel: OrderViewModel,
    refreshKey: Int,
    orderSelectedItem: OrderItem?,
    onOrderSelectedItemChange: (OrderItem) -> Unit // ✅ Add this
) {

    val scrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    val selectedIndex = remember { mutableStateOf(-1) }

    var totalAMt by remember { mutableStateOf("") }
    var totalStoneAmt by remember { mutableStateOf("") }
    var totalStoneWt by remember { mutableStateOf("") }
    var totalDiamondWt by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    LaunchedEffect(refreshKey) {
        // Trigger recomposition if needed
    }

    LaunchedEffect(Unit) {
        orderViewModel.getAllOrderItemsFromRoom()
    }

    val totalGrWt = productList.sumOf { it.grWt?.toDoubleOrNull() ?: 0.0 }
    val totalNetWt = productList.sumOf { it.nWt?.toDoubleOrNull() ?: 0.0 }
    val totalStomeAmt = productList.sumOf { it.stoneAmt?.toDoubleOrNull() ?: 0.0 }
    val totalItemAmt = productList.sumOf { it.itemAmt?.toDoubleOrNull() ?: 0.0 }
    val totalQty = productList.size

    totalAMt = totalItemAmt.toString()
    quantity = totalQty.toString()
    totalStoneAmt = totalStomeAmt.toString()
    totalStoneWt = productList.sumOf { it.stoneWt?.toDoubleOrNull() ?: 0.0 }.toString()
    totalDiamondWt = productList.sumOf { it.dimondWt?.toDoubleOrNull() ?: 0.0 }.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {


            // ✅ Scrollable content with weighted height
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                        .horizontalScroll(scrollState)
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

                // Data Rows
                Box(
                    modifier = Modifier
                        .weight(6.5f)
                        .horizontalScroll(scrollState)

                ) {
                    LazyColumn(state = verticalScrollState) {
                        itemsIndexed(productList) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(35.dp)
                                    .padding(vertical = 2.dp, horizontal = 8.dp)
                                    .clickable {
                                        onEditOrderClicked(item)
                                        onOrderSelectedItemChange(item)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product Name + Radio Button
                                Box(
                                    modifier = Modifier.width(160.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = selectedIndex.value == index,
                                            onClick = {
                                                selectedIndex.value = index
                                                onItemSelected(
                                                    ItemCodeResponse(
                                                        Id = 0,
                                                        SKUId = item.skuId,
                                                        ProductTitle = item.productName,
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
                                                        MakingFixedWastage = item.makingFixedWastage,
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
                                                        BranchId = item.branchId?.toIntOrNull()
                                                            ?: 0,
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
                                                )
                                                *//* onItemSelected(
                                                item.toItemCodeResponse(employee)
                                            )*//*
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            item.productName ?: "-",
                                            fontSize = 13.sp,
                                            color = Color.Black
                                        )
                                    }
                                }

                                // Rest of the Columns
                                listOf(
                                    item.itemCode,
                                    item.grWt,
                                    item.nWt,
                                    item.finePlusWt,
                                    item.stoneAmt,
                                    item.itemAmt,
                                    item.rfidCode
                                ).forEach { value ->
                                    Box(
                                        modifier = Modifier.width(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(value.toString(), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Total Row
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
                        "$totalQty" to 80.dp,
                        String.format("%.3f", totalGrWt) to 80.dp,
                        String.format("%.3f", totalNetWt) to 80.dp,
                        "" to 80.dp,
                        "$totalStomeAmt" to 80.dp,
                        "$totalItemAmt" to 80.dp,
                        "" to 80.dp
                    ).forEach { (text, width) ->
                        Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
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
            }

    }
}*/


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

@Composable
fun ItemCodeInputRow(
    itemCode: TextFieldValue,
    onItemCodeChange: (TextFieldValue) -> Unit,
    showDropdown: Boolean,
    setShowDropdown: (Boolean) -> Unit,
    context: Context,
    onScanClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onAddOrderClicked: () -> Unit,
    validateBeforeShowingDialog: () -> Boolean,
    filteredList: List<ItemCodeResponse>,
    isLoading: Boolean,
    onItemSelected: (ItemCodeResponse) -> Unit,
    modifier: Modifier = Modifier,
    saveToDb: (ItemCodeResponse) -> Unit,
    selectedCustomer: EmployeeList?,

    productList: List<OrderItem>,
    customerId: Int?,
    selectedItem: ItemCodeResponse?,
) {
    Spacer(modifier = Modifier.height(5.dp))
    var singleProductViewModel: SingleProductViewModel = hiltViewModel()
    var showOrderDialog by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 10.dp)
    ) {
        // Item Code Input Box
        Box(
            modifier = Modifier
                .weight(1.1f)
                .height(35.dp)
                .gradientBorderBox()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                BasicTextField(
                    value = itemCode,
                    onValueChange = {
                        onItemCodeChange(it)
                        setShowDropdown(it.text.length >= 1)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (itemCode.text.isEmpty()) {
                                Text("Enter RFID / Itemcode", fontSize = 13.sp, color = Color.Gray)
                            }

                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        if (itemCode.text.isNotEmpty()) onClearClicked()
                        else onScanClicked()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    if (itemCode.text.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.svg_qr),
                            contentDescription = "Scan",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }

                if (showDropdown) {
                    when {
                        isLoading -> {
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
                        }

                        filteredList.isNotEmpty() -> {
                            DropdownMenu(
                                expanded = true,

                                onDismissRequest = { setShowDropdown(false) },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .offset(y = 4.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                filteredList.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        onItemCodeChange(TextFieldValue(item.ItemCode.orEmpty()))
                                        onItemSelected(item)
                                        saveToDb(item) // ✅ save  into local
                                        setShowDropdown(false)
                                    }) {
                                        val query = itemCode.text.trim()
                                        val match = when {
                                            item.ItemCode?.contains(
                                                query,
                                                true
                                            ) == true -> item.ItemCode

                                            item.RFIDCode?.contains(
                                                query,
                                                true
                                            ) == true -> item.RFIDCode

                                            else -> "N/A"
                                        }
                                        Text(match.orEmpty(), fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        else -> {
                            Log.d("Item list", "No Data")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Order Details Button
        Box(
            modifier = Modifier
                .weight(0.8f)
                .height(35.dp)
                .gradientBorderBox()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    // Show the dialog on click
                    if (validateBeforeShowingDialog(
                            selectedCustomer,
                            productList,
                            context
                        )
                    ) {
                        showOrderDialog = true

                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order Details", fontSize = 13.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.vector_add),
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
            }
        }
        if (showOrderDialog) {

            val branchList = singleProductViewModel.branches


            // Log.d("@@ vasanti","custId"+customerId+" , custdata "+selectedCustomer +"   .selecetditem "+selectedItem+" ,branchlist"+branchList)
            Log.d("@@", "@@ vasanti,branchlist" + branchList)

            OrderDetailsDialog(
                customerId,
                selectedCustomer,
                selectedItem!!,
                branchList,
                onDismiss = { showOrderDialog = false },

                onSave = {
                    // handle saved data
                    showOrderDialog = false
                }

            )
        }
    }
}


/*@Composable
fun CustomerNameInput(
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    onClear: () -> Unit,
    onAddCustomerClick: () -> Unit,
    filteredCustomers: List<EmployeeList>,
    isLoading: Boolean,
    onCustomerSelected: (EmployeeList) -> Unit,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    fetchSuggestions: suspend () -> Unit,
    expanded: Boolean,
) {
    var expanded by remember { mutableStateOf(expanded) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .height(35.dp)
                .gradientBorderBox() // custom modifier
                .padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                BasicTextField(
                    value = customerName,
                    onValueChange = {
                        onCustomerNameChange(it)
                        expanded = it.length >= 1

                        if (it.length >= 1) {
                            coroutineScope.launch {
                              //  delay(100)
                                fetchSuggestions()
                                expanded = true
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (customerName.isEmpty()) {
                                Text("Customer Name", fontSize = 13.sp, color = Color.Gray)
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (customerName.isEmpty()) onAddCustomerClick()
                        else onClear()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    if (customerName.isEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.vector_add),
                            contentDescription = "Add",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

            }

            // Dropdown Suggestions
            DropdownMenu(
                expanded = expanded && (filteredCustomers.isNotEmpty() || isLoading),
                onDismissRequest = { expanded = expanded },

                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .offset(y = 4.dp)
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    isLoading -> {
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
                    }

                    filteredCustomers.isNotEmpty() -> {
                        filteredCustomers.forEach { customer ->
                            DropdownMenuItem(
                                onClick = {
                                    onCustomerSelected(customer)
                                    expanded = false
                                }
                            ) {
                                Text(text = customer.FirstName.toString())
                            }
                        }
                    }
                }
            }


        }



    }
}*/
@Composable
fun CustomerNameInput(
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    onClear: () -> Unit,
    onAddCustomerClick: () -> Unit,
    filteredCustomers: List<EmployeeList>,
    isLoading: Boolean,
    onCustomerSelected: (EmployeeList) -> Unit,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    fetchSuggestions: suspend () -> Unit,
    expanded: Boolean,
) {
    var localExpanded by remember { mutableStateOf(expanded) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(35.dp)
                    .weight(1f)
                    .gradientBorderBox()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    BasicTextField(
                        value = customerName,
                        onValueChange = {
                            onCustomerNameChange(it)
                            debounceJob?.cancel()
                            if (it.length >= 1) {
                                debounceJob = coroutineScope.launch {
                                    delay(300)
                                    // ✅ REMOVE fetchSuggestions if not needed
                                    try {
                                        fetchSuggestions()
                                        localExpanded = true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        // You can show a Toast/snackbar or log it
                                    }
                                }
                            } else {
                                localExpanded = false
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (customerName.isEmpty()) {
                                    Text("Customer Name", fontSize = 13.sp, color = Color.Gray)
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (customerName.isEmpty()) {
                                onAddCustomerClick()
                            } else {
                                onClear()
                                localExpanded = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        if (customerName.isEmpty()) {
                            Icon(
                                painter = painterResource(id = R.drawable.vector_add),
                                contentDescription = "Add",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ✅ Custom dropdown — do NOT use DropdownMenuItem outside DropdownMenu
        if (localExpanded && (filteredCustomers.isNotEmpty() || isLoading)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else if (filteredCustomers.isNotEmpty()) {
                    filteredCustomers.take(10).forEach { customer ->
                        val fullName = "${customer.FirstName.orEmpty()} ${customer.LastName.orEmpty()}".trim()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCustomerSelected(customer)
                                    onCustomerNameChange(fullName)
                                    localExpanded = false
                                }
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Text(text = fullName)
                        }
                    }
                } else {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text("No customer found")
                    }
                }
            }
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

/*fun generateInvoicePdfAndOpen(context: Context, order: CustomOrderResponse, employee: Employee?) {
    val document = PdfDocument()
    val paint = Paint()

    // Page size: A4 (595x842 pixels)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    var y = 40

    // ---------- Header ----------
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Order Receipt", 220f, y.toFloat(), paint)

    y += 50
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText("Date: ${order.OrderDate}", 20f, y.toFloat(), paint)
    canvas.drawText("KT: 18KT", 450f, y.toFloat(), paint)

    y += 20
    canvas.drawText(
        "Client Name: ${order.Customer?.FirstName.orEmpty()} ${order.Customer?.LastName.orEmpty()}",
        20f,
        y.toFloat(),
        paint
    )
    canvas.drawText("Screw: 88NS", 450f, y.toFloat(), paint)

    y += 20
    canvas.drawText("Separate Tags: YES", 20f, y.toFloat(), paint)
    canvas.drawText("Wastage: 0.0", 450f, y.toFloat(), paint)

    y += 30

    // ---------- Table Header ----------
    val headers =
        listOf("SNO", "TAG", "ITEM", "DESIGN", "STAMP", "GWT", "SWT", "NWT", "FINE", "STN VAL")
    val colX = listOf(10, 50, 100, 150, 245, 295, 345, 395, 445, 500)
    val colWidth = listOf(40, 50, 50, 95, 50, 50, 50, 50, 55, 85)
    val rowHeight = 22

    paint.textSize = 9f
    paint.isFakeBoldText = true

    for (i in headers.indices) {
        val left = colX[i].toFloat()
        val right = (colX[i] + colWidth[i]).toFloat()
        val bottom = (y + rowHeight).toFloat()

        // Draw header cell border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(left, y.toFloat(), right, bottom, paint)

        // Draw header text
        paint.style = Paint.Style.FILL
        canvas.drawText(headers[i], left + 2f, bottom - 6f, paint)
    }

    y += rowHeight
    paint.isFakeBoldText = false

    // ---------- Table Rows ----------
    for ((index, item) in order.CustomOrderItem.withIndex()) {
        if (y > 750) break // Prevent overflow

        val netWeight =
            (item.GrossWt?.toDoubleOrNull() ?: 0.0) - (item.StoneWt?.toDoubleOrNull() ?: 0.0)
        val row = listOf(
            "${index + 1}",
            item.ItemCode.orEmpty(),
            item.SKU.orEmpty(),
            item.DesignName.orEmpty(),
            item.Purity.orEmpty(),
            item.GrossWt ?: "0.000",
            item.StoneWt ?: "0.000",
            "%.3f".format(netWeight),
            item.FinePercentage ?: "0.000",
            item.StoneAmount ?: "0.000"
        )

        for (i in row.indices) {
            val left = colX[i].toFloat()
            val right = (colX[i] + colWidth[i]).toFloat()
            val bottom = (y + rowHeight).toFloat()

            // Cell border
            paint.style = Paint.Style.STROKE
            canvas.drawRect(left, y.toFloat(), right, bottom, paint)

            // Cell text
            paint.style = Paint.Style.FILL
            canvas.drawText(row[i], left + 2f, bottom - 6f, paint)
        }

        y += rowHeight
    }

    // ---------- Total Row ----------
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
        val right = (colX[i] + colWidth[i]).toFloat()
        val bottom = (y + rowHeight).toFloat()

        paint.style = Paint.Style.STROKE
        canvas.drawRect(left, y.toFloat(), right, bottom, paint)

        paint.style = Paint.Style.FILL
        canvas.drawText(totalRow[i], left + 2f, bottom - 6f, paint)
    }

    y += rowHeight + 50
    paint.isFakeBoldText = true

    // ---------- Footer (Client Details) ----------
    canvas.drawText(employee?.clients?.organisationName.orEmpty(), 20f, y.toFloat(), paint)
    y += 15
    paint.isFakeBoldText = false

    canvas.drawText(
        "ADDRESS - ${employee?.clients?.streetAddress.orEmpty()} , ${employee?.clients?.city.orEmpty()} - ${employee?.clients?.postalCode.orEmpty()}",
        20f,
        y.toFloat(),
        paint
    )
    y += 15
    canvas.drawText("GST - ${employee?.clients?.gstNo.orEmpty()}", 20f, y.toFloat(), paint)

    y += 15
    canvas.drawText("Note - This is not a Tax Invoice", 20f, y.toFloat(), paint)

    document.finishPage(page)

    // ---------- Save PDF File and Launch Viewer ----------
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

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Open PDF with..."))

    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
    }
}*/


/*fun generateInvoicePdfAndOpen(
    context: Context,
    order: CustomOrderResponse,
    employee: Employee?
) {
    CoroutineScope(Dispatchers.Main).launch {
        // Preload images first
        val imageBitmaps = mutableListOf<Bitmap?>()
        for (item in order.CustomOrderItem) {
            Log.d("@@","image@@"+item.Image)
            val bitmap = loadBitmapFromUrl("https://rrgold.loyalstring.co.in/"+item.Image ?: "")
            imageBitmaps.add(bitmap)
        }

        // Now draw PDF with loaded bitmaps
        val document = PdfDocument()
        val paint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val boldTextSize = 12f
        val regularTextSize = 11f
        var y = 30

        paint.textSize = boldTextSize
        paint.isFakeBoldText = true
        canvas.drawText("Bill Report", 20f, y.toFloat(), paint)
        y += 20

        val leftX = 25f
        val rightX = 320f

        paint.textSize = regularTextSize
        paint.isFakeBoldText = false

        for ((index, item) in order.CustomOrderItem.withIndex()) {
            // Box
            val boxLeft = 20f
            val boxTop = y.toFloat()
            val boxRight = 575f
            val boxHeight = 80f
            val boxBottom = boxTop + boxHeight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, paint)

            // Left Column
            paint.style = Paint.Style.FILL
            var leftTextY = y + 15f
            canvas.drawText(
                "Customer Name : ${order.Customer?.FirstName.orEmpty()} ${order.Customer?.LastName.orEmpty()}",
                leftX,
                leftTextY,
                paint
            )
            leftTextY += 18
            canvas.drawText("Order No       : ${item.OrderNo}", leftX, leftTextY, paint)
            leftTextY += 18
            canvas.drawText("Itemcode       : ${item.ItemCode}", leftX, leftTextY, paint)
            leftTextY += 18
            canvas.drawText("Notes          : ${"" ?: "null"}", leftX, leftTextY, paint)

            // Right Column
            var rightTextY = y + 15f
            canvas.drawText("G wt  : ${item.GrossWt}", rightX, rightTextY, paint)
            rightTextY += 18
            canvas.drawText("S wt  : ${item.StoneWt}", rightX, rightTextY, paint)
            rightTextY += 18
            canvas.drawText("N Wt  : ${item.NetWt}", rightX, rightTextY, paint)

            y += boxHeight.toInt() + 10

            // Draw Image from preloaded list
            imageBitmaps.getOrNull(index)?.let { bitmap ->
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 600, true)
                val imageX = (595 - scaledBitmap.width) / 2f
                canvas.drawBitmap(scaledBitmap, imageX, y.toFloat(), null)
                y += scaledBitmap.height + 10
            }
        }

        document.finishPage(page)

        try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Bill_Report_${System.currentTimeMillis()}.pdf"
            )
            document.writeTo(FileOutputStream(file))
            document.close()

            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Open PDF with..."))

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}*/
fun generateInvoicePdfAndOpen(
    context: Context,
    order: CustomOrderResponse,
    employee: Employee?,
    itemCodeList: List<ItemCodeResponse>
) {

    CoroutineScope(Dispatchers.Main).launch {
        val imageBitmaps = mutableListOf<Bitmap?>()
        for (item in order.CustomOrderItem) {
            Log.d("@@@","lastImagePath"+item.Image+" "+itemCodeList)
            if (item.Image != "" && item.Image != "https://rrgold.loyalstring.co.in/null") {
                val bitmap = loadBitmapFromUrl(item.Image)
                imageBitmaps.add(bitmap)
            }else
            {

                for (x in itemCodeList) {
                    if (item.ItemCode.equals(x.ItemCode))
                    {

                        val imageString = x?.Images.toString()
                        val lastImagePath =
                            imageString.split(",").lastOrNull()?.trim()
                        Log.d("@@","lastImagePath"+lastImagePath)
                        val bitmap = loadBitmapFromUrl("https://rrgold.loyalstring.co.in/"+lastImagePath.toString())
                        imageBitmaps.add(bitmap)
                        break
                    }
                }
            }
        }

        val document = PdfDocument()
        val paint = Paint()

        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas

        val boldTextSize = 12f
        val regularTextSize = 11f
        val marginTop = 30
        val marginBottom = 30
        var y = marginTop

        // Header
        paint.textSize = boldTextSize
        paint.isFakeBoldText = true
        canvas.drawText("Bill Report", 20f, y.toFloat(), paint)
        y += 20
        paint.textSize = regularTextSize
        paint.isFakeBoldText = false

        val leftX = 25f
        val rightX = 320f

        for ((index, item) in order.CustomOrderItem.withIndex()) {
            val boxHeight = 80
            val imageHeight = 600
            val spaceNeeded = boxHeight + 10 + imageHeight + 10

            if (y + spaceNeeded > pageHeight - marginBottom) {
                document.finishPage(currentPage)
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                y = marginTop
            }

            // Draw box
            val boxLeft = 20f
            val boxTop = y.toFloat()
            val boxRight = 575f
            val boxBottom = boxTop + boxHeight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, paint)

            // Left Column
            paint.style = Paint.Style.FILL
            var leftTextY = y + 15f
            canvas.drawText(
                "Customer Name : ${order.Customer.FirstName.orEmpty()} ${order.Customer.LastName.orEmpty()}",
                leftX,
                leftTextY,
                paint
            )
            leftTextY += 18
            canvas.drawText("Order No       : ${item.OrderNo}", leftX, leftTextY, paint)
            leftTextY += 18
            canvas.drawText("Itemcode       : ${item.ItemCode}", leftX, leftTextY, paint)
            leftTextY += 18
            canvas.drawText("Notes          : ${""}", leftX, leftTextY, paint)

            // Right Column
            var rightTextY = y + 15f
            canvas.drawText("T wt  : ${item.GrossWt}", rightX, rightTextY, paint)
            rightTextY += 18
            canvas.drawText("S wt  : ${item.StoneWt}", rightX, rightTextY, paint)
            rightTextY += 18
            canvas.drawText("N Wt  : ${item.NetWt}", rightX, rightTextY, paint)

            y += boxHeight + 10

            // Image
            imageBitmaps.getOrNull(index)?.let { bitmap ->
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 600, true)
                val imageX = (pageWidth - scaledBitmap.width) / 2f

                if (y + scaledBitmap.height > pageHeight - marginBottom) {
                    document.finishPage(currentPage)
                    currentPage = document.startPage(pageInfo)
                    canvas = currentPage.canvas
                    y = marginTop
                }

                canvas.drawBitmap(scaledBitmap, imageX, y.toFloat(), null)
                y += scaledBitmap.height + 10
            }
        }

        document.finishPage(currentPage)

        try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Order_Report_${order.Customer.FirstName.orEmpty()}.pdf"
            )
            document.writeTo(FileOutputStream(file))
            document.close()

            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Open PDF with..."))
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}
fun upscaleBitmap(original: Bitmap, scaleFactor: Float = 2f): Bitmap {
    val width = (original.width * scaleFactor).toInt()
    val height = (original.height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(original, width, height, true)
}

/*fun sharpenBitmap(src: Bitmap): Bitmap {
    val width = src.width
    val height = src.height
    val result = src.config?.let { Bitmap.createBitmap(width, height, it) }
    val kernel = arrayOf(
        floatArrayOf(0f, -1f, 0f),
        floatArrayOf(-1f, 5f, -1f),
        floatArrayOf(0f, -1f, 0f)
    )
    val kernelSize = 3
    val edge = kernelSize / 2
    for (y in edge until height - edge) {
        for (x in edge until width - edge) {
            var r = 0f
            var g = 0f
            var b = 0f
            for (ky in 0 until kernelSize) {
                for (kx in 0 until kernelSize) {
                    val pixel = src.getPixel(x + kx - edge, y + ky - edge)
                    val factor = kernel[ky][kx]
                 *//*   r += Color.red(pixel) * factor
                    g += Color.green(pixel) * factor
                    b += Color.blue(pixel) * factor*//*
                }
            }
            // Clamp values to 0–255
            val newR = r.coerceIn(0f, 255f).toInt()
            val newG = g.coerceIn(0f, 255f).toInt()
            val newB = b.coerceIn(0f, 255f).toInt()
            result.setPixel(x, y, Color.rgb(newR, newG, newB))
        }
    }
    return result
}*/


suspend fun loadBitmapFromUrl(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val url = URL(urlString)
        val inputStream = url.openStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}










