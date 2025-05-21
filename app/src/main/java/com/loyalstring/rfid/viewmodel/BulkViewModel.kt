package com.loyalstring.rfid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ScannedItem
import com.loyalstring.rfid.data.reader.BarcodeReader
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.loyalstring.rfid.repository.BulkRepositoryImpl
import com.loyalstring.rfid.repository.DropdownRepository
import com.rscja.deviceapi.entity.UHFTAGInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BulkViewModel @Inject constructor(
    private val readerManager: RFIDReaderManager,
    internal val barcodeReader: BarcodeReader,
    private val repository: DropdownRepository,
    private val bulkItemDao: BulkItemDao,
    private val bulkRepository: BulkRepositoryImpl
) : ViewModel() {

    private val success = readerManager.initReader()
    private val barcodeDecoder = barcodeReader.barcodeDecoder

    private val _scannedTags = MutableStateFlow<List<UHFTAGInfo>>(emptyList())
    val scannedTags: StateFlow<List<UHFTAGInfo>> = _scannedTags

    private val _scannedItems = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems

    private val _rfidMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val rfidMap: StateFlow<Map<Int, String>> = _rfidMap


    val categories =
        repository.categories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val products = repository.products.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val designs = repository.designs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var scanJob: Job? = null

    fun startScanning() {
        if (success) {
            readerManager.startInventoryTag()
            if (scanJob?.isActive == true) return

            scanJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val tag = readerManager.readTagFromBuffer()
                    readerManager.playSound(1)
                    if (tag != null) {
                        val gson = Gson()
                        val json = gson.toJson(tag)
                        println(json)
                        Log.e("RFID", "Tag read: $json")
                        _scannedTags.update { currentList ->
                            if (currentList.any { it.epc == tag.epc }) currentList
                            else currentList + tag
                        }
                    } else {
                        Log.e("RFID", "No tag in buffer")
                    }
                }
            }
        }
    }

    fun startBarcodeScanning() {
        barcodeDecoder.startScan()

    }

    fun assignRfidCode(index: Int, rfid: String) {
        val currentMap = _rfidMap.value

        // Skip if already assigned elsewhere
        if (currentMap.containsValue(rfid)) return

        _rfidMap.value = currentMap.toMutableMap().apply {
            put(index, rfid)
        }
    }


    fun onBarcodeScanned(barcode: String) {
        if (_scannedItems.value.any { it.barcode == barcode }) return

        val nextIndex = _scannedItems.value.size + 1
        val itemCode = generateItemCode(nextIndex)
        val srNo = generateSerialNumber(nextIndex)

        val newItem = ScannedItem(id = srNo, itemCode = itemCode, barcode = barcode)
        _scannedItems.update { it + newItem }
        println("Scanned barcode: $barcode")
    }

    private fun generateItemCode(index: Int): String {
        return "ITEM" + index.toString().padStart(4, '0')
    }

    private fun generateSerialNumber(index: Int): String {
        return index.toString()
    }

    fun stopScanning() {
        scanJob?.cancel()
        readerManager.stopInventory()
        readerManager.stopSound(1)
    }

    fun stopBarcodeScanner() {
        barcodeDecoder.close()
        readerManager.stopSound(2)
    }

    fun resetData() {
        _scannedTags.value = emptyList()
        _scannedItems.value = emptyList()
        _rfidMap.value = emptyMap()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }

    fun saveDropdownCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.addCategory(name)
        }
    }

    fun saveDropdownProduct(name: String, type: String) {
        viewModelScope.launch {
            repository.addProduct(name)
        }
    }

    fun saveDropdownDesign(name: String, type: String) {
        viewModelScope.launch {
            repository.addDesign(name)
        }
    }

    fun saveBulkItems(
        category: String,
        itemCode: String,
        product: String,
        design: String,
        uhftagInfo: UHFTAGInfo
    ) {
        viewModelScope.launch {
            val itemList = _rfidMap.value.mapNotNull { (index, rfid) ->
                rfid.let {
                    BulkItem(
                        category = category,
                        product = product,
                        design = design,
                        itemCode = itemCode,
                        rfidCode = it,
                        uhftagInfo = uhftagInfo
                    )
                }
            }
            if (itemList.isNotEmpty()) {
                bulkRepository.insertBulkItems(itemList)
                // ✅ Print confirmation
                println("SAVED: Saved ${itemList.size} items to DB successfully.")
                //  ToastUtils.showToast(c,"Saved ${itemList.size} items successfully")
            } else {
                println("SAVED: No items to save.")
            }
        }
    }


}
