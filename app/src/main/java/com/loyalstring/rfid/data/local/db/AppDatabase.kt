package com.loyalstring.rfid.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.loyalstring.rfid.data.local.converters.UHFTAGInfoConverter
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.dao.DropdownDao
import com.loyalstring.rfid.data.local.dao.UHFTAGDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.local.entity.Category
import com.loyalstring.rfid.data.local.entity.Design
import com.loyalstring.rfid.data.local.entity.Product
import com.loyalstring.rfid.data.local.entity.UHFTAGEntity

@TypeConverters(UHFTAGInfoConverter::class)
@Database(
    entities = [UHFTAGEntity::class, Category::class, Product::class, Design::class, BulkItem::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): UHFTAGDao
    abstract fun dropdownDao(): DropdownDao
    abstract fun bulkItemDao(): BulkItemDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                // Step 1: Create new table without the unique constraint
//                db.execSQL("""
//            CREATE TABLE IF NOT EXISTS bulk_items_new (
//                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                rfid TEXT NOT NULL,
//                category TEXT,
//                product TEXT,
//                design TEXT,
//                itemCode TEXT
//            )
//        """)
//
//                // Step 2: Copy data from old table
//                db.execSQL("""
//            INSERT INTO bulk_items_new (id, rfid, category, product, design, itemCode)
//            SELECT id, rfid, category, product, design, itemCode FROM bulk_items
//        """)
//
//                // Step 3: Remove old table
//                db.execSQL("DROP TABLE bulk_items")
//
//                // Step 4: Rename new table to original name
//                db.execSQL("ALTER TABLE bulk_items_new RENAME TO bulk_items")
//            }
//        }
//
//        private val MIGRATION_2_3 = object : Migration(2, 3) {
//            override fun migrate(db: SupportSQLiteDatabase) {
//                // Create new table with the unique index on `epc`
//                db.execSQL("""
//            CREATE TABLE IF NOT EXISTS `bulk_items_new` (
//                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                `productName` TEXT NOT NULL,
//                `itemCode` TEXT NOT NULL,
//                `rfid` TEXT NOT NULL,
//                `grossWeight` TEXT NOT NULL,
//                `stoneWeight` TEXT NOT NULL,
//                `dustWeight` TEXT NOT NULL,
//                `netWeight` TEXT NOT NULL,
//                `category` TEXT NOT NULL,
//                `design` TEXT NOT NULL,
//                `purity` TEXT NOT NULL,
//                `makingPerGram` TEXT NOT NULL,
//                `makingPercent` TEXT NOT NULL,
//                `fixMaking` TEXT NOT NULL,
//                `fixWastage` TEXT NOT NULL,
//                `stoneAmount` TEXT NOT NULL,
//                `dustAmount` TEXT NOT NULL,
//                `sku` TEXT NOT NULL,
//                `epc` TEXT NOT NULL,
//                `vendor` TEXT NOT NULL,
//                `tid` TEXT NOT NULL,
//                `uhfTagInfo` BLOB,
//                UNIQUE(`epc`)
//            )
//        """.trimIndent())
//
//                // Copy data from old to new
//                db.execSQL("""
//            INSERT INTO `bulk_items_new` (
//                id, productName, itemCode, rfid, grossWeight, stoneWeight, dustWeight, netWeight,
//                category, design, purity, makingPerGram, makingPercent, fixMaking, fixWastage,
//                stoneAmount, dustAmount, sku, epc, vendor, tid, uhfTagInfo
//            )
//            SELECT id, productName, itemCode, rfid, grossWeight, stoneWeight, dustWeight, netWeight,
//                category, design, purity, makingPerGram, makingPercent, fixMaking, fixWastage,
//                stoneAmount, dustAmount, sku, epc, vendor, tid, uhfTagInfo
//            FROM `bulk_items`
//        """.trimIndent())
//
//                // Drop old table
//                db.execSQL("DROP TABLE `bulk_items`")
//
//                // Rename new to original
//                db.execSQL("ALTER TABLE `bulk_items_new` RENAME TO `bulk_items`")
//            }
//        }


        fun getDatabase(context: Context): AppDatabase =

            INSTANCE ?: synchronized(this) {
                context.deleteDatabase("app_db")
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}