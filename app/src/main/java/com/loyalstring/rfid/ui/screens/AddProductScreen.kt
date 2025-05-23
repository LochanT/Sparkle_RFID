package com.loyalstring.rfid.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.VendorModel
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.viewmodel.SingleProductViewModel

// 1) Data class for each row
data class FormField(
    val label: String,
    val isDropdown: Boolean,
    val options: List<String> = emptyList(),


)


// 2) The list of fields you want
private val sampleFields = listOf(
    FormField("EPC",         isDropdown = false),
    FormField("Vendor",      isDropdown = true),
    FormField("SKU",         isDropdown = true),
    FormField("Itemcode",    isDropdown = false),
    FormField("RFIDcode",    isDropdown = false),
    FormField("Category",    isDropdown = true),
    FormField("Product",     isDropdown = true,  options = listOf("Prod 1","Prod 2")),
    FormField("Design",      isDropdown = true,  options = listOf("X","Y","Z")),
    FormField("Purity",      isDropdown = true,  options = listOf("High","Med","Low")),
    FormField("Gross Weight",isDropdown = false),
    FormField("Stone Weight",isDropdown = false),
    FormField("Diamond Weight",isDropdown = false),
    FormField("Net Weight",isDropdown = false),
    FormField("Making/Gram",isDropdown = false),
    FormField("Making %",isDropdown = false),
    FormField("Fix Making",isDropdown = false),
    FormField("Fix Wastage",isDropdown = false),
    FormField("Stone Amount",isDropdown = false),
    FormField("Diamond Amount",isDropdown = false),
    FormField("Image Upload",isDropdown = false)
)

// 3) The form container
@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    context: Context,
    viewModel: SingleProductViewModel = hiltViewModel()
) {
    val vendors = viewModel.vendorResponse.observeAsState().value
    val skus = viewModel.skuResponse.observeAsState().value
    val category = viewModel.categoryResponse.observeAsState().value
    val product = viewModel.productResponse.observeAsState().value
    val design = viewModel.designResponse.observeAsState().value
    val purity = viewModel.purityResponse.observeAsState().value

    // Trigger API only once
    LaunchedEffect(Unit) {
        val employee = UserPreferences
            .getInstance(context)
            .getEmployee(Employee::class.java)

        employee?.clientCode?.let {
            viewModel.getAllVendor(ClientCodeRequest(it))
            viewModel.getAllSKU(ClientCodeRequest(it))
            viewModel.getAllCategory(ClientCodeRequest(it))
            viewModel.getAllProduct(ClientCodeRequest(it))
            viewModel.getAllDesign(ClientCodeRequest(it))
            viewModel.getAllPurity(ClientCodeRequest(it))
        }
    }

    // ✅ vendor data
    val vendorNames = when (vendors) {
        is Resource.Success -> vendors.data?.map { it.VendorName ?: "" } ?: emptyList()
        else -> emptyList()
    }

    // ✅ sku data
    val skuNames = when (skus) {
        is Resource.Success -> skus.data?.map { it.StockKeepingUnit ?: "" } ?: emptyList()
        else -> emptyList()
    }
    val categoryNames = when (category) {
        is Resource.Success -> category.data?.map { it.CategoryName ?: "" } ?: emptyList()
        else -> emptyList()
    }

    val productNames = when (product) {
        is Resource.Success -> product.data?.map { it.ProductName ?: "" } ?: emptyList()
        else -> emptyList()
    }

    val designNames = when (design) {
        is Resource.Success -> design.data?.map { it.DesignName ?: "" } ?: emptyList()
        else -> emptyList()
    }
    val purityNames = when (design) {
        is Resource.Success -> purity?.data?.map { it.PurityName ?: "" } ?: emptyList()
        else -> emptyList()
    }



    // ✅ Replace Vendor field dynamically
    val dynamicFields = sampleFields.map { field ->
        when (field.label) {
            "Vendor" -> field.copy(options = vendorNames)
            "SKU" -> field.copy(options = skuNames)
            "Category" -> field.copy(options = categoryNames)
            "Product" -> field.copy(options = productNames)
            "Design" -> field.copy(options = designNames)
            "Purity" -> field.copy(options = purityNames)
            else -> field
        }
    }




    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Add Single Product",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = rememberLazyListState()
        ) {
            // ✅ Use dynamic field list
            items(dynamicFields) { field ->
                FormRow(field)
            }
        }
    }
}


