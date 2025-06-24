package com.loyalstring.rfid.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.MappingDialogWrapper
import com.loyalstring.rfid.ui.utils.SyncProgressBar
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel
import com.loyalstring.rfid.viewmodel.ImportExcelViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProductManagementScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    userPreferences: UserPreferences,
    viewModel: BulkViewModel = hiltViewModel()
) {
    val importViewModel: ImportExcelViewModel = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val progress by viewModel.syncProgress.collectAsState()
    val status by viewModel.syncStatusText.collectAsState()
    val context: Context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    var selectedCount by remember { mutableStateOf(1) }
    var selectedPower by remember { mutableStateOf(1) }

    var excelColumns by remember { mutableStateOf(listOf<String>()) }
    var showMappingDialog by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    var showOverlay by remember { mutableStateOf(false) }

    val scanTrigger by viewModel.scanTrigger.collectAsState()
    val bulkItemFieldNames = listOf(
        "productName",
        "itemCode",
        "rfid",
        "grossWeight",
        "stoneWeight",
        "dustWeight",
        "netWeight",
        "category",
        "design",
        "purity",
        "makingPerGram",
        "makingPercent",
        "fixMaking",
        "fixWastage",
        "stoneAmount",
        "dustAmount",
        "sku",
        "epc",
        "vendor",
        "tid",
        "box",
        "designCode",
        "productCode",
        "uhftagInfo"
    )
    var isSheetProcessed by remember { mutableStateOf(false) }

    LaunchedEffect(scanTrigger) {
        scanTrigger?.let { type ->
            // Do something based on the key
            when (type) {
                "scan" -> {
                    viewModel.startScanning(selectedPower)
                }

                "barcode" -> {
                    viewModel.startBarcodeScanning()
                }
            }

            // Important: clear after handling to prevent repeated triggers
            viewModel.clearScanTrigger()
        }
    }





    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Product",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {

                },
                showCounter = true,
                selectedCount = selectedCount,
                onCountSelected = {
                    selectedCount = it
                }
            )
        },
        bottomBar = {
            ScanBottomBar(
                onSave = { /* TODO */ },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan = { /* TODO */ },
                onGscan = { /* TODO */ },
                onReset = { /* TODO */ }
            )
        }


    ) { innerPadding ->

        LaunchedEffect(status) {
            if (status.contains("completed", ignoreCase = true)) {
                scaffoldState.snackbarHostState.showSnackbar(status)
            }
        }


        val productItems = listOf(
            ProductGridItem("Add Single\nProduct", R.drawable.add_single_prod, true, "add product"),
            ProductGridItem("Add Bulk\nProducts", R.drawable.add_bulk_prod, true, "bulk products"),
            ProductGridItem("Import\nExcel", R.drawable.import_excel, false, "import excel"),
            ProductGridItem("Export\nExcel", R.drawable.export_excel, false, ""),
            ProductGridItem("Click to\nSync Data", R.drawable.ic_sync_data, false, ""),
            ProductGridItem("Scan to\nDesktop", R.drawable.barcode_reader, false, "scan_web"),
            ProductGridItem("CLick to\nSync Sheet Data", R.drawable.ic_sync_sheet_data, false, ""),
            ProductGridItem("Click to Upload\nData to Server", R.drawable.upload_data, false, "")
        )



        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            val columns = 2
            val spacing = 16.dp
            val itemCount = productItems.size
            val rows = (itemCount + 1) / 2 // ceil division

            val totalVerticalSpacing = spacing * (rows + 1)
            val totalHorizontalSpacing = spacing * (columns + 1)

            val itemWidth = (maxWidth - totalHorizontalSpacing) / columns
            val itemHeight = (maxHeight - totalVerticalSpacing) / rows

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productItems) { item ->
                    ProductGridCard(
                        item = item,
                        width = itemWidth,
                        height = itemHeight,
                        onClick = { selectedItem ->

                            when (selectedItem.label) {
                                "Click to\nSync Data" -> {
                                    viewModel.syncItems()
                                }

                                "Export\nExcel" -> {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        viewModel.getAllItems(context)
                                    }
                                }

                                "CLick to\nSync Sheet Data" -> {
                                    val sheetId =
                                        userPreferences.getSheetUrl()
                                    if (sheetId.isNullOrBlank()) {
                                        ToastUtils.showToast(
                                            context,
                                            "Please add a valid Sheet URL in Settings"
                                        )
                                        return@ProductGridCard
                                    }
                                    val sheetUrl =
                                        "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv"
                                    if (!isSheetProcessed) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val headers =
                                                viewModel.parseGoogleSheetHeaders(
                                                    sheetUrl
                                                )
                                            if (headers.isNotEmpty()) {
                                                launch(Dispatchers.Main) {
                                                    excelColumns = headers
                                                    showMappingDialog = true
                                                    isSheetProcessed = true
                                                }
                                            } else {
                                                launch(Dispatchers.Main) {
                                                    ToastUtils.showToast(
                                                        context,
                                                        "Failed to fetch or parse sheet headers."
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    if (selectedItem.route.isNotBlank()) {
                                        navController.navigate(selectedItem.route)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }


    }
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            SyncProgressBar(
                isLoading = isLoading,
                progress = progress,
                status = status
            )
        }
    }
    if (showMappingDialog) {
        showProgress = false
        MappingDialogWrapper(
            excelColumns = excelColumns,
            bulkItemFields = bulkItemFieldNames,
            onDismiss = {
                showMappingDialog = false
                navController.navigate(Screens.ProductManagementScreen.route)
            },
            fileSelected = true,
            onImport = { mapping ->
                showOverlay = true
                importViewModel.importMappedData(context, mapping)
                showMappingDialog = false

            },
            isFromSheet = true
        )
    }
}

@Composable
fun ProductGridCard(
    item: ProductGridItem,
    width: Dp,
    height: Dp,
    onClick: (ProductGridItem) -> Unit
) {
    val cardColors = if (item.isGradient) {
        Brush.linearGradient(colors = listOf(Color(0xFF5231A7), Color(0xFFD32940)))
    } else {
        SolidColor(Color.DarkGray)
    }

    Card(
        modifier = Modifier
            .size(width, height)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardColors)
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.label,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.label,
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    maxLines = 2,
                    lineHeight = 16.sp, // adds line spacing
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                )
            }
        }
    }
}


// Data class
data class ProductGridItem(
    val label: String,
    val iconRes: Int,
    val isGradient: Boolean = false,
    val route: String
)
