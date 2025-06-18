package com.loyalstring.rfid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loyalstring.rfid.data.local.entity.BulkItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BulkItemDao {
    @Query("DELETE FROM bulk_items")
    suspend fun clearAllItems()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBulkItem(items: List<BulkItem>): List<Long>

    @Query("SELECT * FROM bulk_items")
    fun getAllItemsFlow(): Flow<List<BulkItem>>

    @Query("SELECT * FROM bulk_items WHERE epc = :epc LIMIT 1")
    suspend fun getItemByEpc(epc: String): BulkItem?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSingleItem(item: BulkItem): Long

}