package com.loyalstring.rfid.ui.screens

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.db.AppDatabase
import com.loyalstring.rfid.data.model.ClientCodeRequest

import com.loyalstring.rfid.data.model.login.Employee

import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.ui.utils.AutoSyncSetting
import com.loyalstring.rfid.ui.utils.GradientButton
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.utils.BackupUtils
import com.loyalstring.rfid.viewmodel.SettingsViewModel
import com.loyalstring.rfid.viewmodel.UiState1
import kotlinx.coroutines.launch
import android.content.Intent

import android.os.Environment
import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

import android.Manifest
import android.content.ActivityNotFoundException

import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.loyalstring.rfid.worker.EmailSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


// ---------------- MENU ITEM TYPES ----------------
sealed class SettingType {
    object Counter : SettingType()
    object Action : SettingType()
}

data class SettingsMenuItem(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val type: SettingType,
    val defaultValue: Int? = null,
    val subtitle: String? = null,
    val onClick: (() -> Unit)? = null
)

// ---------------- SETTINGS SCREEN ----------------
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    userPreferences: UserPreferences
) {
    val viewModel: SettingsViewModel = hiltViewModel()

    var showSheetInput by remember { mutableStateOf(false) }
    var showAutoSyncDialog by remember { mutableStateOf(false) }
    var sheetUrl by remember { mutableStateOf(userPreferences.getSheetUrl()) }

    var showRatesEditor by remember { mutableStateOf(false) }

    val updateState = viewModel.updateDailyRatesState.collectAsState()

    var showCustomApiDialog by remember { mutableStateOf(false) }
    var customApi by remember { mutableStateOf("") }

    var showBackupDialog by remember { mutableStateOf(false) }

    val context: Context = LocalContext.current
    val employee = remember {
        // get your logged-in employee so we have ClientCode/EmployeeCode
        UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    }

    var showEmailDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var savedEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(updateState.value) {
        when (val s = updateState.value) {
            is UiState1.Success -> {
                ToastUtils.showToast(context, "Rates updated successfully")
                // refresh list
                val emp = UserPreferences.getInstance(context)
                    .getEmployee(Employee::class.java)
                emp?.clientCode?.let { cc ->
                    viewModel.getDailyRate(ClientCodeRequest(cc))
                }
                // close dialog & reset state
                showRatesEditor = false
                viewModel.resetUpdateState()
            }

            is UiState1.Failure -> { // If your UiState still uses Error, change to UiState.Error
                ToastUtils.showToast(context, s.message)
                viewModel.resetUpdateState()
            }

            UiState1.Loading, UiState1.Idle -> Unit
            else -> {}
        }
    }


    val scope = rememberCoroutineScope()

    val menuItems = listOf(
        // Counters (first 5)
        SettingsMenuItem(
            "product_count",
            "Product",
            Icons.Default.Settings,
            SettingType.Counter,
            5
        ),
        SettingsMenuItem(
            "inventory_count",
            "Inventory",
            Icons.Default.Settings,
            SettingType.Counter,
            30
        ),
        SettingsMenuItem("search_count", "Search", Icons.Default.Settings, SettingType.Counter, 30),
        SettingsMenuItem("orders_count", "Orders", Icons.Default.Settings, SettingType.Counter, 10),
        SettingsMenuItem(
            "stock_transfer_count",
            "Stock transfer",
            Icons.Default.Settings,
            SettingType.Counter,
            10
        ),

        // Actions
        SettingsMenuItem(
            "rates",
            stringResource(id = R.string.menu_rates_title),
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = stringResource(id = R.string.menu_rates_subtitle)
        ) {
            //showRatesEditor  = true
            navController.navigate(Screens.DailyRatesEditorScreen.route)
        },
        SettingsMenuItem(
            "account",
            "Account",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Username & Password"
        ) {
            /// navController.navigate(Screens.Account.route)
        },
        SettingsMenuItem(
            "permissions",
            "Users and permissions",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Manage permissions"
        ),
        SettingsMenuItem(
            "email",
            "Email",
            Icons.Default.Attachment,
            SettingType.Action,
            subtitle = "Email configuration"
        ),
        SettingsMenuItem(
            "backup",
            "Backup",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Data Backup"
        ) {

            showBackupDialog = true
            /*  scope.launch {
                try {
                    val dbFile = context.getDatabasePath("app_db")
                    val db = SQLiteDatabase.openDatabase(
                        dbFile.absolutePath,
                        null,
                        SQLiteDatabase.OPEN_READONLY
                    )

                    BackupUtils.exportRoomDatabaseToCsv(context, db)
                    db.close()

                } catch (e: Exception) {
                    ToastUtils.showToast(context, "Backup failed: ${e.message}")
                }
            }*/
        },
        SettingsMenuItem(
            "autosync",
            "Auto Sync",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Enable automatic sync"
        ),
        SettingsMenuItem(
            "notifications",
            "Notifications",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Notification settings"
        ),
        SettingsMenuItem(
            "branches",
            "Branches",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Branch management"
        ),
        SettingsMenuItem(
            key = "apis",
            title = stringResource(id = R.string.menu_apis_title),
            icon = Icons.Default.Settings,
            type = SettingType.Action,
            subtitle = stringResource(id = R.string.menu_apis_subtitle)
        ) {
            showCustomApiDialog = true
        },
        SettingsMenuItem(
            "sheet_url",
            "Sheet URL",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Set Google Sheet URL"
        ),
        SettingsMenuItem(
            "stock_transfer_url",
            "Stock Transfer URL",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Stock Transfer API URL"
        )
    )

    Scaffold(
        topBar = {
            GradientTopBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                showCounter = false,
                selectedCount = 0,
                onCountSelected = {}
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            items(menuItems) { item ->
                MenuItemRow(
                    item = item,
                    userPreferences = userPreferences,
                    onAutoSyncClick = { showAutoSyncDialog = true },
                    onSheetUrlClick = { showSheetInput = true }
                )
            }
        }
    }

    if (showSheetInput) {
        sheetUrl?.let {
            SheetInputDialog(
                sheetUrl = it,
                onValueChange = { sheetUrl = it },
                onDismiss = { showSheetInput = false },
                onSetClick = {
                    viewModel.updateSheetUrl(sheetUrl!!)
                    userPreferences.saveSheetUrl(sheetUrl!!)
                    showSheetInput = false
                    ToastUtils.showToast(context, "Sheet URL updated successfully")
                }
            )
        }
    }

    if (showCustomApiDialog) {

        CustomApiDialog(
            onDismiss = { showCustomApiDialog = false },
            onSave = { newApi ->
                customApi = newApi
                userPreferences.saveCustomApi(newApi)
                ToastUtils.showToast(context, "Custom API saved!")
                showCustomApiDialog = false
            }
        )
    }

    if (showBackupDialog) {
        BackupDialogExample(
            onDismiss = { showBackupDialog = false },
            scope = scope
            // userPreferences:userPreferences
        )
    }
}
@Composable
fun BackupDialogExample(
    onDismiss: () -> Unit,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var showEmailDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var savedEmail by remember { mutableStateOf<String?>(null) }

    // ----------------------------------------
    // ✅ Declare launcher at top level
    // ----------------------------------------
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, "restore_temp.csv")
                inputStream?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                restoreBackupFromCsv(context, tempFile)
            } catch (e: Exception) {
                ToastUtils.showToast(context, "❌ Restore failed: ${e.message}")
            }
        }
    }

    // ----------------------------------------
    // ✅ Restore Function
    // ----------------------------------------
    fun restoreBackupFromCsv(context: Context, csvFile: File) {
        try {
            val dbFile = context.getDatabasePath("app_db")
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)

            val lines = csvFile.readLines()
            if (lines.isEmpty()) {
                ToastUtils.showToast(context, "⚠️ Backup file is empty.")
                db.close()
                return
            }

            val headers = lines.first().split(",")
            val data = lines.drop(1)

            db.beginTransaction()
            db.delete("items", null, null) // clear table before restore

            val insertQuery =
                "INSERT INTO items (${headers.joinToString(",")}) VALUES (${headers.joinToString(",") { "?" }})"

            val stmt = db.compileStatement(insertQuery)
            data.forEach { row ->
                val values = row.split(",")
                stmt.clearBindings()
                values.forEachIndexed { i, v ->
                    stmt.bindString(i + 1, v.trim())
                }
                stmt.executeInsert()
            }

            db.setTransactionSuccessful()
            db.endTransaction()
            db.close()
            ToastUtils.showToast(context, "✅ Data restored successfully.")
        } catch (e: Exception) {
            ToastUtils.showToast(context, "❌ Restore failed: ${e.message}")
        }
    }

    // ----------------------------------------
    // ✅ Email Sending Function
    // ----------------------------------------
    fun sendBackupEmail(
        context: Context,
        scope: CoroutineScope,
        recipientEmail: String,
        onDismiss: () -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val exportDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "DatabaseBackup"
                )
                if (!exportDir.exists()) exportDir.mkdirs()

                val file = File(exportDir, "Backup_All.csv")
                val dbFile = context.getDatabasePath("app_db")
                val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                BackupUtils.exportRoomDatabaseToCsv(context, db)
                db.close()
                delay(1500)

                if (!file.exists() || file.length() == 0L) {
                    withContext(Dispatchers.Main) {
                        ToastUtils.showToast(context, "⚠️ Backup file not found or empty.")
                    }
                    return@launch
                }

                EmailSender.sendEmailWithAttachment(
                    toEmails = listOf(recipientEmail),
                    subject = "SparkleERP Backup",
                    body = "Here’s your latest backup file.",
                    attachments = mapOf("Backup_All.csv" to file)
                )

                withContext(Dispatchers.Main) {
                    ToastUtils.showToast(context, "✅ Backup email sent successfully to $recipientEmail")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ToastUtils.showToast(context, "❌ Failed: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) { onDismiss() }
            }
        }
    }

    // ----------------------------------------
    // ✅ UI
    // ----------------------------------------
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup Options") },
        text = { Text("Choose how you’d like to back up your data:") },
        confirmButton = {
            Column {
                // 📂 Save to Device
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val dbFile = context.getDatabasePath("app_db")
                                val db = SQLiteDatabase.openDatabase(
                                    dbFile.absolutePath,
                                    null,
                                    SQLiteDatabase.OPEN_READONLY
                                )

                                BackupUtils.exportRoomDatabaseToCsv(context, db)
                                db.close()

                                ToastUtils.showToast(context, "✅ Backup saved locally.")
                            } catch (e: Exception) {
                                ToastUtils.showToast(context, "❌ Backup failed: ${e.message}")
                            } finally {
                                onDismiss()
                            }
                        }
                    }
                ) { Text("📂 Save to Device") }

                // 📧 Send via Email
                TextButton(onClick = {
                    val activity = context.findActivity() ?: return@TextButton
                    if (!ensureStoragePermission(context, activity)) {
                        ToastUtils.showToast(context, "⚠️ Please grant storage permission to send backup.")
                        return@TextButton
                    }

                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    inputEmail = prefs.getString("backup_email", "") ?: ""
                    showEmailDialog = true
                }) {
                    Text("📧 Send via Email")
                }

                // 🔄 Restore Backup
                TextButton(onClick = {
                    restoreFileLauncher.launch("text/*")
                }) {
                    Text("🔄 Restore Backup")
                }

                // 📥 Email Input Dialog
                if (showEmailDialog) {
                    AlertDialog(
                        onDismissRequest = { showEmailDialog = false },
                        title = { Text("Enter Email Address") },
                        text = {
                            TextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it },
                                label = { Text("Email") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putString("backup_email", inputEmail).apply()
                                    savedEmail = inputEmail
                                    showEmailDialog = false
                                    ToastUtils.showToast(context, "📤 Sending backup…")

                                    sendBackupEmail(context, scope, inputEmail, onDismiss)
                                } else {
                                    ToastUtils.showToast(context, "⚠️ Please enter a valid email address.")
                                }
                            }) { Text("Save & Send") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEmailDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}




