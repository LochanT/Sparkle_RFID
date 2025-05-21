package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.viewmodel.ProductListViewModel

@Composable
fun ProductListScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
    val viewModel: ProductListViewModel = hiltViewModel()
    val searchQuery = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val allItems by viewModel.productList.collectAsState(initial = emptyList())
    val filteredItems = remember(searchQuery.value, allItems) {
        allItems.filter { item ->
            val query = searchQuery.value.trim().lowercase()
            item.itemCode.lowercase().contains(query) ||
                    item.product.lowercase().contains(query) ||
                    item.rfidCode.lowercase().contains(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(Modifier.height(12.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            placeholder = { Text("Enter RFID / Item code / Product") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E2E2E)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed columns
            Text("S.No", Modifier.width(60.dp), color = Color.White, textAlign = TextAlign.Center)
            // Scrollable header
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Product Name" to 150.dp,
                    "Item code" to 150.dp,
                    "RFID" to 90.dp,
                    "G.wt" to 90.dp,
                    "S.wt" to 90.dp,
                    "D.wt" to 90.dp,
                    "N.wt" to 90.dp,
                    "Category" to 70.dp,
                    "Design" to 70.dp,
                    "Purity" to 70.dp,
                    "Making/g" to 90.dp,
                    "Making%" to 90.dp,
                    "Fix Making" to 100.dp,
                    "Fix Wastage" to 100.dp,
                    "S Amount" to 100.dp,
                    "D Amount" to 100.dp,
                    "SKU" to 120.dp,
                    "EPC" to 120.dp,
                    "Vendor" to 120.dp,
                    "TID" to 90.dp
                ).forEach { (label, width) ->
                    Text(
                        label,
                        Modifier.width(width),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Fixed action buttons
            Text("Edit", Modifier.width(30.dp), color = Color.White, textAlign = TextAlign.Center)
            Text("Delete", Modifier.width(50.dp), color = Color.White, textAlign = TextAlign.Center)
        }

        // Data Rows
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(filteredItems) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fixed S.No
                    Text("${index + 1}", Modifier.width(60.dp), textAlign = TextAlign.Center)

                    // Scrollable section
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            item.product to 150.dp,
                            item.itemCode to 150.dp,
                            item.rfidCode to 90.dp,
                            "-" to 90.dp,
                            "-" to 90.dp,
                            "-" to 90.dp,
                            "-" to 90.dp,
                            item.category to 70.dp,
                            item.design to 70.dp,
                            "-" to 70.dp,
                            "-" to 90.dp,
                            "-" to 90.dp,
                            "-" to 100.dp,
                            "-" to 100.dp,
                            "-" to 100.dp,
                            "-" to 100.dp,
                            "-" to 120.dp,
                            item.uhftagInfo.epc to 120.dp,
                            "-" to 120.dp,
                            item.uhftagInfo.tid to 90.dp
                        ).forEach { (value, width) ->
                            Text(
                                value,
                                Modifier.width(width),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Fixed Edit & Delete
                    IconButton(
                        onClick = { /* Edit */ },
                        modifier = Modifier.width(30.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.DarkGray)
                    }
                    IconButton(
                        onClick = { /* Delete */ },
                        modifier = Modifier.width(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}