/*@Composable
fun AddProductScreen(onBack: () -> Unit, navController: NavHostController, context: Context,viewModel: SingleProductViewModel = hiltViewModel()) {
   // val vendors by viewModel.vendorResponse
    val vendors = viewModel.vendorResponse.observeAsState().value

    // Trigger API call only once
    LaunchedEffect(Unit) {

        val employee = UserPreferences
            .getInstance(context)
            .getEmployee(Employee::class.java)

        if (employee != null) {
            Log.d("EMPLOYEE", employee?.clientCode.toString())
        }
        val request = ClientCodeRequest(employee?.clientCode.toString())
        viewModel.getAllVendor(request)

        // ✅ Map vendor response to list of names
        val vendorNames = when (vendors) {
            is Resource.Success -> vendors.data?.map { it.VendorName ?: "" } ?: emptyList()
            else -> emptyList()
        }

        // ✅ Replace static field list with dynamic injection
        val dynamicFields = sampleFields.map {
            if (it.label == "Vendor") {
                it.copy(options = vendorNames)
            } else {
                it
            }
        }
    }



    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Add Single Product",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
         bottomBar = {
            ScanBottomBar(
                onSave  = { *//*...*//* },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan  = { *//*...*//* },
                onGscan = { *//*...*//* },
                onReset = { *//*...*//* }
            )
        }
    // Ensure Scaffold takes full screen
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0)) // Light gray background for debugging
                .padding(innerPadding) // Respect Scaffold padding
                .padding(horizontal = 16.dp, vertical = 12.dp), // Horizontal padding for aesthetics
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = rememberLazyListState() // Explicit scroll state
        ) {
            items(sampleFields) { field ->
                FormRow(field)
            }



        }
    }
}*/
// 4) Single row with text or dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBottomBar(
    onSave: () -> Unit,
    onList: () -> Unit,
    onScan: () -> Unit,
    onGscan: () -> Unit,
    onReset: () -> Unit
) {

            // We use a Box to allow the center button to overlap/elevate
            Box {
                // 1) The background row of four icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(Color.White), // light gray background
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSave) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            tint = Color.DarkGray,
                            contentDescription = "Save",
                        )
                    }
                    IconButton(onClick = onList) {
                        Icon(
                            painter = painterResource(R.drawable.ic_list),
                            tint = Color.DarkGray,
                            contentDescription = "List"
                        )
                    }
                    Spacer(modifier = Modifier.width(64.dp)) // space for center button
                    IconButton(onClick = onGscan) {
                        Icon(
                            painter = painterResource(R.drawable.ic_gscan),
                            tint = Color.DarkGray,
                            contentDescription = "Gscan"
                        )
                    }
                    IconButton(onClick = onReset) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reset),
                            tint = Color.DarkGray,
                            contentDescription = "Reset"
                        )
                    }
                }

                // 2) The elevated circular Scan button, centered
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-24).dp)     // lift it above the row
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF5231A7), Color(0xFFD32940))
                            )
                        )
                        .clickable(onClick = onScan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_scan),
                        contentDescription = "Scan",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }


            }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormRow(field: FormField) {
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = field.options.filter {
        it.contains(text, ignoreCase = true)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Text(
            text = field.label,
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(0.8f)
        )

        // Input / Dropdown Box
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (field.isDropdown) {
                Column {
                    BasicTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            expanded = true
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                expanded = focusState.isFocused
                            },
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (text.isEmpty()) {
                                        Text("Tap to enter…", color = Color.LightGray, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }

                                Row {
                                    if (text.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier
                                                .clickable {
                                                    text = ""
                                                    expanded = false
                                                }
                                                .size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        modifier = Modifier
                                            .clickable {
                                                expanded = true
                                            }
                                            .size(24.dp)
                                    )
                                }
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded && filteredOptions.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    text = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                if (field.label != "Image Upload") {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        decorationBox = { inner ->
                            if (text.isEmpty()) {
                                Text("Tap to enter…", color = Color.LightGray, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_gscan),
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}








