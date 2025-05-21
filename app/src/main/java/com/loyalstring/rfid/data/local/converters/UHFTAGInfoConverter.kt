package com.loyalstring.rfid.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.rscja.deviceapi.entity.UHFTAGInfo

class UHFTAGInfoConverter {

    @TypeConverter
    fun fromUHFTAGInfo(value: UHFTAGInfo): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toUHFTAGInfo(value: String): UHFTAGInfo {
        return Gson().fromJson(value, UHFTAGInfo::class.java)
    }
}
