package com.loyalstring.rfid.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.InsertProductRequest
import com.loyalstring.rfid.data.model.addSingleItem.SKUModel
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.GradientButton
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import java.io.File

// Imports skipped for brevity — keep your existing ones

// Your data model
data class FormField(
    val label: String,
    val isDropdown: Boolean,
    val options: List<String> = emptyList(),
    val value: String = ""
)

private val sampleFields = listOf(
    FormField("EPC", false),
    FormField("Vendor", true),
    FormField("SKU", true),
    FormField("Item Code", false),
    FormField("RFID Code", false),
    FormField("Category", true),
    FormField("Product", true),
    FormField("Design", true),
    FormField("Purity", true),
    FormField("Gross Weight", false),
    FormField("Stone Weight", false),
    FormField("Diamond Weight", false),
    FormField("Net Weight", false),
    FormField("Making/Gram", false),
    FormField("Making %", false),
    FormField("Fix Making", false),
    FormField("Fix Wastage", false),
    FormField("Stone Amount", false),
    FormField("Diamond Amount", false),
    FormField("Image Upload", false)
)

@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: SingleProductViewModel = hiltViewModel()
) {
    val bulkViewModel: BulkViewModel = hiltViewModel()
    val context = LocalContext.current
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)

    val categoryList =
        (viewModel.categoryResponse.observeAsState().value as? Resource.Success)?.data
    val productList = (viewModel.productResponse.observeAsState().value as? Resource.Success)?.data
    val designList = (viewModel.designResponse.observeAsState().value as? Resource.Success)?.data
    val purityList = (viewModel.purityResponse.observeAsState().value as? Resource.Success)?.data
    val vendorList = (viewModel.vendorResponse.observeAsState().value as? Resource.Success)?.data
    val skuList = (viewModel.skuResponse.observeAsState().value as? Resource.Success)?.data

    val vendorNames = vendorList?.mapNotNull { it.VendorName } ?: emptyList()
    val skuNames = skuList?.mapNotNull { it.StockKeepingUnit } ?: emptyList()
    val categoryNames = categoryList?.mapNotNull { it.CategoryName } ?: emptyList()

    val scanTrigger by bulkViewModel.scanTrigger.collectAsState()
    val items by bulkViewModel.scannedItems.collectAsState()
    var scannedBarcode by remember { mutableStateOf("") }

    val showDialog = remember { mutableStateOf(false) }
    val imageUrl = remember { mutableStateOf("") }
    val imageUri = rememberSaveable { mutableStateOf<String?>(null) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        employee?.clientCode?.let {
            viewModel.fetchAllDropdownData(ClientCodeRequest(it))
        }
    }

    LaunchedEffect(scanTrigger) {
        scanTrigger?.let { type ->
            // Do something based on the key
            when (type) {
                "scan" -> {
                    if (items.size != 1) {
                        bulkViewModel.startScanning(20)
                    }
                }

                "barcode" -> {
                    bulkViewModel.startBarcodeScanning()
                }
            }

            // Important: clear after handling to prevent repeated triggers
            bulkViewModel.clearScanTrigger()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri?.toString() }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) imageUri.value = photoUri.value?.toString()
    }

    val fieldValues = remember { mutableStateMapOf<String, String>() }

    val categoryName = fieldValues["Category"].orEmpty()
    val productName = fieldValues["Product"].orEmpty()
    val designName = fieldValues["Design"].orEmpty()
    val purityName = fieldValues["Purity"].orEmpty()
    val vendorName = fieldValues["Vendor"].orEmpty()
    val skuName = fieldValues["SKU"].orEmpty()

    val formFields = remember(
        vendorNames, skuNames, categoryNames, categoryName, productName, designName
    ) {
        sampleFields.map { field ->
            val options = when (field.label) {
                "Vendor" -> vendorNames
                "SKU" -> skuNames
                "Category" -> categoryNames
                "Product" -> {
                    val categoryId = categoryList?.find { it.CategoryName == categoryName }?.Id
                    productList?.filter { it.CategoryId == categoryId }
                        ?.mapNotNull { it.ProductName } ?: emptyList()
                }

                "Design" -> {
                    val productId = productList?.find { it.ProductName == productName }?.Id
                    designList?.filter { it.ProductId == productId }?.mapNotNull { it.DesignName }
                        ?: emptyList()
                }

                "Purity" -> {
                    val categoryId = categoryList?.find { it.CategoryName == categoryName }?.Id
                    purityList?.filter { it.CategoryId == categoryId }?.mapNotNull { it.PurityName }
                        ?: emptyList()
                }

                else -> emptyList()
            }
            field.copy(options = options, value = fieldValues[field.label] ?: "")
        }.toMutableStateList()
    }

    fun updateField(label: String, value: String) {
        fieldValues[label] = value
    }

    LaunchedEffect(Unit) {
        viewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            bulkViewModel.onBarcodeScanned(scanned)
            bulkViewModel.setRfidForAllTags(scanned)
            updateField("RFID Code", scanned)

        }
    }

    fun onSkuSelected(sku: SKUModel) {
        updateField(
            "Category",
            categoryList?.find { it.Id == sku.CategoryId }?.CategoryName.orEmpty()
        )
        updateField("Product", productList?.find { it.Id == sku.ProductId }?.ProductName.orEmpty())
        updateField("Design", designList?.find { it.Id == sku.DesignId }?.DesignName.orEmpty())
        updateField("Purity", purityList?.find { it.Id == sku.PurityId }?.PurityName.orEmpty())
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

                onSave = {

                    formFields.forEach { field ->


                        val itemCode = formFields.find { it.label == "Item Code" }?.value.orEmpty()
                        val rfidCode = formFields.find { it.label == "RFIDcode" }?.value.orEmpty()
                        val epc = formFields.find { it.label == "EPC" }?.value.orEmpty()
                        val gWt = formFields.find { it.label == "Gross Weight" }?.value.orEmpty()
                        val ntWt = formFields.find { it.label == "Net Weight" }?.value.orEmpty()
                        val sWt = formFields.find { it.label == "Stone Weight" }?.value.orEmpty()
                        val dWt = formFields.find { it.label == "Diamond Weight" }?.value.orEmpty()
                        val making_gm =
                            formFields.find { it.label == "Making/Gram" }?.value.orEmpty()
                        val making_perc =
                            formFields.find { it.label == "Making %" }?.value.orEmpty()
                        val fMaking = formFields.find { it.label == "Fix Making" }?.value.orEmpty()
                        val fWastage =
                            formFields.find { it.label == "Fix Wastage" }?.value.orEmpty()
                        val stAmt = formFields.find { it.label == "Stone Amount" }?.value.orEmpty()
                        val dAmt =
                            formFields.find { it.label == "Diamond Amount " }?.value.orEmpty()

                        val categoryId =
                            categoryList?.find { it.CategoryName == categoryName }?.Id ?: 0
                        val productId = productList?.find { it.ProductName == productName }?.Id ?: 0
                        val designId = designList?.find { it.DesignName == designName }?.Id ?: 0
                        val vendorId = vendorList?.find { it.VendorName == vendorName }?.Id ?: 0
                        val skuId = skuList?.find { it.StockKeepingUnit == skuName }?.Id ?: 0
                        val purityId = purityList?.find { it.PurityName == purityName }?.Id ?: 0

                        val request = skuList?.get(0)?.let {
                            InsertProductRequest(
                                CategoryId = categoryId,
                                ProductId = productId,
                                DesignId = designId,
                                VendorId = vendorId,
                                PurityId = purityId,
                                RFIDCode = rfidCode,
                                HUIDCode = "",
                                HSNCode = "",
                                Quantity = "",
                                TotalWeight = 0.0,
                                PackingWeight = 0.0,
                                GrossWt = gWt,
                                TotalStoneWeight = "",
                                NetWt = ntWt,
                                Pieces = "",
                                MakingPercentage = making_perc,
                                MakingPerGram = making_gm,
                                MakingFixedAmt = fMaking,
                                MakingFixedWastage = fWastage,
                                MRP = "",
                                ClipWeight = "",
                                ClipQuantity = "",
                                ProductCode = "",
                                Featured = "",
                                ProductTitle = "",
                                Description = "",
                                Gender = "",
                                DiamondId = "",
                                DiamondName = "",
                                DiamondShape = "",
                                DiamondShapeName = "",
                                DiamondClarity = "",
                                DiamondClarityName = "",
                                DiamondColour = "",
                                DiamondColourName = "",
                                DiamondSleve = "",
                                DiamondSize = "",
                                DiamondSellRate = "",
                                DiamondWeight = dWt,
                                DiamondCut = "",
                                DiamondCutName = "",
                                DiamondSettingType = "",
                                DiamondSettingTypeName = "",
                                DiamondCertificate = "",
                                DiamondDescription = "",
                                DiamondPacket = "",
                                DiamondBox = "",
                                DiamondPieces = "",
                                Stones = emptyList(),
                                DButton = "",
                                StoneName = "",
                                StoneShape = "",
                                StoneSize = "",
                                StoneWeight = sWt,
                                StonePieces = "",
                                StoneRatePiece = "",
                                StoneRateKarate = "",
                                StoneAmount = stAmt,
                                StoneDescription = "",
                                StoneCertificate = "",
                                StoneSettingType = "",
                                BranchName = "",
                                BranchId = it.BranchId,
                                //   SKU = "",
                                PurityName = "",
                                TotalStoneAmount = "",
                                TotalStonePieces = "",
                                ClientCode = it.ClientCode,
                                EmployeeCode = it.EmployeeId,
                                StoneColour = "",
                                CompanyId = 0,
                                MetalId = 0,
                                WarehouseId = 0,
                                TIDNumber = epc,
                                grosswt = "",
                                TotalDiamondWeight = dWt,
                                TotalDiamondAmount = "",
                                Status = "",

                                )

                        }
                        request?.let { viewModel.insertLabelledStock(it) }
                    }
                },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan = {
                    bulkViewModel.startSingleScan(20) { tag ->
                        tag.epc?.let {
                            updateField("EPC", it)
                        }
                    }
                },
                onGscan = {}, onReset = {}
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(formFields) { field ->
                FormRow(
                    field = field,
                    value = fieldValues[field.label] ?: "",
                    showDialog = showDialog.value,
                    onShowDialogChange = { showDialog.value = it },
                    imageUrl = imageUrl.value,
                    onImageUrlChange = { imageUrl.value = it },
                    onValueChange = { value ->
                        updateField(field.label, value)
                        when (field.label) {
                            "Category" -> {
                                updateField("Product", "")
                                updateField("Design", "")
                                updateField("Purity", "")
                            }

                            "Product" -> {
                                updateField("Design", "")
                                updateField("Purity", "")
                            }

                            "Design" -> updateField("Purity", "")
                        }
                    },
                    skuList = skuList,
                    onSkuSelected = { onSkuSelected(it) },
                    selectedCategory = fieldValues["Category"],
                    selectedProduct = fieldValues["Product"],
                    selectedDesign = fieldValues["Design"]
                )
            }
        }

        if (showDialog.value) {
            ImageUploadDialog(
                showDialog = showDialog.value,
                onDismiss = { showDialog.value = false },
                onConfirm = {
                    updateField("Image Upload", imageUri.value.orEmpty())
                    showDialog.value = false
                },
                onTakePhoto = {
                    val uri = File(context.cacheDir, "${System.currentTimeMillis()}.jpg").apply {
                        createNewFile()
                    }.let {
                        FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                    }
                    photoUri.value = uri
                    cameraLauncher.launch(uri)
                },
                onAttachFile = {
                    galleryLauncher.launch("image/*")
                },
                imageUrl = imageUrl.value,
                onImageUrlChange = { imageUrl.value = it },
                imageUri = imageUri.value,
                onImageUriChange = { imageUri.value = it }
            )
        }
    }
}

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
            TextButton(onClick = onSave) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        tint = Color.DarkGray,
                        contentDescription = "Save",
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = Color.DarkGray, fontSize = 12.sp, fontFamily = poppins)
                }
            }
            TextButton(onClick = onList) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_list),
                        tint = Color.DarkGray,
                        contentDescription = "List"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("List", color = Color.DarkGray, fontSize = 12.sp, fontFamily = poppins)
                }
            }
            Spacer(modifier = Modifier.width(64.dp)) // space for center button
            TextButton(onClick = onGscan) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_gscan),
                        tint = Color.DarkGray,
                        contentDescription = "Gscan"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gscan", color = Color.DarkGray, fontSize = 12.sp, fontFamily = poppins)
                }
            }
            TextButton(onClick = onReset) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reset),
                        tint = Color.DarkGray,
                        contentDescription = "Reset"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = Color.DarkGray, fontSize = 12.sp, fontFamily = poppins)
                }
            }
        }


        // 2) The elevated circular Scan button, centered
        Box(
            modifier = Modifier
                .size(65.dp)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF5231A7), Color(0xFFD32940))
                    )
                )
                .clickable(onClick = onScan),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_scan),
                    contentDescription = "Scan",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Scan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = poppins
                )
            }
        }


    }


}