// ---------------------------------------------------------
// 🔹 Restore Function — Reads Backup CSV → Restores to DB
// ---------------------------------------------------------
fun restoreBackupFromCsv(context: Context, csvFile: File) {
    try {
        val dbFile = context.getDatabasePath("app_db")
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)

        db.beginTransaction()
        val reader = csvFile.bufferedReader()
        val lines = reader.readLines()

        if (lines.isEmpty()) {
            ToastUtils.showToast(context, "⚠️ Backup file is empty.")
            db.close()
            return
        }

        val header = lines.first().split(",")
        val dataRows = lines.drop(1)

        // Example: Assume your table name is “items”
        db.delete("items", null, null) // clear table before restore

        val insertQuery =
            "INSERT INTO items (${header.joinToString(",")}) VALUES (${header.joinToString(",") { "?" }})"

        val stmt = db.compileStatement(insertQuery)
        dataRows.forEach { line ->
            val values = line.split(",")
            stmt.clearBindings()
            values.forEachIndexed { index, value ->
                stmt.bindString(index + 1, value.trim())
            }
            stmt.executeInsert()
        }

        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()

        ToastUtils.showToast(context, "✅ Data restored successfully!")

    } catch (e: Exception) {
        ToastUtils.showToast(context, "❌ Restore failed: ${e.message}")
    }
}



