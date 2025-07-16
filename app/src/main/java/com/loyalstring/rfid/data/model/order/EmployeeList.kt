package com.example.sparklepos.models.loginclasses.customerBill

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer")
data class EmployeeList(
    @PrimaryKey(autoGenerate = true) val empId: Int = 0,
    val Id: Int,
    val FirstName: String,
    val LastName: String,
    val PerAddStreet: String,
    val CurrAddStreet: String,
    val Mobile: String,
    val Email: String,
    val Password: String,
    val CustomerLoginId: String,
    val DateOfBirth: String,
    val MiddleName: String,
    val PerAddPincode: String,
    val Gender: String?,               // nullable
    val OnlineStatus: String?,        // nullable
    val CurrAddTown: String?,         // nullable
    val CurrAddPincode: String,
    val CurrAddState: String,
    val PerAddTown: String,
    val PerAddState: String?,         // nullable
    val GstNo: String,
    val PanNo: String,
    val AadharNo: String,
    val BalanceAmount: String,
    val AdvanceAmount: String,
    val Discount: String,
    val CreditPeriod: String?,        // nullable
    val FineGold: String,
    val FineSilver: String,
    val ClientCode: String,
    val VendorId: Int,
    val AddToVendor: Boolean,
    val CustomerSlabId: Int,
    val CreditPeriodId: Int,
    val RateOfInterestId: Int,
    val CustomerSlab: String?,        // nullable
    val RateOfInterest: String?,      // nullable
    val CreatedOn: String,
    val LastUpdated: String,
    val StatusType: Boolean,
    val Remark: String,
    val Area: String,
    val City: String,
    val Country: String
)