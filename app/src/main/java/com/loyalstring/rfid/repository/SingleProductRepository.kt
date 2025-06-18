package com.loyalstring.rfid.repository

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
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import com.loyalstring.rfid.ui.utils.ToastUtils
import retrofit2.Response
import javax.inject.Inject

class SingleProductRepository @Inject constructor(
    private val apiService: RetrofitInterface
) {
    suspend fun getAllVendorDetails(request: ClientCodeRequest): Response<List<VendorModel>> {
        return apiService.getAllVendorDetails(request)
    }

    suspend fun getAllSKUDetails(request: ClientCodeRequest): Response<List<SKUModel>> {
        return apiService.getAllSKUDetails(request)
    }

    suspend fun getAllCategoryDetails(request: ClientCodeRequest): Response<List<CategoryModel>> {
        return apiService.getAllCategoryDetails(request)
    }

    suspend fun getAllProductDetails(request: ClientCodeRequest): Response<List<ProductModel>> {
        return apiService.getAllProductDetails(request)
    }

    suspend fun getAllDesignDetails(request: ClientCodeRequest): Response<List<DesignModel>> {
        return apiService.getAllDesignDetails(request)
    }

    suspend fun getAllPurityDetails(request: ClientCodeRequest): Response<List<PurityModel>> {
        return apiService.getAllPurityDetails(request)
    }
    suspend fun insertLabelledStock(request: InsertProductRequest): Result<List<PurityModel>> {
        return try {
            val payload = listOf(request) // not a single object
            val response = apiService.insertStock(payload)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}