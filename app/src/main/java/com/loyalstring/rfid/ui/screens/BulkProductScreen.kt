package com.loyalstring.rfid.ui.screens

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens

data class ProductItem(val srNo: Int, val itemCode: String, val rfidCode: String)

val sampleData = listOf(
    ProductItem(1, "159878", "857462258"),
    ProductItem(2, "145789", "45453433"),
    ProductItem(3, "145758", "757524822"),
    ProductItem(4, "477896", "787885454"),
    ProductItem(5, "475586", "775877676"),
    ProductItem(6, "148589", "45789546"),
    ProductItem(7, "457572", "64494949"),
    ProductItem(8, "454542", "155999954"),
    ProductItem(9, "445442", "4999494949"),
    ProductItem(10, "445424", "888484884")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkProductScreen(onBack: () -> Unit, navController: NavHostController) {
    // Sample dynamic options
    val categoryOptions = listOf("Shirts", "Pants", "Jackets")
    val productOptions = listOf("Formal", "Casual", "Partywear")
    val designOptions = listOf("Striped", "Plain", "Checked")

    // Selected states
    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var selectedProduct by remember { mutableStateOf(productOptions.first()) }
    var selectedDesign by remember { mutableStateOf(designOptions.first()) }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Add Bulk Products",
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
                onSave  = { /*...*/ },
                onList  = { /*...*/ },
                onScan  = { /*...*/ },
                onGscan = { /*...*/ },
                onReset = { /*...*/ }
            )
        }
        // Ensure Scaffold takes full screen
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Filter Dropdowns
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState()), // Enable horizontal scrolling
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Consistent spacing
            ) {
                FilterDropdown("Category", categoryOptions, selectedCategory) { selectedCategory = it }
                FilterDropdown("Product", productOptions, selectedProduct) { selectedProduct = it }
                FilterDropdown("Design", designOptions, selectedDesign) { selectedDesign = it }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start), // Align to start with even spacing
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Sr No", "Itemcode", "RFIDCode").forEach {
                    Text(it, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                }
            }

            // Table Rows
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF0F0F0))
            ) {
                items(sampleData) { item ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start), // Align to start with even spacing
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(item.srNo.toString(), color = Color.DarkGray,modifier = Modifier
                                .width(100.dp))
                            Text(item.itemCode, color = Color.DarkGray,modifier = Modifier
                                .width(100.dp))
                            Text(item.rfidCode, color = Color.DarkGray,modifier = Modifier
                                .width(100.dp))
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

            // Bottom Summary
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exist Items: 1,256", color = Color.White)
                Text("Total Items: 1,256", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopBar(
    title: String,
    navigationIcon: @Composable () -> Unit,
    onSave: () -> Unit,
    onList: () -> Unit,
    onScan: () -> Unit,
    onGscan: () -> Unit,
    onReset: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                )
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            navigationIcon()
            Text(
                text = title,
                color = Color.White,
                style = TextStyle(fontSize = 20.sp),
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Save",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.Gray),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .widthIn(min = 120.dp) // Set a minimum width
                .padding(horizontal = 4.dp)
                .background(Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$label: $selectedOption",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand $label dropdown",
                    tint = Color.DarkGray
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp, color = Color.DarkGray) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}