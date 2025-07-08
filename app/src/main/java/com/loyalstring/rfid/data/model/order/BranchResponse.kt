package com.loyalstring.rfid.data.model.order

data class BranchResponse(
    val BranchCode: String,
    val ClientCode: String,
    val CompanyId: Int,
    val BranchName: String,
    val BranchType: String,
    val BranchHead: String,
    val BranchAddress: String,
    val PhoneNumber: String,
    val MobileNumber: String,
    val FaxNumber: String,
    val Country: String,
    val Town: String,
    val State: String,
    val City: String,
    val Street: String?,         // nullable
    val Area: String?,           // nullable
    val PostalCode: String,
    val GSTIN: String,
    val BranchEmailId: String,
    val BranchStatus: String,
    val FinancialYear: String,
    val BranchLoginStatus: String?, // nullable
    val Id: Int,
    val CreatedOn: String,          // consider using `LocalDateTime` with a proper adapter
    val LastUpdated: String,        // same as above
    val StatusType: Boolean
)

