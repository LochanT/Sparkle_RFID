package com.loyalstring.rfid.data.model.addSingleItem

data class SKUModel( val Id: Int,
                     val StockKeepingUnit: String,
                     val EmployeeId: Int,
                     val SketchNo: String,
                     val Description: String,
                     val ProductRemark: String,
                     val CategoryId: Int,
                     val ProductId: Int,
                     val DesignId: Int,
                     val PurityId: Int,
                     val Colour: String,
                     val Size: String,
                     val HSNCode: String,
                     val MetalName: String,
                     val GrossWt: String,
                     val NetWt: String,
                     val CollectionName: String?,
                     val OccassionName: String,
                     val Gender: String,
                     val MRP: String,
                     val Images: String,
                     val MakingFixedAmt: String,
                     val MakingPerGram: String,
                     val MakingFixedWastage: String,
                     val MakingPercentage: String,
                     val TotalStoneWeight: String,
                     val TotalStoneAmount: String,
                     val TotalStonePieces: String,
                     val TotalDiamondWeight: String,
                     val TotalDiamondPieces: String,
                     val TotalDiamondAmount: String,
                     val Featured: String,
                     val Pieces: String,
                     val HallmarkAmount: String,
                     val HUIDCode: String,
                     val BoxId: String,
                     val Quantity: String,
                     val BlackBeads: String,
                     val Height: String,
                     val Width: String,
                     val CuttingGrossWt: String,
                     val CuttingNetWt: String,
                     val MetalRate: String,
                     val PurchaseCost: String,
                     val Margin: String,
                     val BranchName: String,
                     val BoxName: String,
                     val EstimatedDays: String,
                     val OfferPrice: String,
                     val Ranking: String,
                     val CompanyId: Int,
                     val CounterId: Int,
                     val BranchId: Int,
                     val Status: String,
                     val ClientCode: String,
                     val VendorId: Int,
                     val MinQuantity: String,
                     val MinWeight: String,
                     val WeightCategories: String,
                     val TagWeight: String,
                     val FindingWeight: String,
                     val LanyardWeight: String,
                     val OtherWeight: String,
                     val PouchWeight: String,
                     val ProductName: String,
                     val CategoryName: String,
                     val DesignName: String,
                     val PurityName: String,
                     val StoneLessPercent: String?,
                     val ClipWeight: String,
                     val CollectionId: Int,
                     val CollectionNameSKU: String?,
                     val SKUVendor: List<SKUVendor>,
                     val Diamonds: List<Diamond>,
                     val SKUStoneMain: List<SKUStoneMain>)

data class SKUVendor(
    val SKUVendorId: Int,
    val SKUId: Int,
    val VendorId: Int,
    val VendorName: String,
    val ClientCode: String,
    val BranchId: Int,
    val CompanyId: Int,
    val EmployeeId: Int
)

data class Diamond(
    val Id: Int,
    val DiamondName: String,
    val DiamondWeight: String,
    val DiamondRate: String,
    val DiamondPieces: String,
    val DiamondClarity: String,
    val DiamondColour: String,
    val DiamondCut: String,
    val DiamondShape: String,
    val DiamondSize: String,
    val Certificate: String,
    val SettingType: String,
    val DiamondAmount: String,
    val DiamondPurchaseAmt: String?,
    val Description: String,
    val ClientCode: String,
    val SKUId: Int,
    val CompanyId: Int,
    val CounterId: Int,
    val BranchId: Int,
    val EmployeeId: Int,
    val Sleve: String
)

data class SKUStoneMain(
    val Id: Int,
    val StoneMainName: String,
    val StoneMainWeight: String,
    val StoneMainPieces: String,
    val StoneMainRate: String,
    val StoneMainAmount: String,
    val StoneMainDescription: String,
    val SKUId: Int,
    val CompanyId: Int,
    val CounterId: Int,
    val BranchId: Int,
    val ClientCode: String,
    val EmployeeId: Int,
    val SKUStoneItem: List<SKUStoneItem>
)

data class SKUStoneItem(
    val Id: Int,
    val StoneName: String,
    val StoneWeight: String,
    val StonePieces: String,
    val StoneRate: String,
    val StoneAmount: String,
    val Description: String,
    val ClientCode: String,
    val SKUStoneMainId: Int,
    val StoneMasterId: Int,
    val SKUId: Int,
    val CompanyId: Int,
    val CounterId: Int,
    val BranchId: Int,
    val EmployeeId: Int,
    val Status: String?,
    val StoneLessPercent: String
)

