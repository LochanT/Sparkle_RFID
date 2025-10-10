package com.loyalstring.rfid.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.loyalstring.rfid.data.local.db.AppDatabase
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    var sheetUrl by mutableStateOf(userPreferences.getSheetUrl().orEmpty())
        private set

    fun updateSheetUrl(newUrl: String) {
        sheetUrl = newUrl
        userPreferences.saveSheetUrl(newUrl)
        Log.e("SHEET ID", userPreferences.getSheetUrl().toString())
    }

    fun clearAllData(context: Context, navController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Clear Room Database
                AppDatabase.getDatabase(context).clearAllTables()

                // 2. Clear SharedPreferences
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit { clear() }

                // 3. Clear cache
                context.cacheDir.deleteRecursively()

                withContext(Dispatchers.Main) {
                    ToastUtils.showToast(context, "All data cleared. Please login again.")

                    // 4. Navigate to Login Screen
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // ✅ clears entire backstack
                        launchSingleTop = true          // prevent multiple copies
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ToastUtils.showToast(context, "Error clearing data: ${e.message}")
                }
            }
        }
    }


}
