package com.loyalstring.rfid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loyalstring.rfid.data.local.entity.BulkItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BulkItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBulkItem(item: List<BulkItem>)

    @Query("SELECT * FROM bulk_items")
    fun getAllItems(): Flow<List<BulkItem>>
}