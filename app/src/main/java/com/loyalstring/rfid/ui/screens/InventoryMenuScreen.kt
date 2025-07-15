package com.loyalstring.rfid.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.BoxModel
import com.loyalstring.rfid.data.model.addSingleItem.BranchModel
import com.loyalstring.rfid.data.model.addSingleItem.CounterModel
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.SingleProductViewModel

@Composable
fun InventoryMenuScreen(onBack: () -> Unit, navController: NavHostController) {
    val menuItems = listOf(
        "Scan Display" to R.drawable.scan_barcode,
        "Scan Counter" to R.drawable.scan_counter,
        "Scan Box" to R.drawable.scan_box,
        "Scan Branch" to R.drawable.scan_branch,
        "Exhibition" to R.drawable.scan_exhibition
    )

    var showCounterDialog by remember { mutableStateOf(false) }
    var showBranchDialog by remember { mutableStateOf(false) }
    var showBoxDialog by remember { mutableStateOf(false) }
    var showExhibitionDialog by remember { mutableStateOf(false) }

    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    val context: Context = LocalContext.current

    val counters by remember { derivedStateOf { singleProductViewModel.counters } }
    val branches by remember { derivedStateOf { singleProductViewModel.branches } }
    val boxes by remember { derivedStateOf { singleProductViewModel.boxes } }
    val exhibitions by remember { derivedStateOf { singleProductViewModel.exhibitions } }

    val selectedCounters = remember { mutableStateListOf<CounterModel>() }
    val selectedBranches = remember { mutableStateListOf<BranchModel>() }
    val selectedBoxes = remember { mutableStateListOf<BoxModel>() }
    val selectedExhibition = remember { mutableStateListOf<BranchModel>() }
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)

    val hasShownCounterDialog = remember { mutableStateOf(false) }
    val hasShownBranchDialog = remember { mutableStateOf(false) }
    val hasShownBoxDialog = remember { mutableStateOf(false) }
    val hasShownExhibitionDialog = remember { mutableStateOf(false) }

    LaunchedEffect(counters) {
        if (counters.isNotEmpty() && !hasShownCounterDialog.value) {
            showCounterDialog = true
            hasShownCounterDialog.value = true
        }
    }

    LaunchedEffect(branches) {
        if (branches.isNotEmpty() && !hasShownBranchDialog.value) {
            showBranchDialog = true
            hasShownBranchDialog.value = true
        }
    }

    LaunchedEffect(boxes) {
        if (boxes.isNotEmpty() && !hasShownBoxDialog.value) {
            showBoxDialog = true
            hasShownBoxDialog.value = true
        }
    }

    LaunchedEffect(exhibitions) {
        if (exhibitions.isNotEmpty() && !hasShownExhibitionDialog.value) {
            showExhibitionDialog = true
            hasShownExhibitionDialog.value = true
        }
    }



    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Inventory",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
            )
        },
        bottomBar = {
            ScanBottomBar(
                onSave = { /* Save logic */ },
                onList = {
                    navController.navigate(Screens.ProductListScreen.route)
                },
                onScan = { /* Scan logic */ },
                onGscan = { /* Gscan logic */ },
                onReset = { /* Reset logic */ }
            )
        }
    ) { innerPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                menuItems.forEach { (title, icon) ->
                    MenuButton(
                        title = title,
                        icon = icon,
                        onClick = {
                            when (title) {
                                "Scan Display" -> {
                                    navController.navigate(Screens.ScanDisplayScreen.route)
                                }

                                "Scan Counter" -> {
                                    employee?.clientCode?.let {
                                        selectedCounters.clear()
                                        showCounterDialog = false
                                        singleProductViewModel.getAllCounters(ClientCodeRequest(it))
                                    }
                                }

                                "Scan Branch" -> {
                                    employee?.clientCode?.let {
                                        selectedBranches.clear()
                                        showBranchDialog = false
                                        singleProductViewModel.getAllBranches(ClientCodeRequest(it))
                                    }
                                }

                                "Scan Box" -> {
                                    employee?.clientCode?.let {
                                        selectedBoxes.clear()
                                        showBoxDialog = false
                                        singleProductViewModel.getAllBoxes(ClientCodeRequest(it))
                                    }
                                }

                                "Scan Exhibition" -> {
                                    employee?.clientCode?.let {
                                        selectedExhibition.clear()
                                        showExhibitionDialog = false
                                        singleProductViewModel.getAllExhibitions(
                                            ClientCodeRequest(
                                                it
                                            )
                                        )
                                    }
                                }


                            }
                        }
                    )
                }
            }
            //*********************************SHOW COUNTER DIALOG**************************************************//

            if (showCounterDialog) {
                AlertDialog(
                    onDismissRequest = { showCounterDialog = false },
                    title = { Text("Select Counters", fontFamily = poppins) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            counters.forEach { counter ->
                                val isChecked =
                                    remember { mutableStateOf(selectedCounters.contains(counter)) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isChecked.value = !isChecked.value
                                            if (isChecked.value) selectedCounters.add(counter)
                                            else selectedCounters.remove(counter)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked.value,
                                        onCheckedChange = {
                                            isChecked.value = it
                                            if (it) selectedCounters.add(counter)
                                            else selectedCounters.remove(counter)
                                        }
                                    )
                                    Text(
                                        text = counter.CounterName,
                                        modifier = Modifier.padding(start = 2.dp),
                                        fontSize = 13.sp,
                                        fontFamily = poppins
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showCounterDialog = false
                            val safeList = selectedCounters.map { it }
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedCounters", safeList)
                            navController.navigate(Screens.ScanCounterScreen.route)
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCounterDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            //*********************************SHOW BRANCH DIALOG**************************************************//
            if (showBranchDialog) {
                AlertDialog(
                    onDismissRequest = { showBranchDialog = false },
                    title = { Text("Select Branch", fontFamily = poppins) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            branches.forEach { branch ->
                                val isChecked =
                                    remember { mutableStateOf(selectedBranches.contains(branch)) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isChecked.value = !isChecked.value
                                            if (isChecked.value) selectedBranches.add(branch)
                                            else selectedBranches.remove(branch)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked.value,
                                        onCheckedChange = {
                                            isChecked.value = it
                                            if (it) selectedBranches.add(branch)
                                            else selectedBranches.remove(branch)
                                        }
                                    )
                                    Text(
                                        text = branch.BranchName,
                                        modifier = Modifier.padding(start = 2.dp),
                                        fontSize = 13.sp,
                                        fontFamily = poppins
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showBranchDialog = false
                            val safeList = selectedBranches.map { it }
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedBranches", safeList)
                            navController.navigate(Screens.ScanBranchScreen.route)
                        }) {
                            Text("OK", fontFamily = poppins)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBranchDialog = false }) {
                            Text("Cancel", fontFamily = poppins)
                        }
                    }
                )
            }
//*********************************SHOW BOX DIALOG**************************************************//
            if (showBoxDialog) {
                AlertDialog(
                    onDismissRequest = { showBoxDialog = false },
                    title = { Text("Select Box", fontFamily = poppins) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            boxes.forEach { box ->
                                val isChecked =
                                    remember { mutableStateOf(selectedBoxes.contains(box)) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isChecked.value = !isChecked.value
                                            if (isChecked.value) selectedBoxes.add(box)
                                            else selectedBoxes.remove(box)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked.value,
                                        onCheckedChange = {
                                            isChecked.value = it
                                            if (it) selectedBoxes.add(box)
                                            else selectedBoxes.remove(box)
                                        }
                                    )
                                    Text(
                                        text = box.BoxName,
                                        modifier = Modifier.padding(start = 2.dp),
                                        fontSize = 13.sp,
                                        fontFamily = poppins
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showBoxDialog = false
                            val safeList = selectedBoxes.map { it }
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedBoxes", safeList)
                            navController.navigate(Screens.ScanBoxScreen.route)
                        }) {
                            Text("OK", fontFamily = poppins)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBoxDialog = false }) {
                            Text("Cancel", fontFamily = poppins)
                        }
                    }
                )
            }
            /******************************************EXHIBITION DIALOG**********************************************************/
            if (showExhibitionDialog) {
                AlertDialog(
                    onDismissRequest = { showExhibitionDialog = false },
                    title = { Text("Select Exhibition", fontFamily = poppins) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            branches.forEach { branch ->
                                val isChecked =
                                    remember { mutableStateOf(selectedExhibition.contains(branch)) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isChecked.value = !isChecked.value
                                            if (isChecked.value) selectedExhibition.add(branch)
                                            else selectedExhibition.remove(branch)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked.value,
                                        onCheckedChange = {
                                            isChecked.value = it
                                            if (it) selectedExhibition.add(branch)
                                            else selectedExhibition.remove(branch)
                                        }
                                    )
                                    Text(
                                        text = branch.BranchType,
                                        modifier = Modifier.padding(start = 2.dp),
                                        fontSize = 13.sp,
                                        fontFamily = poppins
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showExhibitionDialog = false
                            val safeList = selectedExhibition.map { it }
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedExhibition", safeList)
                            navController.navigate(Screens.ScanBranchScreen.route)
                        }) {
                            Text("OK", fontFamily = poppins)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBranchDialog = false }) {
                            Text("Cancel", fontFamily = poppins)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MenuButton(title: String, icon: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B363E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
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
                    .size(35.dp)
                    .padding(end = 16.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = poppins
            )
        }
    }
}

