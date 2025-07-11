package com.loyalstring.rfid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.order.OrderItemModel
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {
    @Query("DELETE FROM orderItem")
    suspend fun clearAllItems()

    @Query("SELECT * FROM OrderItem WHERE RFIDCode = :rfidCode LIMIT 1")
    suspend fun getItemByRfid(rfidCode: String): OrderItem?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Update
    suspend fun update(orderItem: OrderItem)

    // Helper method for upsert logic
    suspend fun insertOrUpdate(orderItem: OrderItem) {
        val existing = getItemByRfid(orderItem.rfidCode)
        if (existing == null) {
            insertOrderItem(orderItem)
        } else {
            update(orderItem.copy(id = existing.id)) // preserve the existing ID
        }
    }

    @Query("SELECT * FROM orderItem")
    fun getAllOrderItem(): Flow<List<OrderItem>>
}