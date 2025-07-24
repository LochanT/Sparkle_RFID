package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.UserPreferences


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction


import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.viewmodel.OrderViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderLisrScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    userPreferences: UserPreferences
) {
    val orderViewModel: OrderViewModel = hiltViewModel()
    val context = LocalContext.current
    val employee =
        remember { UserPreferences.getInstance(context).getEmployee(Employee::class.java) }
    LaunchedEffect(Unit) {
        employee?.clientCode?.let { clientCode ->
            orderViewModel.fetchAllOrderListFromApi(ClientCodeRequest(clientCode))

        }
    }



    val headerTitles = listOf("Product Name", "Qty", "G.Wt", "M.Qty", "N.Wt")

    val data = listOf(
        listOf("Product 1", "8", "10.500", "6", "8.009"),
        listOf("Product 2", "3", "16.050", "3", "15.010"),
        listOf("Product 3", "2", "18.950", "3", "10.000"),
        // Add more rows here
    )

    var searchQuery by remember { mutableStateOf("") }

    val filteredData = if (searchQuery.isNotEmpty()) {
        data.filter { row -> row.any { it.contains(searchQuery, ignoreCase = true) } }
    } else data


    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(
            title = "OrderList",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
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

        // 🔍 Search Bar
        SearchBar(searchQuery) {
            searchQuery = it
        }

        // Horizontal scroll
        HorizontalScrollTable(headerTitles = headerTitles, data = filteredData)
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(45.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF2F2F2)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.padding(start = 12.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.Gray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = if (value.isNotEmpty()) 36.dp else 12.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
            )

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        }

    }
}

@Composable
fun HorizontalScrollTable(headerTitles: List<String>, data: List<List<String>>) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        Column(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .verticalScroll(verticalScroll)
        ) {
            // Header Row
            Row(modifier = Modifier
                .background(Color.DarkGray)
                .padding(vertical = 8.dp)) {
                headerTitles.forEach { title ->
                    Text(
                        text = title,
                        modifier = Modifier
                            .width(120.dp)
                            .padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Data Rows
            data.forEach { row ->
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            modifier = Modifier
                                .width(120.dp)
                                .padding(horizontal = 8.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
