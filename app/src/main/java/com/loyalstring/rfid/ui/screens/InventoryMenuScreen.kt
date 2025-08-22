package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.BulkViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun InventoryMenuScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    bulkViewModel: BulkViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Cached lists
    val counters by bulkViewModel.counters.collectAsState()
    val branches by bulkViewModel.branches.collectAsState()
    val boxes by bulkViewModel.boxes.collectAsState()
    val exhibitions by bulkViewModel.exhibitions.collectAsState()

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogItems by remember { mutableStateOf(listOf<String>()) }
    var onItemSelected by remember { mutableStateOf<(String) -> Unit>({}) }

    LaunchedEffect(Unit) {
        showDialog = false
        dialogTitle = ""
        dialogItems = emptyList()
    }



    val menuItems = listOf(
        "Scan Display" to R.drawable.scan_barcode,
        "Scan Counter" to R.drawable.scan_counter,
        "Scan Box" to R.drawable.scan_box,
        "Scan Branch" to R.drawable.scan_branch,
        "Exhibition" to R.drawable.scan_exhibition
    )

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Inventory",
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            menuItems.forEach { (title, icon) ->
                MenuButton(title = title, icon = icon) {
                    when (title) {
                        "Scan Display" -> {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("filterType", "Scan Display")
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("filterValue", "Scan Display")
                            navController.navigate(Screens.ScanDisplayScreen.route)
                        }

                        "Scan Counter" -> {
                            // 🔥 Load data in background, then open dialog
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    if (counters.isEmpty()) {
                                        ToastUtils.showToast(
                                            context,
                                            "No counters available"
                                        )
                                    } else {
                                        dialogTitle = "Select Counter"
                                        dialogItems = counters
                                        onItemSelected = { selected ->
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterType", "Counter")
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterValue", selected)
                                            navController.navigate(Screens.ScanDisplayScreen.route)
                                        }
                                        showDialog = true
                                    }
                                }
                            }
                        }

                        "Scan Branch" -> {
                            CoroutineScope(Dispatchers.IO).launch {

                                withContext(Dispatchers.Main) {
                                    if (branches.isEmpty()) {
                                        ToastUtils.showToast(
                                            context,
                                            "No branches available"
                                        )
                                    } else {
                                        dialogTitle = "Select Branch"
                                        dialogItems = branches
                                        onItemSelected = { selected ->
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterType", "Branch")
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterValue", selected)
                                            navController.navigate(Screens.ScanDisplayScreen.route)
                                        }
                                        showDialog = true
                                    }
                                }
                            }
                        }

                        "Scan Box" -> {
                            CoroutineScope(Dispatchers.IO).launch {

                                withContext(Dispatchers.Main) {
                                    if (boxes.isEmpty()) {
                                        ToastUtils.showToast(
                                            context,
                                            "No boxes available"
                                        )
                                    } else {
                                        dialogTitle = "Select Box"
                                        dialogItems = boxes
                                        onItemSelected = { selected ->
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterType", "Box")
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterValue", selected)
                                            navController.navigate(Screens.ScanDisplayScreen.route)
                                        }
                                        showDialog = true
                                    }
                                }
                            }
                        }

                        "Exhibition" -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    if (exhibitions.isEmpty()) {
                                        ToastUtils.showToast(
                                            context,
                                            "No exhibitions branch available"
                                        )
                                    } else {
                                        dialogTitle = "Select Exhibition"
                                        dialogItems = exhibitions
                                        onItemSelected = { selected ->
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterType", "Exhibition")
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("filterValue", selected)
                                            navController.navigate(Screens.ScanDisplayScreen.route)
                                        }
                                        showDialog = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        SelectionDialog(
            title = dialogTitle,
            items = dialogItems,
            onDismiss = { showDialog = false },
            onSelect = {
                showDialog = false
                onItemSelected(it)
            }
        )
    }
}


@Composable
fun MenuButton(title: String, icon: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B363E)),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier
                    .size(75.dp)
                    .padding(end = 16.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = poppins
            )
        }
    }
}

@Composable
fun SelectionDialog(
    title: String,
    items: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onAddClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select $title",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Row {
                        if (onAddClick != null) {
                            IconButton(onClick = onAddClick) {
                                Icon(
                                    painter = painterResource(id = R.drawable.vector_add),
                                    contentDescription = "Add $title",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // List of items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = item,
                                fontFamily = poppins,
                                fontSize = 14.sp,
                                color = Color(0xFF3B363E)
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = MaterialTheme.shapes.medium
    )
}

