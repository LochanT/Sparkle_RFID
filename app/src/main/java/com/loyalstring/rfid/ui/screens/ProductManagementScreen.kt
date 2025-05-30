package com.loyalstring.rfid.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.SyncProgressBar
import com.loyalstring.rfid.viewmodel.BulkViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProductManagementScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: BulkViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val progress by viewModel.syncProgress.collectAsState()
    val status by viewModel.syncStatusText.collectAsState()
    val context: Context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()




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

        val items = listOf(
            ProductGridItem("Add Single\nProduct", R.drawable.add_single_prod, true, "add product"),
            ProductGridItem("Add Bulk\nProducts", R.drawable.add_bulk_prod, true, "bulk products"),
            ProductGridItem("Import\nExcel", R.drawable.export_excel, true, "import excel"),
            ProductGridItem("Export\nExcel", R.drawable.export_excel, true, "export excel"),
            ProductGridItem("Click to\nSync Data", R.drawable.ic_sync_data, true, ""),
            ProductGridItem("Scan to\nDesktop", R.drawable.ic_sync_sheet_data, false, ""),
            ProductGridItem("Sync Sheet\nData", R.drawable.barcode_reader, false, ""),
            ProductGridItem("Upload\nData", R.drawable.upload_data, false, "")
        )



        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {


            val columns = 2
            val rows = 4
            val spacing = 16.dp
            val totalVerticalSpacing = spacing * (rows + 1)
            val totalHorizontalSpacing = spacing * (columns + 1)
            val itemWidth = (maxWidth - totalHorizontalSpacing) / columns
            val itemHeight = (maxHeight - totalVerticalSpacing) / rows

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                for (row in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0 until columns) {
                            val index = row * columns + col
                            if (index < items.size) {
                                ProductGridCard(
                                    item = items[index],
                                    navController = navController,
                                    width = itemWidth,
                                    height = itemHeight,
                                    viewModel,
                                    context

                                )
                            } else {
                                Spacer(modifier = Modifier.size(itemWidth, itemHeight))
                            }
                        }
                    }
                }
            }
            StatusHandler(
                scaffoldState = scaffoldState,
                isWorking = viewModel.isExporting,
                statusText = viewModel.exportStatus
            )
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
}

@Composable
fun ProductGridCard(
    item: ProductGridItem,
    navController: NavHostController,
    width: Dp,
    height: Dp,
    viewModel: BulkViewModel,
    context: Context
) {
    val cardColors = if (item.isGradient) {
        Brush.linearGradient(colors = listOf(Color(0xFF5231A7), Color(0xFFD32940)))
    } else {
        SolidColor(Color.DarkGray)
    }

    Card(
        modifier = Modifier
            .size(width, height)
            .clickable {
                when (item.label) {
                    "Click to\nSync Data" -> {
                        Log.d("GridClick", "Calling syncItems()")
                        viewModel.syncItems()

                    }

                    "Export\nExcel" -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.getAllItems(context)
                        }
                    }

                    else -> {
                        if (item.route.isNotBlank()) {
                            navController.navigate(item.route)
                        }
                    }
                }
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardColors)
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.label,
                    tint = if (item.isGradient) Color.Unspecified else Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
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
