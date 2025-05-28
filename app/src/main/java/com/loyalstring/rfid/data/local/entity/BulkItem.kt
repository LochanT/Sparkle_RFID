package com.loyalstring.rfid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rscja.deviceapi.entity.UHFTAGInfo

@Entity(
    tableName = "bulk_items",
    indices = [Index(value = ["rfid"], unique = true)]
)
data class BulkItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val itemCode: String,
    val rfid: String,
    val grossWeight: String,
    val stoneWeight: String,
    val dustWeight: String,
    val netWeight: String,
    val category: String,
    val design: String,
    val purity: String,
    val makingPerGram: String,
    val makingPercent: String,
    val fixMaking: String,
    val fixWastage: String,
    val stoneAmount: String,
    val dustAmount: String,
    val sku: String,
    val epc: String,
    val vendor: String,
    val tid: String,
    val uhfTagInfo: UHFTAGInfo? = null
)