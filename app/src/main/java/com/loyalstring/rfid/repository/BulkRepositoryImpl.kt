package com.loyalstring.rfid.repository

import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BulkRepositoryImpl @Inject constructor(
    private val bulkItemDao: BulkItemDao
) : BulkRepository {

    override suspend fun insertBulkItems(items: List<BulkItem>) {
        bulkItemDao.insertBulkItem(items)
    }

    override fun getAllBulkItems(): Flow<List<BulkItem>> {
        return bulkItemDao.getAllItems()
    }
}


