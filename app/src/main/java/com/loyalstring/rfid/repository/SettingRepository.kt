package com.loyalstring.rfid.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeResponse
import com.loyalstring.rfid.data.local.dao.OrderItemDao
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderUpdateResponse
import com.loyalstring.rfid.data.model.setting.UpdateDailyRatesReq
import com.loyalstring.rfid.data.model.setting.UpdateDailyRatesResponse
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import kotlinx.coroutines.launch
import retrofit2.Response


import javax.inject.Inject

class SettingRepository @Inject constructor(
    private val apiService: RetrofitInterface,

) {
    /*update order*/
    suspend fun updateDailyRates(updateDailyRatesReq: List<UpdateDailyRatesReq>): Response<List<UpdateDailyRatesResponse>> {
        return apiService.updateDailyRate(updateDailyRatesReq)
    }

}