@OptIn(ExperimentalMaterial3Api::class)
// ✅ Updated FormRow with proper value binding and no local text state

@Composable
fun FormRow(
    field: FormField,
    value: String,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    skuList: List<SKUModel>? = null,
    onSkuSelected: ((SKUModel) -> Unit)? = null,
    selectedCategory: String? = null,
    selectedProduct: String? = null,
    selectedDesign: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = when (field.label) {
        "Product" -> if (!selectedCategory.isNullOrBlank()) field.options else emptyList()
        "Design" -> if (!selectedProduct.isNullOrBlank()) field.options else emptyList()
        "Purity" -> if (!selectedDesign.isNullOrBlank()) field.options else emptyList()
        else -> field.options
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = field.label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(0.8f)
        )

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
                if (filteredOptions.isEmpty()) {
                    Text(
                        text = "Please select a ${
                            when (field.label) {
                                "Product" -> "Category first"
                                "Design" -> "Product first"
                                "Purity" -> "Design first"
                                else -> "option"
                            }
                        }",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                } else {
                    Column {
                        BasicTextField(
                            value = value,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (value.isEmpty()) {
                                            Text(
                                                "select",
                                                color = Color.LightGray,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }

                                    Row {
                                        if (value.isNotEmpty()) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = Color.Gray,
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .size(20.dp)
                                                    .clickable { onValueChange("") }
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            modifier = Modifier.clickable { expanded = true }
                                        )
                                    }
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onValueChange(option)
                                        expanded = false

                                        if (field.label == "SKU") {
                                            skuList?.find { it.StockKeepingUnit == option }?.let {
                                                onSkuSelected?.invoke(it)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else if (field.label == "Image Upload") {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Upload",
                    modifier = Modifier.clickable { onShowDialogChange(true) }
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text("Tap to enter…", color = Color.LightGray, fontSize = 14.sp)
                        }
                        inner()
                    }
                )
            }
        }
    }
}


@Composable
fun ImageUploadDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onTakePhoto: () -> Unit,
    onAttachFile: () -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    imageUri: String?, // <-- add this
    onImageUriChange: (String?) -> Unit // <-- add this
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = null,
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        Color(0xFFF0F0F0),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add Photo",
                                    tint = Color.Gray
                                )
                            }
                        } else {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Uploaded Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Take Photo Button
                    GradientButton(text = "Take Photo", onClick = onTakePhoto)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image URL Input
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = onImageUrlChange,
                        placeholder = { Text("Image Url") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach File",
                                tint = Color(0xFF8B0000),
                                modifier = Modifier.clickable(onClick = onAttachFile)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GradientButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    GradientButton(
                        text = "Ok",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
        )

    }
}







