package com.loyalstring.rfid.data.remote.api.requests

data class UserPermissionRequest(
    val ClientCode: String,
    val UserId: Int
)