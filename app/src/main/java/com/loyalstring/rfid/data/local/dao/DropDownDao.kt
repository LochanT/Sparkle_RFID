package com.loyalstring.rfid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loyalstring.rfid.data.local.entity.Category
import com.loyalstring.rfid.data.local.entity.Design
import com.loyalstring.rfid.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface DropdownDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDesign(design: Design)

    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM product")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM design")
    fun getAllDesigns(): Flow<List<Design>>
}
