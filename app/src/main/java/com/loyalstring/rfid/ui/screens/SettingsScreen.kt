package com.loyalstring.rfid.ui.screens

import android.content.Context
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.navigation.GradientTopBar
import com.loyalstring.rfid.ui.utils.AutoSyncSetting
import com.loyalstring.rfid.ui.utils.GradientButton
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.poppins
import com.loyalstring.rfid.viewmodel.SettingsViewModel

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
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    val context: Context = LocalContext.current
    val employee =
        remember { UserPreferences.getInstance(context).getEmployee(Employee::class.java) }

    Log.d("EMPLOYEE", employee.toString())
    employee?.empEmail?.let { Log.d("EMAIL ", it) }

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
            "Rates",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Add/Update rates"
        ),
        SettingsMenuItem(
            "account",
            "Account",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Username & Password"
        ),
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
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = employee?.empEmail


        ),
        SettingsMenuItem(
            "backup",
            "Backup",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Data Backup"
        ),
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
            "apis",
            "Custom APIs",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "API configuration"
        ),
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
        ),
        SettingsMenuItem(
            "clear_data",
            "Clear Data",
            Icons.Default.Settings,
            SettingType.Action,
            subtitle = "Clear data"
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
                    onSheetUrlClick = { showSheetInput = true },
                    onClearDataClick = { showClearDataConfirm = true }
                )
            }
        }
    }

    // ✅ Sheet URL Dialog
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

    // ✅ Auto Sync Dialog
    if (showAutoSyncDialog) {
        AlertDialog(
            onDismissRequest = { showAutoSyncDialog = false },
            title = { Text("Auto Sync Settings") },
            text = {
                AutoSyncSetting(userPref = userPreferences)
            },
            confirmButton = {}
        )
    }

    // ✅ Clear Data Confirmation Dialog
    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Confirm Clear Data", fontFamily = poppins) },
            text = {
                Text(
                    "This will permanently delete all app data from this device. Continue?",
                    fontFamily = poppins
                )
            },
            confirmButton = {
                GradientButton(
                    text = "Yes, Clear Data",
                    onClick = {
                        showClearDataConfirm = false
                        showPasswordDialog = true
                    },
                )
            },
            dismissButton = {
                GradientButton(
                    text = "Cancel",
                    onClick = {
                        showClearDataConfirm = false
                    },
                )
            }
        )
    }
    if (showPasswordDialog) {
        var password by remember { mutableStateOf("") }
        val correctPassword =
            userPreferences.getSavedPassword() // You can define this in UserPreferences

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Password Verification", fontFamily = poppins) },
            text = {
                Column {
                    Text("Enter your password to confirm data wipe:")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", fontFamily = poppins) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {

                GradientButton("Confirm", onClick = {
                    if (password == correctPassword) {
                        showPasswordDialog = false
                        viewModel.clearAllData(context, navController)
                    } else {
                        ToastUtils.showToast(context, "Incorrect password")
                    }
                })
            },
            dismissButton = {
                GradientButton("Cancel", onClick = { showPasswordDialog = false })
            }
        )
    }

}

// ---------------- MENU ROW ----------------
@Composable
fun MenuItemRow(
    item: SettingsMenuItem,
    userPreferences: UserPreferences,
    onAutoSyncClick: () -> Unit,
    onSheetUrlClick: () -> Unit,
    onClearDataClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember {
        mutableIntStateOf(userPreferences.getInt(item.key, item.defaultValue ?: 0))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                when (item.key) {
                    "autosync" -> onAutoSyncClick()
                    "sheet_url" -> onSheetUrlClick()
                    "clear_data" -> onClearDataClick()
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
                fontFamily = poppins,
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
                        Text(
                            selectedValue.toString(),
                            fontWeight = FontWeight.Bold,
                            fontFamily = poppins
                        )
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
                                text = { Text(count.toString(), fontFamily = poppins) },
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
                    Text(
                        item.subtitle ?: "",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontFamily = poppins
                    )
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
            Column(Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)) {
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
