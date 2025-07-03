package com.loyalstring.rfid.data.remote.api

import ScannedDataToService
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeResponse
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.CategoryModel
import com.loyalstring.rfid.data.model.addSingleItem.DesignModel
import com.loyalstring.rfid.data.model.addSingleItem.InsertProductRequest
import com.loyalstring.rfid.data.model.addSingleItem.ProductModel
import com.loyalstring.rfid.data.model.addSingleItem.PurityModel
import com.loyalstring.rfid.data.model.addSingleItem.SKUModel
import com.loyalstring.rfid.data.model.addSingleItem.VendorModel
import com.loyalstring.rfid.data.model.login.LoginRequest
import com.loyalstring.rfid.data.model.login.LoginResponse
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.remote.response.AlllabelResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitInterface {
    /*Login*/
    @POST("api/ClientOnboarding/ClientOnboardingLogin")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /*get all vendor*/
    @POST("api/ProductMaster/GetAllPartyDetails")
    suspend fun getAllVendorDetails(@Body request: ClientCodeRequest): Response<List<VendorModel>>


    /*Get all SKU*/
    @POST("api/ProductMaster/GetAllSKU")
    suspend fun getAllSKUDetails(@Body request: ClientCodeRequest): Response<List<SKUModel>>

    /*Get all Category*/
    @POST("api/ProductMaster/GetAllCategory")
    suspend fun getAllCategoryDetails(@Body request: ClientCodeRequest): Response<List<CategoryModel>>

    /*Get all Category*/
    @POST("api/ProductMaster/GetAllProductMaster")
    suspend fun getAllProductDetails(@Body request: ClientCodeRequest): Response<List<ProductModel>>

    /*Get all design*/
    @POST("api/ProductMaster/GetAllDesign")
    suspend fun getAllDesignDetails(@Body request: ClientCodeRequest): Response<List<DesignModel>>

    /*Get all purity*/
    @POST("api/ProductMaster/GetAllPurity")
    suspend fun getAllPurityDetails(@Body request: ClientCodeRequest): Response<List<PurityModel>>

    //Get all products
    @POST("api/ProductMaster/GetAllLabeledStock")
    suspend fun getAllLabeledStock(@Body request: RequestBody): Response<List<AlllabelResponse.LabelItem>>

    /* insert single stock*/
    @POST("api/ProductMaster/InsertLabelledStock")
    suspend fun insertStock(
        @Body payload: List<InsertProductRequest>
    ): Response<List<PurityModel>>

    //AddScannedDataToWeb
    @POST("api/RFIDDevice/AddRFID")
    suspend fun addAllScannedData(@Body scannedDataToService: List<ScannedDataToService>): Response<List<ScannedDataToService>>


    //add employee api
    @POST("api/ClientOnboarding/AddCustomer")
    suspend fun addEmployee(
        @Body addEmployeeRequest: AddEmployeeRequest
    ): Response<EmployeeResponse>

    /*Get Emp List*/
    @POST("api/ClientOnboarding/GetAllCustomer") // Replace with your actual API endpoint
    suspend fun getAllEmpList(@Body clientCodeRequest: ClientCodeRequest): Response<List<EmployeeList>>

    //Lebel list
    @POST("api/ProductMaster/GetAllLabeledStock") // Replace with your actual API endpoint
    suspend fun getAllItemCodeList(@Body clientCodeRequest: ClientCodeRequest): Response<List<ItemCodeResponse>>


}