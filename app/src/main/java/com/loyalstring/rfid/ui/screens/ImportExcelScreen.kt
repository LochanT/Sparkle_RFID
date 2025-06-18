package com.loyalstring.rfid.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.FilePickerDialog
import com.loyalstring.rfid.ui.utils.MappingDialogWrapper
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.ImportExcelViewModel
import kotlinx.coroutines.launch

@Composable
fun ImportExcelScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
    val viewModel: ImportExcelViewModel = hiltViewModel()
    val context: Context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var excelColumns by remember { mutableStateOf(listOf<String>()) }
    var showMappingDialog by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(true) }
    var fileSelected by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showOverlay by remember { mutableStateOf(false) }

    val isImportDone by viewModel.isImportDone.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val isDone by viewModel.isImportDone.collectAsState()

    val bulkItemFieldNames = listOf(
        "productName",
        "itemCode",
        "rfid",
        "grossWeight",
        "stoneWeight",
        "dustWeight",
        "netWeight",
        "category",
        "design",
        "purity",
        "makingPerGram",
        "makingPercent",
        "fixMaking",
        "fixWastage",
        "stoneAmount",
        "dustAmount",
        "sku",
        "epc",
        "vendor",
        "tid",
        "box",
        "designCode",
        "productCode",
        "uhftagInfo"
    )

    LaunchedEffect(isImportDone) {
        if (isImportDone) {
            showOverlay = false
            showProgress = false
            val message = if (importProgress.failedFields.isEmpty()) {
                "✅ Import successful: ${importProgress.importedFields} fields"
            } else {
                "⚠️ Imported with errors: ${importProgress.failedFields.joinToString()}"
            }
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            navController.navigate(Screens.ProductManagementScreen.route) {
                popUpTo(Screens.ImportExcelScreen.route) { inclusive = true }
            }
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val headers = viewModel.parseExcelHeaders(context, it)
                excelColumns = headers
                showMappingDialog = true
                showProgress = true
                selectedUri = it
                viewModel.setSelectedFile(it)
            }
        }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showFilePicker) {
                FilePickerDialog(
                    onDismiss = {
                        showFilePicker = false
                        navController.navigate(Screens.ProductManagementScreen.route)
                    },
                    onFileSelected = {
                        showFilePicker = false
                        fileSelected = true
                        launcher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-excel"
                            )
                        )
                    }
                )
            }

            if (showMappingDialog) {
                showProgress = false
                MappingDialogWrapper(
                    excelColumns = excelColumns,
                    bulkItemFields = bulkItemFieldNames,
                    onDismiss = {
                        showMappingDialog = false
                        navController.navigate(Screens.ProductManagementScreen.route)
                    },
                    fileSelected = fileSelected,
                    onImport = { mapping ->
                        selectedUri?.let {
                            showOverlay = true
                            viewModel.importMappedData(context, mapping)
                            showMappingDialog = false
                        }
                    },
                    isFromSheet = false
                )
            }

            if (showOverlay) {
                ExcelImportProgressOverlay(importProgress = importProgress)
            }

            // ✅ This block is your actual import progress visualization
            if (!showMappingDialog && !showFilePicker && showProgress) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (importProgress.totalFields > 0 && !isDone) {
                        LinearProgressIndicator(
                            progress = { importProgress.importedFields.toFloat() / importProgress.totalFields },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Importing ${importProgress.importedFields} of ${importProgress.totalFields}...",
                            fontFamily = poppins
                        )
                    }

                    if (isDone) {
                        Text(
                            "✅ Imported ${importProgress.importedFields} items",
                            fontFamily = poppins
                        )
                        if (importProgress.failedFields.isNotEmpty()) {
                            Text(
                                "⚠️ Failed fields: ${importProgress.failedFields.joinToString()}",
                                fontFamily = poppins
                            )
                        }
                    }
                }
            }
        }

    }
}
