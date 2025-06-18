package com.loyalstring.rfid.repository

import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.remote.response.AlllabelResponse
import kotlinx.coroutines.flow.Flow

interface BulkRepository {
    suspend fun insertBulkItems(items: List<BulkItem>)
    fun getAllBulkItems(): Flow<List<BulkItem>>
    suspend fun clearAllItems()
    suspend fun syncBulkItemsFromServer(request: ClientCodeRequest): List<AlllabelResponse.LabelItem>
    suspend fun insertSingleItem(item: BulkItem)
}