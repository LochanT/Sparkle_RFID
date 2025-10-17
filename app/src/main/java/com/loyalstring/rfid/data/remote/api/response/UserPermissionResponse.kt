package com.loyalstring.rfid.data.remote.api.response

data class UserPermissionResponse(
    val IsSuccess: Boolean,
    val Branches: List<Branch>?
)

data class Branch(
    val BranchId: Int,
    val BranchName: String
)
