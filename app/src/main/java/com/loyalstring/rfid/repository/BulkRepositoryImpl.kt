package com.loyalstring.rfid.repository

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import com.loyalstring.rfid.data.remote.response.AlllabelResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class BulkRepositoryImpl @Inject constructor(
    private val apiService: RetrofitInterface,
    private val bulkItemDao: BulkItemDao
) : BulkRepository {

    override suspend fun insertBulkItems(items: List<BulkItem>) {
        bulkItemDao.insertBulkItem(items)
    }

    override suspend fun insertSingleItem(item: BulkItem) {
        bulkItemDao.insertSingleItem(item)
    }

    override fun getAllBulkItems(): Flow<List<BulkItem>> {
        return bulkItemDao.getAllItemsFlow()
    }

    override suspend fun clearAllItems() {
        bulkItemDao.clearAllItems()
    }


    override suspend fun syncBulkItemsFromServer(request: ClientCodeRequest): List<AlllabelResponse.LabelItem> {

        val jsonObject = JsonObject().apply {
            addProperty("ClientCode", request.clientcode)
        }

// Convert it to pretty JSON
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(jsonObject)

// Convert string to RequestBody
        val requestBody = prettyJson.toRequestBody("application/json".toMediaType())

        return try {
            val response = apiService.getAllLabeledStock(requestBody)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}


