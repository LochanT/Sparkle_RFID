package com.loyalstring.rfid.viewmodel

import ScannedDataToService
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.ScannedItem
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.reader.BarcodeReader
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import com.loyalstring.rfid.repository.BulkRepositoryImpl
import com.loyalstring.rfid.repository.DropdownRepository
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.ui.utils.toBulkItem
import com.rscja.deviceapi.entity.UHFTAGInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BulkViewModel @Inject constructor(
    private val readerManager: RFIDReaderManager,
    internal val barcodeReader: BarcodeReader,
    private val repository: DropdownRepository,
    private val bulkItemDao: BulkItemDao,
    private val bulkRepository: BulkRepositoryImpl,
    private val userPreferences: UserPreferences,
    private val apiService: RetrofitInterface
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

    private val _syncCompleted = MutableStateFlow(false)
    var syncCompleted: StateFlow<Boolean> = _syncCompleted

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    private val _exportStatus = MutableStateFlow("")
    val exportStatus: StateFlow<String> = _exportStatus

    private val _reloadTrigger = MutableStateFlow(false)
    val reloadTrigger = _reloadTrigger.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage

    private val existingTags = mutableListOf<UHFTAGInfo>()
    private val duplicateTags = mutableListOf<UHFTAGInfo>()

    private val _allScannedTags = mutableStateOf<List<UHFTAGInfo>>(emptyList())
    val allScannedTags: State<List<UHFTAGInfo>> = _allScannedTags

    private val _existingItems = mutableStateOf<List<UHFTAGInfo>>(emptyList())
    val existingItems: State<List<UHFTAGInfo>> = _existingItems

    private val _duplicateItems = mutableStateOf<List<UHFTAGInfo>>(emptyList())
    val duplicateItems: State<List<UHFTAGInfo>> = _duplicateItems
    val rfidInput = mutableStateOf("")

    val scannedEpcList = mutableStateListOf<String>()

    private val _matchedItems = mutableStateListOf<BulkItem>()
    val matchedItems: List<BulkItem> get() = _matchedItems

    private val _unmatchedItems = mutableStateListOf<BulkItem>()
    val unmatchedItems: List<BulkItem> get() = _unmatchedItems

    private val _scannedFilteredItems = mutableStateOf<List<BulkItem>>(emptyList())
    val scannedFilteredItems: State<List<BulkItem>> = _scannedFilteredItems

    private var _filteredSource: List<BulkItem> = emptyList()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _counters = MutableStateFlow<List<String>>(emptyList())
    val counters: StateFlow<List<String>> = _counters

    private val _branches = MutableStateFlow<List<String>>(emptyList())
    val branches: StateFlow<List<String>> = _branches

    private val _boxes = MutableStateFlow<List<String>>(emptyList())
    val boxes: StateFlow<List<String>> = _boxes

    private val _exhibitions = MutableStateFlow<List<String>>(emptyList())
    val exhibitions: StateFlow<List<String>> = _exhibitions


    fun preloadFilters(allItems: List<BulkItem>) {
        _counters.value = allItems.mapNotNull { it.counterName?.takeIf { it.isNotBlank() } }.distinct()
        _branches.value = allItems.mapNotNull { it.branchName?.takeIf { it.isNotBlank() } }.distinct()
        _boxes.value = allItems.mapNotNull { it.boxName?.takeIf { it.isNotBlank() } }.distinct()
        _exhibitions.value = allItems
            .filter { it.branchType?.equals("Exhibition", ignoreCase = true) == true }
            .mapNotNull { it.branchName }
            .distinct()
    }


    fun setSyncCompleted() {
        _syncStatusText.value = "completed"
    }

    // ← the function your UI calls
    fun clearSyncStatus() {
        _syncStatusText.value = ""
    }

    fun setFilteredItems(filtered: List<BulkItem>) {
        _filteredSource = if (filtered.isEmpty()) _allItems else filtered
    }


    private var scanJob: Job? = null

    private val _scanTrigger = MutableStateFlow<String?>(null)
    val scanTrigger: StateFlow<String?> = _scanTrigger

    private val _searchItems = mutableStateListOf<BulkItem>()
    val searchItems: SnapshotStateList<BulkItem> get() = _searchItems

    private var _allItems: List<BulkItem> = emptyList()
    val allItems: List<BulkItem> get() = _allItems

    private val _filteredItems = mutableStateListOf<BulkItem>()
    val filteredItems: List<BulkItem> get() = _filteredItems

    // private val _allItems = mutableStateListOf<BulkItem>()
    // val allItems: List<BulkItem> get() = _allItems


    init {
        viewModelScope.launch {
            bulkRepository.getAllBulkItems().collect { items ->
                _allItems = items
                preloadFilters(_allItems)
                _scannedFilteredItems.value = items
            }
        }
    }
    fun toggleScanningInventory(selectedPower: Int) {
        if (_isScanning.value) {
            stopScanningAndCompute()
            _isScanning.value = false
            Log.d("RFID", "Scanning stopped by toggle")
        } else {
            _isScanning.value = true
            resetScanResults()  // 🔑 Always reset before scanning
            setFilteredItems(_allItems)
            startScanningInventory(selectedPower)
            Log.d("RFID", "Scanning started by toggle")
        }
    }


    fun toggleScanning(selectedPower: Int) {
        if (_isScanning.value) {
            stopScanning()
            _isScanning.value = false
            Log.d("RFID", "Scanning stopped by toggle")
        } else {
           // resetScanResults()
           // setFilteredItems(_allItems) // or _filteredSource depending on scope
            startScanning(selectedPower)
            _isScanning.value = true
            Log.d("RFID", "Scanning started by toggle")
        }
    }






    fun onScanKeyPressed(type: String) {
        _scanTrigger.value = type
    }

    fun clearScanTrigger() {
        _scanTrigger.value = null
    }

    fun startSearch(items: List<BulkItem>) {
        _searchItems.clear()
        _searchItems.addAll(items.filter { it.scannedStatus == "Unmatched" })
    }


    fun startSingleScan(selectedPower: Int, onTagFound: (UHFTAGInfo) -> Unit) {
        if (!success) return

        readerManager.startInventoryTag(selectedPower)

        // Cancel any ongoing scan job first
        scanJob?.cancel()

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            val timeoutMillis = 2000L // wait max 2 seconds
            val startTime = System.currentTimeMillis()

            while (isActive && (System.currentTimeMillis() - startTime < timeoutMillis)) {
                val tag = readerManager.readTagFromBuffer()
                if (tag != null && !tag.epc.isNullOrBlank()) {
                    Log.d("RFID", "Single scan found: ${tag.epc}")
                    readerManager.stopInventory()
                    withContext(Dispatchers.Main) {
                        onTagFound(tag)
                        readerManager.playSound(1)
                        readerManager.stopSound(1)
                    }
                    return@launch
                } else {
                    delay(100) // brief wait before retry
                }
            }

            // Timeout
            readerManager.stopInventory()
            withContext(Dispatchers.Main) {
                Log.d("Tags :", "No Tags found")
            }
        }
    }

    fun startScanningInventory(selectedPower: Int) {
        if (!success || _isScanning.value) return
        scanJob?.cancel()
        scannedEpcList.clear()
        _isScanning.value = true

        readerManager.startInventoryTag(selectedPower)
        readerManager.playSound(1)

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val tag = readerManager.readTagFromBuffer()
                if (tag != null) {
                    val scannedEpc = tag.epc?.trim()?.uppercase() ?: continue
                    if (scannedEpcList.add(scannedEpc)) {
                        Log.d("SCAN_DEBUG", "New scanned EPC: $scannedEpc")

                        withContext(Dispatchers.Main) {
                            val scannedSet = scannedEpcList.toSet()
                            val updatedList = _scannedFilteredItems.value.map { item ->
                                val dbEpc = item.epc?.trim()?.uppercase()
                                when {
                                    dbEpc != null && scannedSet.contains(dbEpc) -> {
                                        Log.d(
                                            "SCAN_MATCH",
                                            "Matched DB EPC: $dbEpc with Scanned EPC: $scannedEpc"
                                        )
                                        item.copy(scannedStatus = "Matched")
                                    }

                                    else -> item.copy(scannedStatus = "Unmatched")
                                }
                            }
                            _scannedFilteredItems.value = updatedList
                        }
                    }
                }
            }
        }
    }

    fun computeScanResults(filteredItems: List<BulkItem>) {
        val matched = mutableListOf<BulkItem>()
        val unmatched = mutableListOf<BulkItem>()
        val scannedEpcSet = scannedEpcList.map { it.trim().uppercase() }.toSet()

        val updatedFiltered = filteredItems.map { item ->
            val dbEpc = item.epc?.trim()?.uppercase()
            when {
                dbEpc != null && scannedEpcSet.contains(dbEpc) -> {
                    val updatedItem = item.copy(scannedStatus = "Matched")
                    matched.add(updatedItem)
                    updatedItem
                }

                else -> {
                    val updatedItem = item.copy(scannedStatus = "Unmatched")
                    unmatched.add(updatedItem)
                    updatedItem
                }
            }
        }

        _matchedItems.clear()
        _matchedItems.addAll(matched)

        _unmatchedItems.clear()
        _unmatchedItems.addAll(unmatched)

        _scannedFilteredItems.value = updatedFiltered

        Log.d("SCAN_RESULT", "Matched: ${matched.size}, Unmatched: ${unmatched.size}")
    }


    fun startScanning(selectedPower: Int) {
        if (success) {
            readerManager.startInventoryTag(selectedPower)
            readerManager.playSound(1, 0)
            scanJob?.cancel()
            if (scanJob?.isActive == true) return

            scanJob = viewModelScope.launch(Dispatchers.IO) {

                while (isActive) {
                    val tag = readerManager.readTagFromBuffer()
                    if (tag != null) {
                        val epc = tag.epc ?: continue

                        val exists = isTagExistsInDatabase(epc)

                        withContext(Dispatchers.Main) {
                            val alreadyInExisting = existingTags.any { it.epc == epc }
                            val alreadyInScanned = _allScannedTags.value.any { it.epc == epc }
                            val alreadyInDuplicates = duplicateTags.any { it.epc == epc }

                            // Case 1: Already marked existing → ignore
                            if (alreadyInExisting) return@withContext

                            // Case 2: Seen before but not in duplicates → mark as duplicate
                            if (alreadyInScanned) {
                                if (!alreadyInDuplicates) {
                                    duplicateTags.add(tag)
                                    _duplicateItems.value = duplicateTags.toList()
                                }
                            } else {
                                // First time scanned → add to list
                                _allScannedTags.value += tag

                                // Only mark as existing if it exists in DB and is not a duplicate
                                if (exists && !alreadyInDuplicates) {
                                    existingTags.add(tag)
                                    _existingItems.value = existingTags.toList()
                                }
                            }

                            // Always update scannedTags with distinct values
                            _scannedTags.update { currentList ->
                                if (currentList.any { it.epc == epc }) currentList
                                else currentList + tag
                            }

                            Log.d("RFID", "Scanned EPC: $epc")
                        }
                    }
                }
            }
        } else {
            Log.e("RFID", "Reader not connected.")
            return
        }
    }

    fun stopScanningAndCompute() {
        stopScanning()
        computeScanResults(_filteredSource)
    }


    fun resetProductScanResults() {
        viewModelScope.launch(Dispatchers.Default) {
            _scannedTags.value = emptyList()
            _scannedItems.value = emptyList()
            _rfidMap.value = emptyMap()
            _allScannedTags.value = emptyList()
            _existingItems.value = emptyList()
            _duplicateItems.value = emptyList()
            _matchedItems.clear()
            _unmatchedItems.clear()
            scannedEpcList.clear()
            _scannedFilteredItems.value = _filteredSource.map { it.copy(scannedStatus = null) }
        }
    }


    fun resetScanResults() {
        viewModelScope.launch(Dispatchers.Default)  {
            _matchedItems.clear()
            _unmatchedItems.clear()
            scannedEpcList.clear()
            _scannedFilteredItems.value = _filteredSource.map { it.copy(scannedStatus = null) }
        }
    }

    //    fun scanSingleTagBlocking(onResult: (String?) -> Unit) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val tag = readerManager.inventorySingleTag(se)