private fun ensureStoragePermission(context: Context, activity: Activity): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                ),
                100
            )
        }
        hasPermission
    } else {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        }
        hasPermission
    }
}


@Composable
fun CustomApiDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences.getInstance(context) }

    // 🔹 Load the saved custom API from SharedPreferences
    val savedUrl = remember { mutableStateOf(userPrefs.getCustomApi().orEmpty()) }

    var input by remember { mutableStateOf(savedUrl.value) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = stringResource(R.string.dialog_custom_api_title),
                fontSize = 16.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = {
                        Text(
                            text = stringResource(R.string.hint_custom_api),
                            fontSize = 14.sp,
                            fontFamily = poppins
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GradientButton(
                    text = stringResource(R.string.button_save),
                    onClick = {
                        if (input.isNotBlank()) {
                            userPrefs.saveCustomApi(input)  // ✅ Save in SharedPreferences
                            onSave(input)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    )
}


// ---------------- MENU ROW ----------------
@Composable
fun MenuItemRow(
    item: SettingsMenuItem,
    userPreferences: UserPreferences,
    onAutoSyncClick: () -> Unit,
    onSheetUrlClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember {
        mutableStateOf(userPreferences.getInt(item.key, item.defaultValue ?: 0))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                when (item.key) {
                    "autosync" -> onAutoSyncClick()
                    "sheet_url" -> onSheetUrlClick()
                    else -> item.onClick?.invoke()
                }
            },
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        color = Color(0xFFF7F7F7)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )

            when (item.type) {
                is SettingType.Counter -> {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                            .clickable { expanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedValue.toString(), fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .height(300.dp)
                            .width(60.dp)
                    ) {
                        (1..30).forEach { count ->
                            DropdownMenuItem(
                                text = { Text(count.toString()) },
                                onClick = {
                                    selectedValue = count
                                    userPreferences.saveInt(item.key, count)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                is SettingType.Action -> {
                    Text(item.subtitle ?: "", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}

// ---------------- SHEET URL DIALOG ----------------
@Composable
fun SheetInputDialog(
    sheetUrl: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSetClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Set Sheet URL", fontFamily = poppins, fontSize = 14.sp) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = sheetUrl,
                    onValueChange = onValueChange,
                    label = { Text("Enter Sheet Url", fontFamily = poppins, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GradientButton(
                    text = "Set Sheet Id",
                    onClick = onSetClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    )


}














