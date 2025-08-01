package com.loyalstring.rfid.repository

import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeResponse
import com.loyalstring.rfid.data.local.dao.OrderItemDao
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.model.order.LastOrderNoResponse
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

    /*get All order list*/
    suspend fun getAllOrderList(clientCodeRequest: ClientCodeRequest): Response<List<CustomOrderResponse>> {
        return apiService.getAllOrderList(clientCodeRequest)
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
  /*  suspend fun getAllEmpListLocal(clientCodeRequest: ClientCodeRequest): Response<List<AddEmployeeRequest>> {
        return apiService.getAllEmpList(clientCodeRequest)
    }*/

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

    /**************last order no *****************/
    suspend fun getLastOrderNoFromRoom(clientCodeRequest: ClientCodeRequest): LastOrderNoResponse {
        return orderItemDao.getLastOrderNo()
    }
    /*local database save  last order no data*/
    suspend fun saveLastOrderNoToRoom(orderNo: LastOrderNoResponse) {
        orderItemDao.insertLastOrderNo(orderNo)
    }

    suspend fun clearLastOrderNo() {
        orderItemDao.clearLastOrderNo()
    }


    // Save the custom order response to Room
    suspend fun saveCustomerOrder(request: CustomOrderRequest) {
        orderItemDao.insertCustomerOrder(request)
    }

    // Get all customer order responses from Room based on the client code
    suspend fun getAllCustomerOrders(clientCode: String): List<CustomOrderRequest> {
        return orderItemDao.getAllCustomnerOrderReponse(clientCode)
    }

    // Delete unsynced orders (syncStatus = 0)
    suspend fun deleteUnsyncedOrders() {
        orderItemDao.deleteUnsyncedOrder()
    }

  /*  // Optionally, you can add syncing logic here (to handle sync with the server)
    suspend fun syncOrdersToServer(customOrderResponse: CustomOrderResponse): Response<CustomOrderResponse> {
        // Implement your API call here
        return apiService.syncOrders(customOrderResponse)
    }*/
}