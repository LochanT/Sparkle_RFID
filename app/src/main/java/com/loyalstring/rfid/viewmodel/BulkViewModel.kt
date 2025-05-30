package com.loyalstring.rfid.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.ScannedItem
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.reader.BarcodeReader
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.loyalstring.rfid.repository.BulkRepositoryImpl
import com.loyalstring.rfid.repository.DropdownRepository
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.toBulkItem
import com.rscja.deviceapi.entity.UHFTAGInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class BulkViewModel @Inject constructor(
    private val readerManager: RFIDReaderManager,
    internal val barcodeReader: BarcodeReader,
    private val repository: DropdownRepository,
    private val bulkItemDao: BulkItemDao,
    private val bulkRepository: BulkRepositoryImpl,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val success = readerManager.initReader()
    private val barcodeDecoder = barcodeReader.barcodeDecoder

    private val _scannedTags = MutableStateFlow<List<UHFTAGInfo>>(emptyList())
    val scannedTags: StateFlow<List<UHFTAGInfo>> = _scannedTags

    private val _scannedItems = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems

    private val _rfidMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val rfidMap: StateFlow<Map<Int, String>> = _rfidMap

    val employee: Employee? = userPreferences.getEmployee(Employee::class.java)

    val categories =
        repository.categories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val products = repository.products.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val designs = repository.designs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _syncProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val syncProgress: StateFlow<Float> = _syncProgress

    private val _syncStatusText = MutableStateFlow("")
    val syncStatusText: StateFlow<String> = _syncStatusText

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    private val _exportStatus = MutableStateFlow("")
    val exportStatus: StateFlow<String> = _exportStatus


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
                        productName = product,
                        design = design,
                        itemCode = itemCode,
                        rfid = it,
                        uhfTagInfo = uhftagInfo,
                        grossWeight = "",
                        stoneWeight = "",
                        dustWeight = "",
                        netWeight = "",
                        purity = "",
                        makingPerGram = "",
                        makingPercent = "",
                        fixMaking = "",
                        fixWastage = "",
                        stoneAmount = "",
                        dustAmount = "",
                        sku = "",
                        epc = "",
                        vendor = "",
                        tid = ""
                    )
                }
            }
            if (itemList.isNotEmpty()) {
                bulkRepository.insertBulkItems(itemList)
                println("SAVED: Saved ${itemList.size} items to DB successfully.")
            } else {
                println("SAVED: No items to save.")
            }
        }
    }

    fun exportToExcel(context: Context, items: List<BulkItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isExporting.value = true
                _exportStatus.value = "Preparing export..."
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("all_sync_items")

                // Create header row
                val columns = listOf<(BulkItem) -> String>(
                    { it.category },
                    { it.productName },
                    { it.design },
                    { it.itemCode },
                    { it.rfid },
                    { it.grossWeight },
                    { it.stoneWeight },
                    { it.dustWeight },
                    { it.netWeight },
                    { it.purity },
                    { it.makingPerGram },
                    { it.makingPercent },
                    { it.fixMaking },
                    { it.fixWastage },
                    { it.stoneAmount },
                    { it.dustAmount },
                    { it.sku },
                    { it.epc },
                    { it.vendor },
                    { it.tid }
                )
                val headers = listOf(
                    "Category",
                    "Product Name",
                    "Design",
                    "Item Code",
                    "RFID",
                    "Gross Weight",
                    "Stone Weight",
                    "Dust Weight",
                    "Net Weight",
                    "Purity",
                    "Making/Gram",
                    "Making %",
                    "Fix Making",
                    "Fix Wastage",
                    "Stone Amount",
                    "Dust Amount",
                    "SKU",
                    "EPC",
                    "Vendor",
                    "TID"
                )
                Log.e("HEADERS :", headers.toString())
                val headerRow = sheet.createRow(0)
                headers.forEachIndexed { colIndex, title ->
                    headerRow.createCell(colIndex).setCellValue(title)
                    sheet.setColumnWidth(colIndex, 4000)
                }

                // Add data rows
                items.forEachIndexed { rowIndex, item ->
                    val row = sheet.createRow(rowIndex + 1)
                    columns.forEachIndexed { colIndex, extractor ->
                        row.createCell(colIndex)
                            .setCellValue(extractor(item))
                    }
                }

                // Create file

                val downloads =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloads.exists()) downloads.mkdirs()
                val file = File(downloads, "all_items.xlsx")
                FileOutputStream(file).use { workbook.write(it) }
                workbook.close()

                // Media scan
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    null
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported to ${file.absolutePath}", Toast.LENGTH_LONG)
                        .show()
                    openExcelFile(context, file)
                }
                _exportStatus.value = "Exported to ${file.absolutePath}"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: ${e.localizedMessage}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    private fun openExcelFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app to open Excel", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getAllItems(context: Context) {
        viewModelScope.launch {
            bulkRepository.getAllBulkItems().collect { items ->
                exportToExcel(context, items)
            }
        }
    }

    fun syncItems() {
        viewModelScope.launch {
            try {
                Log.d("Sync", "syncItems called")
                _isLoading.value = true
                _syncProgress.value = 0f
                _syncStatusText.value = "Starting sync..."

                val clientCode = employee?.clientCode ?: return@launch
                val request = ClientCodeRequest(clientCode)

                _syncStatusText.value = "Fetching data..."
                val response = bulkRepository.syncBulkItemsFromServer(request)
                val bulkItems = response.map { it.toBulkItem() }

                val total = bulkItems.size
                bulkItemDao.clearAllItems()

                bulkItems.forEachIndexed { index, item ->
                    bulkItemDao.insertBulkItem(listOf(item))
                    _syncProgress.value = (index + 1f) / total
                    _syncStatusText.value = "Syncing... ${index + 1} of $total"
                    delay(100)
                }

                _syncStatusText.value = "Sync completed successfully!"
            } catch (e: Exception) {
                _syncStatusText.value = "Sync failed: ${e.localizedMessage}"
                Log.e("Sync", "Error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }



}
