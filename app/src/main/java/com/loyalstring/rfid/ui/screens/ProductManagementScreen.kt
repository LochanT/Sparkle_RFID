package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.navigation.GradientTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
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
                onSave  = { /*...*/ },
                onList  = { /*...*/ },
                onScan  = { /*...*/ },
                onGscan = { /*...*/ },
                onReset = { /*...*/ }
            )
        }
    ) { innerPadding ->
        // Apply BOTH the topBar + bottomBar insets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)    // <-- this is crucial!
                .background(Color.White)
        ) {
            LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                contentPadding       = PaddingValues(16.dp),
                verticalArrangement   = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier              = Modifier.fillMaxSize()
            ) {
                val items = listOf(
                    ProductGridItem("Add Single\nProduct", R.drawable.add_single_prod, true,"add products"),
                    ProductGridItem("Add Bulk\nProducts", R.drawable.add_bulk_prod, true,"bulk products"),
                    ProductGridItem("Import\nExcel", R.drawable.export_excel,false,""),
                    ProductGridItem("Export\nExcel", R.drawable.export_excel,false,""),
                    ProductGridItem("Click to\nSync Data", R.drawable.ic_sync_data,false,""),
                    ProductGridItem("Scan to\nDesktop", R.drawable.ic_sync_sheet_data,false,""),
                    ProductGridItem("Click to\nSync Sheet Data", R.drawable.barcode_reader,false,""),
                    ProductGridItem("Click to Upload\nData to Server", R.drawable.upload_data,false,"")
                )
                items(items) { item ->
                    ProductGridCard(item, navController)
                }
            }
        }
    }
}





@Composable
fun ProductGridCard(item: ProductGridItem, navController: NavHostController) {
    val cardColors = if (item.isGradient) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
        )
    } else {
        SolidColor(Color.DarkGray)

    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardColors)
                .fillMaxSize()
                .clickable {
                    navController.navigate(item.route)
                }
                .padding(16.dp, 40.dp, 16.dp, 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.label,
                    tint = if (item.isGradient) Color.Unspecified else Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
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
    val route: String,
)