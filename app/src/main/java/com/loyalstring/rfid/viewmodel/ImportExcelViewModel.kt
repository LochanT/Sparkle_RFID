package com.loyalstring.rfid.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.repository.BulkRepositoryImpl
import com.loyalstring.rfid.ui.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import javax.inject.Inject

@HiltViewModel
class ImportExcelViewModel @Inject constructor(
    private val bulkRepository: BulkRepositoryImpl,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _importProgress = MutableStateFlow(ImportProgress())
    val importProgress: StateFlow<ImportProgress> = _importProgress

    private val _isImportDone = MutableStateFlow(false)
    val isImportDone: StateFlow<Boolean> = _isImportDone

    private val _parsedItems = MutableStateFlow<List<BulkItem>>(emptyList())
    val parsedItems: StateFlow<List<BulkItem>> = _parsedItems

    private var selectedUri: Uri? = null
    private var syncedRFIDMap: Map<String, String>? = null


    fun setSelectedFile(uri: Uri) {
        selectedUri = uri
    }

    fun parseExcelHeaders(context: Context, uri: Uri): List<String> {
        val headers = mutableListOf<String>()
        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)

                for (cell in headerRow) {
                    headers.add(getCellValue(cell).trim())
                }

                workbook.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return headers
    }

    fun importMappedData(
        context: Context,
        fieldMapping: Map<String, String>
    ) {
        val uri = selectedUri ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val failed = mutableListOf<String>()
            var imported = 0
            var total = 0
            _parsedItems.value = emptyList()

            try {
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    val headerRow = sheet.getRow(0)
                    val rawHeaderIndexMap = mutableMapOf<String, Int>()
                    for (cell in headerRow) {
                        val name = getCellValue(cell).trim().lowercase()
                        rawHeaderIndexMap[name] = cell.columnIndex
                    }

                    val normalizedFieldMapping = fieldMapping.entries.associate {
                        it.value.trim().lowercase() to it.key.trim().lowercase()
                    }

                    val items = mutableListOf<BulkItem>()
                    val seenEpc = mutableSetOf<String>()

                    total = sheet.lastRowNum

                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i)
                        if (row == null || row.firstCellNum < 0) continue

                        val rfid = getStringFromRow(
                            row,
                            rawHeaderIndexMap,
                            normalizedFieldMapping["rfid"]
                        )

                        var epcVal = getStringFromRow(
                            row,
                            rawHeaderIndexMap,
                            normalizedFieldMapping["epc"]
                        ).trim()

// If EPC is missing → fetch using itemCode
                        if (epcVal.isBlank()) {
                            epcVal = syncAndMapRow(rfid)
                        }

                        if (epcVal.isBlank()) {
                            failed.add("Missing EPC for RFID $rfid at row ${i + 1}")
                            continue // Skip row if EPC still not found
                        }

                        try {
                            val item = BulkItem(
                                productName = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["productname"]
                                ),
                                itemCode = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["itemcode"]
                                ),
                                rfid = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["rfid"]
                                ),
                                netWeight = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["netweight"]
                                ),
                                category = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["category"]
                                ),
                                purity = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["purity"]
                                ),
                                design = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["design"]
                                ),
                                grossWeight = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["grossweight"]
                                ),
                                stoneWeight = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["stoneweight"]
                                ),
                                makingPerGram = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["makingpergram"]
                                ),
                                makingPercent = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["makingpercent"]
                                ),
                                fixMaking = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["fixmaking"]
                                ),
                                fixWastage = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["fixwastage"]
                                ),
                                stoneAmount = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["stoneamount"]
                                ),
                                counterName = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["countername"]
                                ),
                                branchName = getStringFromRow(
                                    row,
                                    rawHeaderIndexMap,
                                    normalizedFieldMapping["branchname"]
                                ),
                                id = 0,
                                dustWeight = "",
                                dustAmount = "",
                                sku = "",
                                epc = epcVal,
                                vendor = "",
                                tid = epcVal,
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
                                matchedQty = 0,
                                unmatchedGrossWt = 0.0,
                                mrp = 0.0,
                                counterId = 0,
                                scannedStatus = "",
                                boxId = 0,
                                boxName = "",
                                branchId = 0,
                                categoryId = 0,
                                productId = 0,
                                designId = 0,
                                packetId = 0,
                                packetName = ""
                            ).apply {
                                uhfTagInfo = null
                            }

                            items.add(item)
                            imported++

                        } catch (e: Exception) {
                            failed.add("Row ${i + 1}")
                        }

                        _importProgress.value = ImportProgress(
                            totalFields = total,
                            importedFields = imported,
                            failedFields = failed.toList()
                        )
                    }


                    bulkRepository.clearAllItems()
                    bulkRepository.insertBulkItems(items)
                    _parsedItems.value = items
                    _isImportDone.value = true
                    delay(200)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isImportDone.value = true
            }
        }
    }

    fun syncAndMapRow(itemCode: String): String {
        return syncedRFIDMap?.get(itemCode) ?: ""
    }

    suspend fun syncRFIDDataIfNeeded(context: Context) {
        if (syncedRFIDMap != null) return // Already synced

        val employee = userPreferences.getEmployee(Employee::class.java)
        val clientCode = employee?.clientCode ?: return

        val response = bulkRepository.syncRFIDItemsFromServer(ClientCodeRequest(clientCode))
        bulkRepository.insertRFIDTags(response)

        // Cache mapping in memory (itemCode -> EPC)
        syncedRFIDMap = response.associateBy(
            { it.BarcodeNumber },
            { it.TidValue }
        )
    }



    private fun getStringFromRow(
        row: Row,
        headerIndexMap: Map<String, Int>,
        columnName: String?
    ): String {
        return try {
            val index = columnName?.let { headerIndexMap[it] }
            if (index != null) getCellValue(row.getCell(index)).trim() else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getCellValue(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString()
                } else {
                    cell.numericCellValue.toString()
                }
            }

            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                when (cell.cachedFormulaResultType) {
                    CellType.STRING -> cell.stringCellValue
                    CellType.NUMERIC -> cell.numericCellValue.toString()
                    CellType.BOOLEAN -> cell.booleanCellValue.toString()
                    else -> ""
                }
            }

            else -> ""
        }
    }
}

data class ImportProgress(
    val totalFields: Int = 0,
    val importedFields: Int = 0,
    val failedFields: List<String> = emptyList()
)
