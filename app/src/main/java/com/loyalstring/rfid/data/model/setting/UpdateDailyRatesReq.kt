package com.loyalstring.rfid.data.model.setting

import com.google.gson.annotations.SerializedName

data class UpdateDailyRatesReq(
    @SerializedName("CategoryId") val categoryId: Int,
    @SerializedName("CategoryName") val categoryName: String,
    @SerializedName("ClientCode") val clientCode: String,
    @SerializedName("EmployeeCode") val employeeCode: String,
    @SerializedName("FinePercentage") val finePercentage: String,
    @SerializedName("PurityId") val purityId: Int,
    @SerializedName("PurityName") val purityName: String,
    @SerializedName("Rate") val rate: String
)
