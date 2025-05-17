package com.loyalstring.rfid.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.loyalstring.rfid.data.local.dao.DropdownDao
import com.loyalstring.rfid.data.local.dao.UHFTAGDao
import com.loyalstring.rfid.data.local.entity.Category
import com.loyalstring.rfid.data.local.entity.Design
import com.loyalstring.rfid.data.local.entity.Product
import com.loyalstring.rfid.data.local.entity.UHFTAGEntity


@Database(
    entities = [UHFTAGEntity::class, Category::class, Product::class, Design::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): UHFTAGDao
    abstract fun dropdownDao(): DropdownDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                ).build().also { INSTANCE = it }
            }
    }
}