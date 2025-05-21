package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens

// 1) Data class for each row
data class FormField(
    val label: String,
    val isDropdown: Boolean,
    val options: List<String> = emptyList()

)


// 2) The list of fields you want
private val sampleFields = listOf(
    FormField("EPC",         isDropdown = false),
    FormField("Vendor",      isDropdown = true,  options = listOf("Acme", "Zenith", "Global")),
    FormField("SKU",         isDropdown = true,  options = listOf("123","456","789")),
    FormField("Itemcode",    isDropdown = false),
    FormField("RFIDcode",    isDropdown = false),
    FormField("Category",    isDropdown = true,  options = listOf("A","B","C")),
    FormField("Product",     isDropdown = true,  options = listOf("Prod 1","Prod 2")),
    FormField("Design",      isDropdown = true,  options = listOf("X","Y","Z")),
    FormField("Purity",      isDropdown = true,  options = listOf("High","Med","Low")),
    FormField("Gross Weight",isDropdown = false),
    FormField("Stone Weight",isDropdown = false),
    FormField("Diamond Weight",isDropdown = false),
    FormField("Net Weight",isDropdown = false),
    FormField("Making/Gram",isDropdown = false),
    FormField("Making %",isDropdown = false),
    FormField("Fix Making",isDropdown = false),
    FormField("Fix Wastage",isDropdown = false),
    FormField("Stone Amount",isDropdown = false),
    FormField("Diamond Amount",isDropdown = false),
    FormField("Image Upload",isDropdown = false)
)

// 3) The form container

@Composable
fun AddProductScreen(onBack: () -> Unit, navController: NavHostController) {
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
                onSave  = { /*...*/ },
                onList = { navController.navigate(Screens.ProductListScreen.route) },
                onScan  = { /*...*/ },
                onGscan = { /*...*/ },
                onReset = { /*...*/ }
            )
        }
    // Ensure Scaffold takes full screen
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0)) // Light gray background for debugging
                .padding(innerPadding) // Respect Scaffold padding
                .padding(horizontal = 16.dp, vertical = 12.dp), // Horizontal padding for aesthetics
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = rememberLazyListState() // Explicit scroll state
        ) {
            items(sampleFields) { field ->
                FormRow(field)
            }



        }
    }
}
// 4) Single row with text or dropdown
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
                    IconButton(onClick = onSave) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            tint = Color.DarkGray,
                            contentDescription = "Save",
                        )
                    }
                    IconButton(onClick = onList) {
                        Icon(
                            painter = painterResource(R.drawable.ic_list),
                            tint = Color.DarkGray,
                            contentDescription = "List"
                        )
                    }
                    Spacer(modifier = Modifier.width(64.dp)) // space for center button
                    IconButton(onClick = onGscan) {
                        Icon(
                            painter = painterResource(R.drawable.ic_gscan),
                            tint = Color.DarkGray,
                            contentDescription = "Gscan"
                        )
                    }
                    IconButton(onClick = onReset) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reset),
                            tint = Color.DarkGray,
                            contentDescription = "Reset"
                        )
                    }
                }

                // 2) The elevated circular Scan button, centered
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-24).dp)     // lift it above the row
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF5231A7), Color(0xFFD32940))
                            )
                        )
                        .clickable(onClick = onScan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_scan),
                        contentDescription = "Scan",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }


            }


}


@Composable
private fun FormRow(field: FormField) {
    // build local state
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Text(
            text = field.label,
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(0.8f)
        )

        // Input container
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .clickable { if (field.isDropdown) expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (field.isDropdown) {
                Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (text.isEmpty()) "Tap to enter…" else text,
                            fontSize = 16.sp,
                            color = if (text.isEmpty()) Color.LightGray else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        field.options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    text = option
                                    expanded = false
                                }
                            )
                        }
                    }
                } else {
                if (!field.label.equals("Image Upload")) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        decorationBox = { inner ->
                            if (text.isEmpty()) {
                                Text("Tap to enter…", fontSize = 14.sp, color = Color.LightGray)
                            }
                            inner()
                        }
                    )
                }else{
                    Icon(
                        painter = painterResource(R.drawable.ic_gscan),
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            }
    }
}

