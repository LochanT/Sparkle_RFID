package com.loyalstring.rfid.repository

import com.loyalstring.rfid.data.local.entity.BulkItem
import kotlinx.coroutines.flow.Flow

interface BulkRepository {
    suspend fun insertBulkItems(items: List<BulkItem>)
    fun getAllBulkItems(): Flow<List<BulkItem>>
}