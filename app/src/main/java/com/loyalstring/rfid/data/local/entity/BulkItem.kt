package com.loyalstring.rfid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rscja.deviceapi.entity.UHFTAGInfo

@Entity(
    tableName = "bulk_items",
    indices = [Index(value = ["rfidCode"], unique = true)]
)
data class BulkItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val product: String,
    val design: String,
    val itemCode: String,
    val rfidCode: String,
    val uhftagInfo: UHFTAGInfo
)