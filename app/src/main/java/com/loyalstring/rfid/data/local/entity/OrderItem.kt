package com.loyalstring.rfid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orderItem")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branchId: String,
    val branchName: String,
    val exhibition: String,
    val remark: String,
    val purity: String,
    val size: String,
    val length: String,
    val typeOfColor: String,
    val screwType: String,
    val polishType: String,
    val finePer: String,
    val wastage: String,
    val orderDate: String,
    val deliverDate: String

)
