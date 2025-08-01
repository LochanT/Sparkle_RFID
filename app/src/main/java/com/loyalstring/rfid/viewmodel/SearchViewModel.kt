package com.loyalstring.rfid.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.local.entity.SearchItem
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.loyalstring.rfid.ui.utils.ToastUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val readerManager: RFIDReaderManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /// private val _unmatchedItems = savedStateHandle.get<List<BulkItem>>("unmatchedItems") ?: emptyList()
    private val _searchItems = mutableStateListOf<SearchItem>()
    val searchItems: List<SearchItem> get() = _searchItems

    init {
        val unmatched = savedStateHandle.get<List<BulkItem>>("unmatchedItems") ?: emptyList()
        Log.d("SearchViewModel", "Received ${unmatched.size} items")
        startSearch(unmatched)
    }

    private var scanJob: Job? = null

    fun startSearch(unmatchedItems: List<BulkItem>) {
        _searchItems.clear()
        _searchItems.addAll(unmatchedItems.map {
            SearchItem(
                epc = it.epc ?: "",
                itemCode = it.itemCode ?: "",
                productName = it.productName ?: "",
                rfid = it.rfid ?: ""
            )
        })

        if (readerManager.initReader()) {
            startTagScanning()

        }
    }


    fun startTagScanning() {
        readerManager.startInventoryTag(30)

        scanJob?.cancel()
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val tag = readerManager.readTagFromBuffer()
                if (tag?.epc != null) {
                    val epc = tag.epc
                    val rssi = tag.rssi

                    val proximity = convertRssiToProximity(rssi)
                    var id = -1

                    if (proximity in 1..49) {
                        id = 4
                    } else if (proximity in 51..59) {
                        id = 2
                    } else if (proximity in 61..69) {
                        id = 5
                    } else if (proximity >= 70) {
                        id = 1
                    }

                    if (id != -1) {
                        withContext(Dispatchers.Main) {
                            readerManager.playSound(id)
                        }
                    }

                    val index = _searchItems.indexOfFirst { it.epc == epc }
                    if (index != -1) {
                        withContext(Dispatchers.Main) {
                            _searchItems[index] = _searchItems[index].copy(
                                rssi = rssi,
                                proximityPercent = proximity
                            )
                        }
                    }
                } else {
                    delay(100)
                }
            }
        }
    }

    fun stopSearch() {
        scanJob?.cancel()
        readerManager.stopInventory()
    }

    private fun convertRssiToProximity(rssi: String): Int {
        return try {
            val rssiValue = rssi.toFloat()  // Change here
            ((rssiValue + 80).coerceAtLeast(0f) * 100f / 40f).toInt().coerceIn(0, 100)
        } catch (e: NumberFormatException) {
            0 // Default proximity if parsing fails
        }
    }
}
