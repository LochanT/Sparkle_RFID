package com.loyalstring.rfid.data.model.order

data class CustomOrderItem(
    val CustomOrderId: Int,
   //val OrderDate: String,
    //val DeliverDate: String,
    val SKUId: Int,
    val SKU: String,
    val CategoryId: String?,
    val VendorId: Int?,
    val CategoryName: String,
    val CustomerName: String?,
    val VendorName: String?,
    val ProductId: Int,
    val ProductName: String,
    val DesignId: Int,
    val DesignName: String,
    val PurityId: Int,
    val PurityName: String,
    val GrossWt: String,
    val StoneWt: String,
    val DiamondWt: String,
    val NetWt: String,
    val Size: String,
    val Length: String,
    val TypesOdColors: String,
    val Quantity: String,
    val RatePerGram: String,
    val MakingPerGram: String,
    val MakingFixed: String,
    val FixedWt: String,
    val MakingPercentage: String,
    val DiamondPieces: String,
    val DiamondRate: String,
    val DiamondAmount: String,
    val StoneAmount: String,
    val ScrewType: String,
    val Polish: String,
    val Rhodium: String,
    val SampleWt: String,
    val Image: String,
    val ItemCode: String,
    val CustomerId: Int,
    val MRP: String,
    val HSNCode: String,
    val UnlProductId: Int,
    val OrderBy: String,
    val StoneLessPercent: String,
    val ProductCode: String,
    val TotalWt: String,
    val BillType: String,
    val FinePercentage: String,
    val ClientCode: String?,
    val OrderId: String?,
    //val CreatedOn: String,
    //val LastUpdated: String,
    val StatusType: Boolean,
    val PackingWeight: String,
    val MetalAmount: String,
    val OldGoldPurchase: Boolean,
    val Amount: String,
    val totalGstAmount: String,
    val finalPrice: String,
    val MakingFixedWastage: String,
    val Description: String,
    val CompanyId: Int,
    val LabelledStockId: Int?,
    val TotalStoneWeight: String,
    val BranchId: Int,
    val BranchName: String,
    val Exhibition: String,
    val CounterId: String,
    val EmployeeId: Int,
    val OrderNo: String?,
    val OrderStatus: String?,
    val DueDate: String?,
    val Remark: String?,
    val Id: Int,
    val PurchaseInvoiceNo: String?,
    val Purity: String,
    val Status: String?,
    val URDNo: String?,
    val Stones: List<Stone>,
    val Diamond: List<Diamond>
)

