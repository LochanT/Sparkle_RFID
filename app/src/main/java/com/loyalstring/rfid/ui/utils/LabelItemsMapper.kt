package com.loyalstring.rfid.ui.utils

import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.remote.data.AlllabelResponse


fun AlllabelResponse.LabelItem.toBulkItem(): BulkItem {
    return BulkItem(
        productName = this.productName,
        itemCode = this.itemCode ?: "",
        rfid = this.rfidCode ?: "",

        // Weights
        grossWeight = this.grossWt ?: "",
        stoneWeight = this.totalStoneWeight ?: "",
        dustWeight = "",   // if you want pouch/other
        netWeight = this.netWt ?: "",

        // Category/design/purity
        category = this.categoryName ?: "",
        design = this.designName ?: "",
        purity = this.purityName ?: "",

        // Making charges
        makingPerGram = this.makingPerGram ?: "",
        makingPercent = this.makingPercentage ?: "",
        fixMaking = this.makingFixedAmt ?: "",
        fixWastage = this.makingFixedWastage ?: "",

        // Stone amounts
        stoneAmount = this.totalStoneAmount ?: "",
        dustAmount = "",

        // Identifiers
        sku = this.sku ?: "", // if your JSON has an EPC field
        vendor = this.vendorName ?: "",
        tid = this.tidNumber ?: "",
        id = 0,
        epc = "",
        uhfTagInfo = null,
    )
}
