package com.loyalstring.rfid.data.remote.response

import com.google.gson.annotations.SerializedName

data class AlllabelResponse(
    @SerializedName("labelList") val labelList: List<LabelItem>?
) {
    data class LabelItem(
        @SerializedName("Id") val id: Int,
        @SerializedName("ProductName") val productName: String,
        @SerializedName("SKUId") val skuId: Int,
        @SerializedName("ItemCode") val itemCode: String?,
        @SerializedName("GrossWt") val grossWt: String?,
        @SerializedName("NetWt") val netWt: String?,
        @SerializedName("TotalStoneWeight") val totalStoneWeight: String?,
        @SerializedName("TotalStoneAmount") val totalStoneAmount: String?,
        @SerializedName("MakingPerGram") val makingPerGram: String?,
        @SerializedName("MakingPercentage") val makingPercentage: String?,
        @SerializedName("MakingFixedAmt") val makingFixedAmt: String?,
        @SerializedName("MakingFixedWastage") val makingFixedWastage: String?,
        @SerializedName("SKU") val sku: String?,
        @SerializedName("TIDNumber") val tidNumber: String?,
        @SerializedName("RFIDCode") val rfidCode: String?,
        @SerializedName("VendorName") val vendorName: String?,
        @SerializedName("CategoryName") val categoryName: String?,
        @SerializedName("DesignName") val designName: String?,
        @SerializedName("PurityName") val purityName: String?,
        // add any other fields you need below…
    )
}




