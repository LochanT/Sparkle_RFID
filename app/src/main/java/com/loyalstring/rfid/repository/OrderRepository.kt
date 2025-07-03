package com.loyalstring.rfid.repository

import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeResponse
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import retrofit2.Response
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val apiService: RetrofitInterface
) {
    suspend fun AAddAllEmployeeDetails(request: AddEmployeeRequest): Response<EmployeeResponse> {
        return apiService.addEmployee(request)
    }

    suspend fun getAllEmpList(clientCodeRequest: ClientCodeRequest): Response<List<EmployeeList>> {
        return apiService.getAllEmpList(clientCodeRequest)
    }

    suspend fun getAllItemCodeList(clientCodeRequest: ClientCodeRequest): Response<List<ItemCodeResponse>> {
        return apiService.getAllItemCodeList(clientCodeRequest)
    }
}