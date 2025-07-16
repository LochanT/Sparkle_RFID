package com.loyalstring.rfid.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sparklepos.models.loginclasses.customerBill.AddEmployeeRequest
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeResponse
import com.google.gson.Gson
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.BranchModel
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.model.order.LastOrderNoResponse
import com.loyalstring.rfid.data.model.order.OrderItemModel
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.repository.OrderRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class OrderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: OrderRepository // or whatever your dependency is
) : ViewModel() {
    private val _addEmpResponse = MutableLiveData<Resource<EmployeeResponse>>()
    val addEmpReposnes: LiveData<Resource<EmployeeResponse>> = _addEmpResponse

    private val _empListResponse = MutableLiveData<List<EmployeeList>>()
    val empListResponse: LiveData<List<EmployeeList>> = _empListResponse

    private val _itemCodeResponse = MutableStateFlow<List<ItemCodeResponse>>(emptyList())
    val itemCodeResponse: StateFlow<List<ItemCodeResponse>> = _itemCodeResponse
    val isItemCodeLoading = MutableStateFlow(false)


    private val _lastOrderNOResponse = MutableStateFlow(LastOrderNoResponse())
    val lastOrderNoresponse: StateFlow<LastOrderNoResponse> = _lastOrderNOResponse

    private val _orderResponse = MutableStateFlow<CustomOrderResponse?>(null) // ✅
    val orderResponse: StateFlow<CustomOrderResponse?> = _orderResponse

    private val _allOrderItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val allOrderItems: StateFlow<List<OrderItem>> = _allOrderItems

    fun setOrderResponse(response: CustomOrderResponse) {
        _orderResponse.value = response
    }



    /*add employee*/
    fun addEmployee(request: AddEmployeeRequest) {
        viewModelScope.launch {
            _addEmpResponse.value = Resource.Loading()
            try {
                val gson = Gson()
                val json = gson.toJson(request)  // Convert to JSON string
                Log.d("AddEmployeeRequestJSON", json)
                val response = repository.AAddAllEmployeeDetails(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("@@", "Response Body: $body")
                    if (body != null) {
                        _addEmpResponse.value = Resource.Success(body)
                    } else {
                        _addEmpResponse.value = Resource.Error("Invalid response data")
                    }
                } else {
                    Log.d("@@", "Response Error: ${response.errorBody()?.string()}")
                    _addEmpResponse.value = Resource.Error("Server error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("@@", "Exception: ${e.message}")
                _addEmpResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    /*emp list function*/
    fun getAllEmpList(request: ClientCodeRequest) {
        viewModelScope.launch {
           // delay(2000)
            _addEmpResponse.value = Resource.Loading()
            try {
                val response = repository.getAllEmpList(request)
                if (response.isSuccessful && response.body() != null) {
                    _empListResponse.value = ((response.body()!!))

                    Log.d("SingleProductViewModel", "empList" + response.body())
                    repository.clearAllEmployees()
                    // Save to Room for offline use
                    repository.saveEmpListToRoom(response.body()!!)
                } else {
                    // Fallback to Room DB if API fails
                    val localData = repository.getAllEmpListFromRoom(request)
                    _empListResponse.value = localData
                }
            } catch (e: Exception) {
                // Fallback if there's an exception (like no internet)
                val localData = repository.getAllEmpListFromRoom(request)
                _empListResponse.value = localData
            }
        }
    }

    /*get all item code list*/
    fun getAllItemCodeList(request: ClientCodeRequest) {
        viewModelScope.launch {
            isItemCodeLoading.value = true
            delay(2000)
            try {
                val response = repository.getAllItemCodeList(request)
                if (response.isSuccessful && response.body() != null) {
                    _itemCodeResponse.value = response.body()!!
                    Log.d("OrderViewModel", "itemcode: ${response.body()}")
                } else {
                    _itemCodeResponse.value = emptyList()
                    Log.e("OrderViewModel", "Response error: ${response.code()}")
                }
            } catch (e: Exception) {
                _itemCodeResponse.value = emptyList()
                Log.e("OrderViewModel", "Exception: ${e.message}")
            }
            finally {
                isItemCodeLoading.value = false
            }
        }
    }

    /*customer order*/
    fun addOrderCustomer(request: CustomOrderRequest) {
        viewModelScope.launch {
            try {
                val response = repository.addOrder(request)
                if (response.isSuccessful && response.body() != null) {
                    _orderResponse.value = response.body()!!
                    Log.d("OrderViewModel", "Custom Order: ${response.body()}")
                } else {
                    _orderResponse.value = response.body()// ✅ Use default object
                    Log.e("OrderViewModel", "Custom Order Response error: ${response.code()}")
                }
            } catch (e: Exception) {
                _orderResponse.value = _orderResponse.value
                Log.e("OrderViewModel", "Custom Order Exception: ${e.message}")
            }
        }
    }

    /*last order no*/
    fun fetchLastOrderNo(request: ClientCodeRequest) {
        viewModelScope.launch {
            try {
                val response = repository.getLastOrderNo(request)
                if (response.isSuccessful && response.body() != null) {
                    _lastOrderNOResponse.value = response.body()!!
                    Log.d("OrderViewModel", "Last Order No: ${response.body()}")
                } else {
                    Log.e("OrderViewModel", "Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Exception: ${e.message}")
            }
        }
    }


    /*insert order item locally*/
    fun insertOrderItemToRoom(item: OrderItem) {
        viewModelScope.launch {
            try {
                repository.insertOrderItems(item)
                Log.d("OrderViewModel", "Order item inserted into Room: $item")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Room Insert Error: ${e.message}")
            }
        }
    }

    /*getAll order items from the roomdatbase*/

    fun getAllOrderItemsFromRoom() {
        viewModelScope.launch {
            repository.getAllOrderItems().collect { items ->
                _allOrderItems.value = items
                Log.d("OrderViewModel", "Fetched ${items.size} order items from Room")
            }
        }
    }

/*delete all order*/
    fun deleteAllOrders() {
        viewModelScope.launch {
            repository.deleteAllOrder()
        }
    }

    /*insert order item or update locally*/
    fun insertOrderItemToRoomORUpdate(item: OrderItem) {
        viewModelScope.launch {
            try {
                repository.insertORUpdate(item)
                Log.d("OrderViewModel", "Order item updated into Room: $item")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Room update Error: ${e.message}")
            }
        }
    }





}