//            val epc = tag?.epc ?: ""
//
//            Log.d("RFID", "Blocking scan result: $epc")
//
//            withContext(Dispatchers.Main) {
//                onResult(epc.ifBlank { null })
//            }
//        }
//    }
    fun startBarcodeScanning(context: Context) {
        if (!barcodeDecoder.isOpen) {
            barcodeDecoder.open(context)
        }
        barcodeDecoder.startScan()

    }
    private suspend fun isTagExistsInDatabase(epc: String): Boolean {
        return bulkItemDao.getItemByEpc(epc) != null
    }

    fun getLocalCounters(): List<String> =
        allItems.mapNotNull { it.counterName?.takeIf { it.isNotBlank() } }.distinct()

    fun getLocalBranches(): List<String> =
        allItems.mapNotNull { it.branchName?.takeIf { it.isNotBlank() } }.distinct()

    fun getLocalBoxes(): List<String> =
        allItems.mapNotNull { it.boxName?.takeIf { it.isNotBlank() } }.distinct()

    fun getLocalExhibitions(): List<String> =
        allItems
            .filter { it.branchType?.equals("Exhibition", ignoreCase = true) == true }
            .mapNotNull { it.branchName } // return the branch names
            .distinct()

    fun setFilteredItemsByType(type: String, value: String) {
        val filtered = when (type) {
            "scan display" -> allItems
            "counter" -> allItems.filter { it.counterName == value }
            "branch" -> allItems.filter { it.branchName == value }
            "box" -> allItems.filter { it.boxName == value }
            "exhibition" -> allItems.filter {
                it.branchName == value && it.branchType.equals(
                    "Exhibition",
                    true
                )
            }

            else -> allItems
        }
        _filteredItems.clear()
        _filteredItems.addAll(filtered)
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
        rfidInput.value = barcode
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
        //  onScanStopped()
        readerManager.stopSound(1)
        readerManager.stopInventory()
        _isScanning.value = false
        scanJob?.cancel()
        scanJob = null

    }


    fun onScanStopped() {
        scanJob?.cancel()
        scanJob = null
        readerManager.stopInventory()
        readerManager.stopSound(1)
        scannedEpcList.clear()
        _allScannedTags.value.forEach { tag ->
            tag.epc?.let { epc ->
                if (!scannedEpcList.contains(epc)) {
                    scannedEpcList.add(epc)
                }
            }
        }
    }











    fun stopBarcodeScanner() {
        barcodeDecoder.close()
        readerManager.stopSound(2)
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
        scannedTags: List<UHFTAGInfo>,
        index: Int
    ) {
        viewModelScope.launch {
            val itemList = scannedTags.mapNotNull { tag ->
                val epc = tag.epc ?: return@mapNotNull null
                val tid = tag.tid ?: ""
                // val rfid = epc // or your display RFID if different

                BulkItem(
                    category = category,
                    productName = product,
                    design = design,
                    itemCode = itemCode,
                    rfid = rfidMap.value.get(index),
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
                    epc = epc,
                    vendor = "",
                    tid = tid,
                    box = "",
                    designCode = "",
                    productCode = "",
                    imageUrl = "",
                    totalQty = 0,
                    pcs = 0,
                    matchedPcs = 0,
                    totalGwt = 0.0,
                    matchGwt = 0.0,
                    totalStoneWt = 0.0,
                    matchStoneWt = 0.0,
                    totalNetWt = 0.0,
                    matchNetWt = 0.0,
                    unmatchedQty = 0,
                    unmatchedGrossWt = 0.0,
                    mrp = 0.0,
                    counterName = "",
                    matchedQty = 0,
                    counterId = 0,
                    scannedStatus = "",
                    boxId = 0,
                    boxName = "",
                    branchId = 0,
                    branchName = "",
                    categoryId = 0,
                    productId = 0,
                    designId = 0,
                    packetId = 0,
                    packetName = "",
                    branchType = "",
                ).apply {
                    uhfTagInfo = tag
                }
            }
            if (itemList.isNotEmpty()) {
                bulkRepository.clearAllItems()
                bulkRepository.insertBulkItems(itemList)
                println("SAVED: Saved ${itemList.size} items to DB successfully.")
                _toastMessage.emit("Saved ${itemList.size} items successfully!")
            } else {
                _toastMessage.emit("No items to save.")
            }
        }
    }

    suspend fun parseGoogleSheetHeaders(url: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val headersLine = reader.readLine()
            reader.close()
            println()
            headersLine.split(",").map {
                it.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    private fun exportToExcel(context: Context, items: List<BulkItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isExporting.value = true
                _exportStatus.value = "Preparing export..."
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("all_sync_items")

                // Create header row
                val columns = listOf<(BulkItem) -> String>(
                    { it.category!! },
                    { it.productName!! },
                    { it.design!! },
                    { it.itemCode!! },
                    { it.rfid!! },
                    { it.grossWeight!! },
                    { it.stoneWeight!! },
                    { it.dustWeight!! },
                    { it.netWeight!! },
                    { it.purity!! },
                    { it.makingPerGram!! },
                    { it.makingPercent!! },
                    { it.fixMaking!! },
                    { it.fixWastage!! },
                    { it.stoneAmount!! },
                    { it.dustAmount!! },
                    { it.sku!! },
                    { it.epc!! },
                    { it.vendor!! },
                    { it.tid!! },
                    { it.productCode!! },
                    { it.box!! },
                    { it.designCode!! },
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
                    "TID",
                    "Box",
                    "Product Code",
                    "Design Code"
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

// Optional: Delete existing file if you want to ensure it's removed before writing
                if (file.exists()) {
                    file.delete()
                }



                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }

                workbook.close()

                // Media scan
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    null
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported to ${file.absolutePath}", Toast.LENGTH_SHORT)
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

    fun getAllItems() {
        viewModelScope.launch {
            bulkRepository.getAllBulkItems().collect { items ->
                _scannedFilteredItems.value = items // ✅ initialize display list
            }
        }
    }
    suspend fun uploadImage(clientCode: String, itemCode: String, imageUri: File) {

        val clientCodePart = clientCode.toRequestBody("text/plain".toMediaTypeOrNull())
        val itemCodePart = itemCode.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = imageUri.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData(
            name = "File",
            filename = imageUri.name,
            body = requestFile
        )

        apiService.uploadLabelStockImage(clientCodePart, itemCodePart, listOf(multipartBody))
    }

    fun getAllItems(context: Context) {
        viewModelScope.launch {
            bulkRepository.getAllBulkItems().collect { items ->
                _allItems = items
                _scannedFilteredItems.value = items
                exportToExcel(context, items)
                preloadFilters(_allItems)
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
                val bulkItems = response
                    .filter { it.status == "ApiActive" || it.status == "Active" && !it.rfidCode.isNullOrBlank()  }
                    .map { it.toBulkItem() }

                val total = bulkItems.size
                bulkRepository.clearAllItems()

                _scannedFilteredItems.value = bulkItems


                bulkItems.forEachIndexed { index, item ->
                    val result = bulkRepository.insertSingleItem(item)
                    Log.d("Insert", "Inserted item with EPC ${item.epc}, result = $result")
                    _syncProgress.value = (index + 1f) / total
                    _syncStatusText.value = "Syncing... ${index + 1} of $total"
                    delay(100)
                }

                Log.d("ToastEmit", "Emitting toast")
                _toastMessage.emit("Synced $total items successfully!")
                _syncStatusText.value = "Sync completed successfully!"



            } catch (e: Exception) {
                _syncStatusText.value = "Sync failed: ${e.localizedMessage}"
                Log.d("ToastEmit", "Emitting toast")
                _toastMessage.emit("Sync failed: ${e.localizedMessage}")
                Log.e("Sync", "Error: ${e.localizedMessage}")

            }finally {
                _isLoading.value = false
            }
        }
    }


    fun setRfidForAllTags(scanned: String) {
        val updatedMap = mutableMapOf<Int, String>()
        scannedTags.value.forEachIndexed { index, _ ->
            updatedMap[index] = scanned
        }
        _rfidMap.value = updatedMap
    }


    fun sendScannedData(tags: List<UHFTAGInfo>, androidId: String, context: Context) {
        Log.d("send scanned items", "CALLED")
        val currentDateTime = LocalDateTime.now()
        val formatted = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        val clientCode = employee?.clientCode


        val data = _rfidMap.value.mapNotNull { (index, rfid) ->
            rfid.let {
                ScannedDataToService(
                    tIDValue = tags.get(index).tid,
                    rFIDCode = it,
                    createdOn = formatted,
                    lastUpdated = formatted,
                    id = 0,
                    clientCode = clientCode,
                    statusType = true,
                    deviceId = androidId

                )


            }
        }

        Log.d("DATA", data.toString())
        if (data.isNotEmpty()) {


            viewModelScope.launch {
                val response = apiService.addAllScannedData(data)
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                    ToastUtils.showToast(context, "Items scanned successfully")
                    _reloadTrigger.value = !_reloadTrigger.value // triggers recomposition
                    Log.d("API_SUCCESS", "Received response: ${response.body()}")

                } else {
                    Log.e("API_ERROR", "Error: ${response.code()}")
                    ToastUtils.showToast(context, "Failed to scan")
                }
            }


        }


    }



}
