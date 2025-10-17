package com.loyalstring.rfid.data.model.setting

data class UserPermissionResponse(
    val UserId: Int,
    val FirstName: String?,
    val LastName: String?,
    val BranchSelectionJson: String?, // JSON string containing branch list
    val CompanySelectionJson: String?,
    val CounterJson: String?,
    val Modules: List<Module>?
)

data class Module(
    val Id: Int,
    val PageId: Int,
    val PageName: String?,
    val PageDisplayName: String?
)
