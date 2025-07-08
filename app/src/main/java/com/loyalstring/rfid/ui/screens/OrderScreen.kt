package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenuItem
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.GradientButtonIcon
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.OrderViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import kotlinx.coroutines.delay


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
        bottomBar = {
            ScanBottomBar(
                onSave = {},
                onList = {},
                onScan = {
                    bulkViewModel.startSingleScan(20) { tag ->
                        tag.epc?.let {
                            it
                            Log.d("Scanned EPC", it)
                        }
                    }
                },
                onGscan = {},
                onReset = {}
            )
        }
    ) { innerPadding -> // <- Apply padding here
        Box(modifier = Modifier.padding(innerPadding)) {
            OrderScreenContent(userPreferences, bulkViewModel)
        }
    }
}

@Composable
fun OrderScreenContent(userPreferences: UserPreferences, bulkViewModel: BulkViewModel) {
    val orderViewModel: OrderViewModel = hiltViewModel()
    var customerName by remember { mutableStateOf(TextFieldValue("")) }
    var customerId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomer by remember { mutableStateOf<EmployeeList?>(null) }
    var itemCode by remember { mutableStateOf(TextFieldValue("")) }
    val customerOptions = listOf("John Doe", "Alice Smith", "Raj Kumar", "Ravi Jain")
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
    val context = LocalContext.current
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
    // val customerSuggestions by orderViewModel.empListResponse.observeAsState()

    val items by bulkViewModel.scannedItems.collectAsState()

    val scanTrigger by bulkViewModel.scanTrigger.collectAsState()
    var showOrderDialog by remember { mutableStateOf(false) }
    //val scannedData by bulkViewModel.scannedData.collectAsState()

    // Function to handle dialog confirm action
    val onConfirmOrderDetails: (String) -> Unit = { orderDetails ->
        // Handle the order details here
        Log.d("OrderDetails", "Order Details Confirmed: $orderDetails")
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
    LaunchedEffect(Unit) {
        bulkViewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            bulkViewModel.onBarcodeScanned(scanned)
            bulkViewModel.setRfidForAllTags(scanned)
            itemCode = TextFieldValue(scanned)

        }
    }


    val customerSuggestions by orderViewModel.empListResponse.observeAsState(initial = emptyList())

    val countryOptions = listOf("USA", "Canada", "Mexico", "UK", "India")
    var showDropdown by remember { mutableStateOf(false) }
    val isLoading by orderViewModel.isItemCodeLoading.collectAsState()

    val itemCodeList by orderViewModel.itemCodeResponse.collectAsState()
    val filteredList by remember(itemCode.text, itemCodeList, isLoading) {
        derivedStateOf {
            if (itemCode.text.isBlank() || itemCodeList.isEmpty() || isLoading) {
                emptyList()
            } else {
                itemCodeList.filter {
                    it.ItemCode?.contains(itemCode.text.trim(), ignoreCase = true) == true
                }
            }
        }
    }


    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)

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
                                    Log.d("@@", "Add button clicked")
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
                                    selectedCustomer=customer
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
                                            employee?.clientCode?.let { code ->
                                                orderViewModel.getAllItemCodeList(
                                                    ClientCodeRequest(
                                                        code
                                                    )
                                                )
                                            }
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
                            modifier = Modifier.size(20.dp)
                            .clickable {
                            // Show the dialog on click
                            showOrderDialog = true
                        },
                            tint = Color.Unspecified // keeps original colors of the vector
                        )
                    }
                }
            }

            // Show the OrderDetailsDialog when showDialog is true
            if (showOrderDialog) {
                OrderDetailsDialog(
                    customerId,
                    selectedCustomer,
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
                            .fillMaxWidth(0.9f) // Limit the dropdown width to 90% of the screen
                            .offset(y = 4.dp) // Position the dropdown just below the text field
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        filteredList.forEach { item ->
                            DropdownMenuItem(onClick = {
                                itemCode = TextFieldValue(item.ItemCode.toString())
                                showDropdown = false
                            }) {
                                Text(text = item.ItemCode.toString(), fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    Log.d("Item list", "No Data")
                }
            }
        }
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

                                // Spacer(modifier = Modifier.height(5.dp))

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



