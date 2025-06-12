package com.loyalstring.rfid.data.remote.api

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
import com.loyalstring.rfid.data.remote.data.AlllabelResponse
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

    /*Get all insert stock*/
    @POST("api/ProductMaster/InsertLabelledStock")
    suspend fun insertStock(@Body request: InsertProductRequest): Response<List<PurityModel>>



}