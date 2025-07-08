package com.loyalstring.rfid.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.reader.BarcodeReader
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import com.loyalstring.rfid.repository.BulkRepositoryImpl
import com.loyalstring.rfid.repository.DropdownRepository
import com.loyalstring.rfid.ui.utils.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryScanViewModel @Inject constructor(
    private val readerManager: RFIDReaderManager,
    internal val barcodeReader: BarcodeReader,
    private val repository: DropdownRepository,
    private val bulkItemDao: BulkItemDao,
    private val bulkRepository: BulkRepositoryImpl,
    private val userPreferences: UserPreferences,
    private val apiService: RetrofitInterface
) {

    private val _scannedEpcList = mutableStateListOf<String>()
    val scannedEpcList: List<String> get() = _scannedEpcList

    // 🔹 All BulkItems (DB source)
    val allItemsFlow: Flow<List<BulkItem>> = bulkRepository.getAllBulkItems()
//
//    val categorySummary: StateFlow<List<CategoryRow>> = allItemsFlow
//        .map { items ->
//            items
//                .filter { !it.category.isNullOrBlank() }
//                .groupBy { it.category!! }
//                .map { (category, group) ->
//                    val quantity = group.size
//                    val grossWeight = group.sumOf { it.grossWeight?.toDoubleOrNull() ?: 0.0 }
//                    val matchedItems = group.filter { it.matchedQty }
//                    val matchedQty = matchedItems.size
//                    val matchedWeight = matchedItems.sumOf { it.netWeight?.toDoubleOrNull() ?: 0.0 }
//                    val isMatched = matchedQty == quantity && quantity > 0
//
//                    CategoryRow(
//                        category = category,
//                        qty = quantity,
//                        grossWeight = grossWeight,
//                        matchedQty = matchedQty,
//                        matchedWeight = matchedWeight,
//                        isMatched = isMatched
//                    )
//                }
//        }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
//
//    // 🔹 Selected category item list
//    private val _selectedCategoryItems = mutableStateOf<List<BulkItem>>(emptyList())
//    val selectedCategoryItems: Thread.State<List<BulkItem>> = _selectedCategoryItems
//
//    fun selectCategory(category: String) {
//        viewModelScope.launch {
//            val allItems = dao.getAllItemsFlow().first()
//            _selectedCategoryItems.value = allItems.filter { it.category == category }
//        }
//    }

}