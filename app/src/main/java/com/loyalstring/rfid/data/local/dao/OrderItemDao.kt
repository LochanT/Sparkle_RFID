package com.loyalstring.rfid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.order.OrderItemModel
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {
    @Query("DELETE FROM orderItem")
    suspend fun clearAllItems()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Query("SELECT * FROM orderItem")
    fun getAllOrderItem(): Flow<List<OrderItem>>
}