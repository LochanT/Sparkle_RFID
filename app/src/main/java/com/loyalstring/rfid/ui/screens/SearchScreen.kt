package com.loyalstring.rfid.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.data.local.entity.BulkItem
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
    var firstPress by remember { mutableStateOf(false) }

    val unmatchedItems = remember {
        try {
            navController.getBackStackEntry(Screens.SearchScreen.route)
                .savedStateHandle
                .get<List<BulkItem>>("unmatchedItems") ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    // Log to verify
    Log.d("UNMATCHED_LIST", "From SavedStateHandle: ${unmatchedItems.size}")


    var searchQuery by remember { mutableStateOf("") }

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

    LaunchedEffect(unmatchedItems) {
        if (unmatchedItems.isNotEmpty()) {
            searchViewModel.startSearch(unmatchedItems)
        }
    }
    DisposableEffect(Unit) {
        onDispose { searchViewModel.stopSearch() }
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
                    if (!firstPress) {
                        firstPress = true
                        searchViewModel.startSearch(unmatchedItems)
                    } else {
                        searchViewModel.stopSearch()
                        firstPress = false
                    }
                },
                onReset = {
                    searchQuery = ""
                    searchViewModel.stopSearch()
                    firstPress = false
                }
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
                        listOf("Sr No", "RFIDcode", "Itemcode", "Progress", "Percentage").forEach {
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

                itemsIndexed(filteredItems, key = { _, item -> item.epc }) { index, item ->
                    val percent = item.proximityPercent.toFloat() ?: 0f
                    val progressColor = getColorByPercentage(percent.toInt())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("${index + 1}", modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text(item.rfid ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text(item.itemCode ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)

                        Box(modifier = Modifier.weight(2f)) {
                            LinearProgressIndicator(
                                progress = percent / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = progressColor,
                                trackColor = Color.LightGray
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

fun getColorByPercentage(percent: Int): Color {
    return when {
        percent <= 25 -> Color.Red
        percent <= 50 -> Color(0xFFFFC107)
        percent <= 75 -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
}
