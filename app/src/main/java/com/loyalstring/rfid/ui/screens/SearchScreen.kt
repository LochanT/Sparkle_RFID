package com.loyalstring.rfid.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.loyalstring.rfid.MainActivity
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.local.entity.SearchItem
import com.loyalstring.rfid.data.reader.ScanKeyListener
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    navController: NavHostController,
) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    var isScanning by remember { mutableStateOf(false) }
    //val activity = LocalContext.current as MainActivity
    var firstPress by remember { mutableStateOf(false) }


    val unmatchedItems = remember {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<List<BulkItem>>("unmatchedItems") ?: emptyList()
    }
    // Log to verify
    Log.d("UNMATCHED_LIST", "From SavedStateHandle: ${unmatchedItems.size}")
    var showList by remember { mutableStateOf(true) }


    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current


    val allItems = remember(searchViewModel.searchItems, unmatchedItems) {
        searchViewModel.searchItems.toMutableList().apply {
            addAll(unmatchedItems.map {
                SearchItem(
                    epc = it.epc ?: "",
                    itemCode = it.itemCode ?: "",
                    productName = it.productName ?: "",
                    rfid = it.rfid ?: ""
                )
            })
        }
    }



    val filteredItems by remember(searchQuery, searchViewModel.searchItems) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                searchViewModel.searchItems
            } else {
                searchViewModel.searchItems.filter {
                    it.rfid.contains(searchQuery, ignoreCase = true) || it.itemCode.contains(
                        searchQuery,
                        ignoreCase = true
                    )
                }
            }
        }
    }

    val activity = context.findActivity() as? MainActivity
    val lifecycleOwner = LocalLifecycleOwner.current


    DisposableEffect(lifecycleOwner, activity) {
        val listener = object : ScanKeyListener {
            override fun onBarcodeKeyPressed() {
                // optional
            }

            override fun onRfidKeyPressed() {
                if (isScanning) {
                    searchViewModel.stopSearch()
                    isScanning = false
                    Log.d("@@", "RFID STOPPED from key")
                } else {
                    searchViewModel.startSearch(unmatchedItems)
                    isScanning = true
                    Log.d("@@", "RFID STARTED from key")
                }
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activity?.registerScanKeyListener(listener)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    activity?.unregisterScanKeyListener()
                    // optional: force stop when leaving screen
                    if (isScanning) {
                        searchViewModel.stopSearch()
                        isScanning = false
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity?.unregisterScanKeyListener()
            if (isScanning) {
                searchViewModel.stopSearch()
                isScanning = false
            }
        }
    }



    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Search",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                showCounter = true,
                selectedCount = 30,
                onCountSelected = {}
            )
        },
        bottomBar = {

            ScanBottomBar(
                onSave = { },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan = { },
                onGscan = {

                    if (!isScanning) {
                        firstPress = true
                        isScanning=true
                        searchViewModel.startSearch(unmatchedItems)

                        // 🔊 Start sound here
                        // searchViewModel.start()
                    } else {
                        searchViewModel.stopSearch()
                        firstPress = false
                        isScanning=false

                        // 🔇 Stop sound here
                        // searchViewModel.stopScanSound()
                    }
                },
                onReset = {
                    searchQuery = ""
                    searchViewModel.stopSearch()
                    firstPress = false
                    isScanning=false

                },
                isScanning = isScanning

            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                label = { Text("Enter RFID / Itemcode", fontFamily = poppins) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )
            if (showList) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF3B363E))
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                "Sr No",
                                "RFIDcode",
                                "Itemcode",
                                "Progress",
                                "Percentage"
                            ).forEach {
                                Text(
                                    text = it,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    fontFamily = poppins,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    val displayItems = if (filteredItems.isNotEmpty()) filteredItems else allItems
                    itemsIndexed(displayItems, key = { _, item -> item.epc }) { index, item ->
                        val percent = item.proximityPercent.toFloat()
                        val progressColor = getColorByPercentage(percent.toInt())

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("${index + 1}", modifier = Modifier.weight(1f), fontSize = 12.sp)
                            Text(item.rfid, modifier = Modifier.weight(1f), fontSize = 12.sp)
                            Text(item.itemCode, modifier = Modifier.weight(1f), fontSize = 12.sp)

                            Box(modifier = Modifier.weight(2f)) {
                                LinearProgressIndicator(
                                    progress = { percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = progressColor,
                                    trackColor = Color.LightGray,
                                )
                            }

                            Text(
                                "${percent.toInt()}%",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun getColorByPercentage(percent: Int): Color {
    return when {
        percent <= 25 -> Color.Red
        percent <= 50 -> Color.Yellow
        percent <= 75 -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
}
