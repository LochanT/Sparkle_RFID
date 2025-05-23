package com.loyalstring.rfid.data.model.addSingleItem

data class PurityModel(
    val Id: Int,
    val PurityName: String?,
    val CategoryId: Int,
    val ShortName: String?,
    val Description: String?,
    val FinePercentage: String?,
    val TodaysRate: String?,
    val Status: String?,
    val ClientCode: String?,
    val EmployeeCode: String?,
    val CategoryName: String?,
    val LastUpdated: String?, // Can be changed to `LocalDateTime?` with proper parsing
    val CreatedOn: String?,    // Can be changed to `LocalDateTime?` with proper parsing
    val StatusType: Boolean
)

