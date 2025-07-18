package com.loyalstring.rfid.repository

import androidx.room.Query
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeResponse
import com.loyalstring.rfid.data.local.dao.OrderItemDao
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.model.order.LastOrderNoResponse
import com.loyalstring.rfid.data.model.order.OrderItemModel
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val apiService: RetrofitInterface,
    private val orderItemDao: OrderItemDao
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



    suspend fun addOrder(customOrderRequest: CustomOrderRequest): Response<CustomOrderResponse> {
        return apiService.addOrder(customOrderRequest)
    }


    suspend fun getLastOrderNo(clientCodeRequest: ClientCodeRequest): Response<LastOrderNoResponse> {
        return apiService.getLastOrderNo(clientCodeRequest)
    }



    suspend fun insertOrderItems(items: OrderItem) {
        orderItemDao.insertOrderItem(items)
    }

    fun getAllOrderItems(): Flow<List<OrderItem>> {
        return orderItemDao.getAllOrderItem()
    }
    suspend fun deleteAllOrder() {
        orderItemDao.clearAllItems()
    }

    suspend fun insertORUpdate(items: OrderItem) {
        orderItemDao.insertOrUpdate(items)
    }


    /*local database get all employee data*/
    suspend fun getAllEmpListFromRoom(clientCodeRequest: ClientCodeRequest): List<EmployeeList> {
        return orderItemDao.getAllEmployees(clientCodeRequest.clientcode.toString())
    }
    /*local database save  all employee data*/
    suspend fun saveEmpListToRoom(empList: List<EmployeeList>) {
        orderItemDao.insertAll(empList)
    }

    suspend fun clearAllEmployees() {
        orderItemDao.clearAllEmployees()
    }

    suspend fun getAllItemCodeFromRoom(clientCodeRequest: ClientCodeRequest): List<ItemCodeResponse> {
        return orderItemDao.getAllItemCode(clientCodeRequest.clientcode.toString())
    }
    /*local database save  all employee data*/
    suspend fun saveAllItemCodeToRoom(itemCodeList: List<ItemCodeResponse>) {
        orderItemDao.insertAllItemCode(itemCodeList)
    }

    suspend fun clearAllItemCode() {
        orderItemDao.clearAllItemCode()
    }
}