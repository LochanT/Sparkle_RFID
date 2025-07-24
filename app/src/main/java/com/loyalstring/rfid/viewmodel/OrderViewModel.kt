package com.loyalstring.rfid.viewmodel

import android.content.Context
import android.util.Log
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
import com.loyalstring.rfid.data.model.order.CustomOrderRequest
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
import com.loyalstring.rfid.data.model.order.LastOrderNoResponse
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.repository.OrderRepository
import com.loyalstring.rfid.ui.utils.NetworkUtils

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: OrderRepository // or whatever your dependency is
) : ViewModel() {
    private val _addEmpResponse = MutableLiveData<Resource<EmployeeResponse>>()
    val addEmpReposnes: LiveData<Resource<EmployeeResponse>> = _addEmpResponse




   /* private val _empListResponse = MutableLiveData<List<EmployeeList>>()
    val empListResponse: LiveData<List<EmployeeList>> = _empListResponse
    val empListFlow = _empListResponse.asStateFlow()*/

    private val _empListFlow = MutableStateFlow<UiState<List<EmployeeList>>>(UiState.Loading)
    val empListFlow: StateFlow<UiState<List<EmployeeList>>> = _empListFlow
    val isEmpListLoading = MutableStateFlow(false)


    private val _itemCodeResponse = MutableStateFlow<List<ItemCodeResponse>>(emptyList())
    val itemCodeResponse: StateFlow<List<ItemCodeResponse>> = _itemCodeResponse
    val isItemCodeLoading = MutableStateFlow(false)


    private val _lastOrderNOResponse = MutableStateFlow(LastOrderNoResponse())
    val lastOrderNoresponse: StateFlow<LastOrderNoResponse> = _lastOrderNOResponse

    private val _orderResponse = MutableStateFlow<CustomOrderResponse?>(null) // ✅
    val orderResponse: StateFlow<CustomOrderResponse?> = _orderResponse

    private val _allOrderItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val allOrderItems: StateFlow<List<OrderItem>> = _allOrderItems


    private val _insertOrderOffline = MutableStateFlow<CustomOrderRequest?>(null) // ✅
    val insertOrderOffline: StateFlow<CustomOrderRequest?> = _insertOrderOffline


    private val _getAllOrderList = MutableStateFlow<List<CustomOrderResponse>>(emptyList())
    val getAllOrderList: StateFlow<List<CustomOrderResponse>> = _getAllOrderList

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

  /*  fun getAllEmpList(clientCode: String) {
        viewModelScope.launch {
           // delay(1000)
            isEmpListLoading.value = true

            try {
                val response = repository.getAllEmpList(ClientCodeRequest(clientCode)) // API call

                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    val data = response.body()

                    // Save to Room
                    // repository.clearAllEmployees()
                  // repository.saveEmpListToRoom(data!!)

                  *//*  if (!data.isNullOrEmpty()) {
                        withContext(Dispatchers.IO) {
                            val chunkSize = 10 // try with 500–1000
                            data.chunked(chunkSize).forEach { chunk ->
                                repository.saveEmpListToRoom(chunk)
                            }
                        }
                    }*//*
                   // repository.saveEmpListToRoom(data!!)
                    _empListFlow.value = UiState.Success(data!!)

                } else {
                    val localData = repository.getAllEmpListFromRoom(ClientCodeRequest(clientCode))
                    _empListFlow.value = UiState.Success(localData)

                    // API failed => try loading from local DB
                  *//*  val localData = repository.getAllEmpListFromRoom(ClientCodeRequest(clientCode))
                    val employeeList = localData.map { it.toEmployeeList() }  // Convert to List<EmployeeList>
                    _empListFlow.value = UiState.Success(employeeList!!)*//*
                }

            } catch (e: Exception) {
                val localData = repository.getAllEmpListFromRoom(ClientCodeRequest(clientCode))
                _empListFlow.value = UiState.Success(localData)
                // Exception (e.g., no internet) => try loading from local DB
              *//*  val localData = repository.getAllEmpListFromRoom(ClientCodeRequest(clientCode))
                val employeeList = localData.map { it.toEmployeeList() }  // Convert to List<EmployeeList>
                _empListFlow.value = UiState.Success(employeeList!!)*//*
            } finally {
                isEmpListLoading.value = false
            }
        }
    }
*/

    fun getAllEmpList(clientCode: String) {
        viewModelScope.launch {
          //  delay(1000)
            isEmpListLoading.value = true

            try {
                val response = repository.getAllEmpList(ClientCodeRequest(clientCode)) // API call

                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    val data = response.body()

                    // Save to Room
                    // repository.clearAllEmployees()
                     repository.saveEmpListToRoom(data!!)

                    _empListFlow.value = UiState.Success(data!!)

                } else {
                    // API failed => try loading from local DB
                   /* val localData = repository.getAllEmpListFromRoom(ClientCodeRequest(clientCode))
                    _empListFlow.value = UiState.Success(localData)*/
                }

            } catch (e: Exception) {
                // Exception (e.g., no internet) => try loading from local DB
               /* val localData = repository.getAllEmpListFromRoom(ClientCodeRequest(clientCode))
                _empListFlow.value = UiState.Success(localData)*/
            } finally {
                isEmpListLoading.value = false
            }
        }
    }
    fun AddEmployeeRequest.toEmployeeList(): EmployeeList {
        return EmployeeList(
            custId = 0, // auto-generate in Room
            Id = this.Id?.toIntOrNull() ?: 0,
            FirstName = this.FirstName.orEmpty(),
            LastName = this.LastName.orEmpty(),
            PerAddStreet = this.PerAddStreet.orEmpty(),
            CurrAddStreet = this.CurrAddStreet.orEmpty(),
            Mobile = this.Mobile.orEmpty(),
            Email = this.Email.orEmpty(),
            Password = this.Password.orEmpty(),
            CustomerLoginId = this.CustomerLoginId.orEmpty(),
            DateOfBirth = this.DateOfBirth.orEmpty(),
            MiddleName = this.MiddleName.orEmpty(),
            PerAddPincode = this.PerAddPincode.orEmpty(),
            Gender = this.Gender,
            OnlineStatus = this.OnlineStatus,
            CurrAddTown = this.PerAddTown, // using PerAddTown because CurrAddTown not in API
            CurrAddPincode = this.CurrAddPincode.orEmpty(),
            CurrAddState = this.CurrAddState.orEmpty(),
            PerAddTown = this.PerAddTown.orEmpty(),
            PerAddState = this.PerAddState,
            GstNo = this.GstNo.orEmpty(),
            PanNo = this.PanNo.orEmpty(),
            AadharNo = this.AadharNo.orEmpty(),
            BalanceAmount = this.BalanceAmount.orEmpty(),
            AdvanceAmount = this.AdvanceAmount.orEmpty(),
            Discount = this.Discount.orEmpty(),
            CreditPeriod = this.CreditPeriod,
            FineGold = this.FineGold.orEmpty(),
            FineSilver = this.FineSilver.orEmpty(),
            ClientCode = this.ClientCode.orEmpty(),
            VendorId = this.VendorId ?: 0,
            AddToVendor = this.AddToVendor ?: false,
            CustomerSlabId = this.CustomerSlabId ?: 0,
            CreditPeriodId = this.CreditPeriodId ?: 0,
            RateOfInterestId = this.RateOfInterestId ?: 0,
            CustomerSlab = null,
            RateOfInterest = null,
            CreatedOn = "", // not in API, can be current time or empty
            LastUpdated = "", // not in API
            StatusType = true,
            Remark = this.Remark.orEmpty(),
            Area = this.Area.orEmpty(),
            City = this.City.orEmpty(),
            Country = this.Country.orEmpty()
        )
    }



    fun EmployeeList.toAddEmployeeRequest(): AddEmployeeRequest {
        return AddEmployeeRequest(
            Id = this.Id.toString(),
            FirstName = this.FirstName,
            MiddleName = this.MiddleName,
            LastName = this.LastName,
            Email = this.Email,
            CustomerLoginId = this.CustomerLoginId,
            Password = this.Password,
            Gender = this.Gender,
            CustomerSlabId = this.CustomerSlabId,
            CreditPeriodId = this.CreditPeriodId,
            RateOfInterestId = this.RateOfInterestId,
            Mobile = this.Mobile,
            OnlineStatus = this.OnlineStatus,
            DateOfBirth = this.DateOfBirth,
            AdvanceAmount = this.AdvanceAmount,
            BalanceAmount = this.BalanceAmount,
            CurrAddStreet = this.CurrAddStreet,
            Area = this.Area,
            PerAddTown = this.PerAddTown,
            City = this.City,
            CurrAddState = this.CurrAddState,
            CurrAddPincode = this.CurrAddPincode,
            PerAddStreet = this.PerAddStreet,
            PerAddState = this.PerAddState,
            PerAddPincode = this.PerAddPincode,
            Country = this.Country,
            PerAddCountry = null, // You don't have PerAddCountry in EmployeeList, so set null
            AadharNo = this.AadharNo,
            Discount = this.Discount,
            CreditPeriod = this.CreditPeriod,
            PanNo = this.PanNo,
            FineGold = this.FineGold,
            FineSilver = this.FineSilver,
            GstNo = this.GstNo,
            ClientCode = this.ClientCode,
            VendorId = this.VendorId,
            Remark = this.Remark,
            AddToVendor = this.AddToVendor
        )
    }


    /*    fun getAllEmpList(request: String) {
            viewModelScope.launch {//
                isEmpListLoading.value=true
                 delay(2000)

                try {
                    val response = repository.getAllEmpList(ClientCodeRequest(request))
                    if (response.isSuccessful && response.body() != null) {
                      *//*  _empListResponse.value = ((response.body()!!))

                    Log.d("SingleProductViewModel", "empList" + response.body())
                    repository.clearAllEmployees()
                    // Save to Room for offline use
                    repository.saveEmpListToRoom(response.body()!!)*//*
                    val data = response.body()!!

                    Log.d("SingleProductViewModel", "empList: $data")

                   // repository.clearAllEmployees()
                    repository.saveEmpListToRoom(data)

                    _empListFlow.value =  UiState.Success(response.body()!!)

                } else {
                    // Fallback to Room DB if API fails
                   // val localData = repository.getAllEmpListFromRoom(request.toString())
                    //_empListFlow.value =  UiState.Success(localData)
                }
            } catch (e: Exception) {
                // Fallback if there's an exception (like no internet)
               // val localData = repository.getAllEmpListFromRoom(request)
               // _empListFlow.value = UiState.Success( localData)
            } finally {
                isEmpListLoading.value = false
            }
        }
    }*/

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
                    repository.saveAllItemCodeToRoom(response.body()!!)
                } else {
                    val localData = repository.getAllItemCodeFromRoom(request)
                    _itemCodeResponse.value = localData
                    Log.e("OrderViewModel", "Response error: ${response.code()}")
                }
            } catch (e: Exception) {
                val localData = repository.getAllItemCodeFromRoom(request)
                _itemCodeResponse.value = localData
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
                    //repository.clearLastOrderNo()
                    repository.saveLastOrderNoToRoom(response.body()!!)
                    Log.d("OrderViewModel", "Last Order No: ${response.body()}")
                } else {
                    Log.e("OrderViewModel", "Error: ${response.code()} ${response.message()}")
                    val localData = repository.getLastOrderNoFromRoom(request)
                    _lastOrderNOResponse.value = localData
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Exception: ${e.message}")
                val localData = repository.getLastOrderNoFromRoom(request)
                _lastOrderNOResponse.value = localData
            }
        }
    }

    /*get All order list in list screen*/
    /*last order no*/
    fun fetchAllOrderListFromApi(request: ClientCodeRequest) {
        viewModelScope.launch {
            try {
                val response = repository.getAllOrderList(request)
                if (response.isSuccessful && response.body() != null) {
                    _getAllOrderList.value = response.body()!!
                    //repository.clearLastOrderNo()
                   // repository.saveLastOrderNoToRoom(response.body()!!)
                    Log.d("OrderViewModel", "get All order list: ${response.body()}")
                } else {
                    Log.e("OrderViewModel", "Error: ${response.code()} ${response.message()}")
                    /*val localData = repository.getLastOrderNoFromRoom(request)
                    _lastOrderNOResponse.value = localData*/
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Exception: ${e.message}")
              /*  val localData = repository.getLastOrderNoFromRoom(request)
                _lastOrderNOResponse.value = localData*/
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


    /*sync data to server*/
    // Save the customer order to Room
    fun saveOrder(customerOrderRequest: CustomOrderRequest) {
        viewModelScope.launch {
            try {
                repository.saveCustomerOrder(customerOrderRequest)
                _insertOrderOffline.value = (customerOrderRequest!!)
                Log.d("@@","@@"+customerOrderRequest)
            } catch (e: Exception) {
                _insertOrderOffline.value = (customerOrderRequest!!)
                Log.d("@@","@@"+e.toString())
            }
        }
    }

    // Fetch all customer orders based on the client code
    fun getAllOrders(clientCode: String) {
        viewModelScope.launch {
            try {
                val orders = repository.getAllCustomerOrders(clientCode)
               // _orderResponse.value = (orders)
            } catch (e: Exception) {
               // _orderResponse.value =("Failed to fetch orders: ${e.message}")
            }
        }
    }

    // Delete unsynced orders (syncStatus = 0)
    fun deleteUnsyncedOrders() {
        viewModelScope.launch {
            try {
                repository.deleteUnsyncedOrders()
                //_orderResponse.value = ("Unsynced orders deleted successfully.")
            } catch (e: Exception) {
               // _orderResponse.value = UiState.Error("Failed to delete unsynced orders: ${e.message}")
            }
        }
    }

   /* fun syncDataWhenOnline() {
        // Check if the device is online (Use a utility method or Network API to check connectivity)
        if (NetworkUtils.isNetworkAvailable(context)) {
            // Fetch unsynced data from Room and sync to server
            viewModelScope.launch {
                val unsyncedOrders = repository.getAllCustomerOrders("clientCode") // Replace with actual client code
               *//* unsyncedOrders.filter { !it.syncStatus } // Filter unsynced orders

                unsyncedOrders.forEach { order ->
                    val response = repository.addOrder(order)
                    if (response.isSuccessful) {
                        // Mark as synced after successful sync
                        repository.deleteUnsyncedOrders() // Delete unsynced orders from Room after syncing
                    }
                }*//*
            }
        } else {
            Log.d("Sync", "No internet available. Will retry when online.")
        }
    }*/
   fun syncDataWhenOnline() {
       viewModelScope.launch {
           val unsyncedOrders = repository.getAllCustomerOrders("clientCode")
           for (order in unsyncedOrders) {
               try {
                   val response = repository.addOrder(order)
                   if (response.isSuccessful) {
                       repository.addOrder(order)
                       Log.e("Sync", "Successfully done")
                   }
               } catch (e: Exception) {
                   Log.e("Sync", "Failed to sync: ${e.message}")
               }
           }
       }
   }



    // Sync orders to the server (if needed)
    /*fun syncOrders(customOrderResponse: CustomOrderResponse) {
        viewModelScope.launch {
            try {
                val response = repository.addOrder(customOrderResponse)
                if (response.isSuccessful) {
                  //  _orderResponse.value = UiState.Success("Orders synced successfully.")
                } else {
                  //  _orderResponse.value = UiState.Error("Failed to sync orders.")
                }
            } catch (e: Exception) {
             //   _orderResponse.value = UiState.Error("Error syncing orders: ${e.message}")
            }
        }
    }*/
}






