package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel

@SuppressLint("HardwareIds")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanToDesktopScreen(onBack: () -> Unit, navController: NavHostController) {
    val viewModel: BulkViewModel = hiltViewModel()
    val context = LocalContext.current
    // Observe barcode and tag data
    val tags by viewModel.scannedTags.collectAsState()
    val items by viewModel.scannedItems.collectAsState()
    val rfidMap by viewModel.rfidMap.collectAsState()
    var itemCodes by remember { mutableStateOf("") }
    var firstPress by remember { mutableStateOf(false) }
    val reloadTrigger by viewModel.reloadTrigger.collectAsState()
    var selectedCount by remember { mutableStateOf(1) }


    var clickedIndex by remember { mutableStateOf<Int?>(null) }

    // ✅ Set barcode scan callback ONCE
    LaunchedEffect(Unit) {
        viewModel.barcodeReader.openIfNeeded()
        viewModel.barcodeReader.setOnBarcodeScanned { scanned ->
            viewModel.onBarcodeScanned(scanned)
            clickedIndex?.let { index ->
                viewModel.assignRfidCode(index, scanned)
            }

        }
    }
//    LaunchedEffect(reloadTrigger) {
//      navController.navigate(Screens.ProductManagementScreen.route)
//    }

//    LaunchedEffect(tags) {
//        itemCodes = List(tags.size) { index ->
//            itemCodes.getOrNull(index) ?: ""
//        }
//    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Scan to Desktop",
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
                onSave = {
                    viewModel.barcodeReader.close()
                    Log.d("save scanned items", "CLICKED")
                    val androidId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    if (rfidMap.isNotEmpty()) {
                        viewModel.sendScannedData(tags, androidId, context)

                    } else {
                        ToastUtils.showToast(context, "Please scan RFID tag")
                    }
                },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan = { //viewModel.startScanning(20)
                    viewModel.startSingleScan(20) { tag ->
                        tag.epc?.let {

                        }
                    }
                },
                onGscan = {
                    if (!firstPress) {
                        firstPress = true
                        viewModel.startScanning(selectedPower = selectedCount)
                        //   viewModel.startBarcodeScanning()
                    } else {
                        viewModel.stopScanning()
                        //     viewModel.startBarcodeScanning()
                    }
                },
                onReset = {
                    firstPress = false
                    viewModel.resetScanResults()
                    viewModel.stopBarcodeScanner()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sr No",
                    Modifier.weight(0.8f),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    fontSize = 13.sp
                )
                Text(
                    "EPC",
                    Modifier.weight(2.2f),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    fontSize = 13.sp
                )
                Text(
                    "RFIDCode",
                    Modifier.weight(2f),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    fontSize = 13.sp
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF0F0F0))

            ) {
                itemsIndexed(tags) { index, item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {


                            }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "${index + 1}",
                                Modifier
                                    .width(100.dp)
                                    .background(Color.Transparent),
                                color = Color.DarkGray,
                                fontFamily = poppins,
                                fontSize = 11.sp
                            )
                            Text(
                                item.epc,
                                Modifier
                                    .width(100.dp)
                                    .background(Color.Transparent),
                                color = Color.DarkGray,
                                fontFamily = poppins,
                                fontSize = 11.sp
                            )

                            val rfid = rfidMap[index]
                            val isScanned = rfid != null
                            val displayText = rfid ?: "scan here"
                            val textColor = if (!isScanned) Color.Blue else Color.DarkGray
                            val style =
                                if (!isScanned) TextDecoration.Underline else TextDecoration.None

                            Text(
                                " $displayText",
                                Modifier
                                    .width(100.dp)
                                    .clickable {
                                        clickedIndex = index
                                        viewModel.startBarcodeScanning()
                                    }, color = textColor, textDecoration = style,
                                fontFamily = poppins,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("", color = Color.White, fontFamily = poppins)
                Text(
                    "Total Items: ${items.size}",
                    color = Color.White,
                    fontFamily = poppins,
                    fontSize = 12.sp
                )
            }
        }
    }
}
