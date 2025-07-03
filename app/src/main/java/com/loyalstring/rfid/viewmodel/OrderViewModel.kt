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
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.order.ItemCodeResponse
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
            _addEmpResponse.value = Resource.Loading()
            try {
                val response = repository.getAllEmpList(request)
                if (response.isSuccessful && response.body() != null) {
                    _empListResponse.value = ((response.body()!!))

                    Log.d("SingleProductViewModel", "empList" + response.body())
                } else {
                    _empListResponse.value = response.body()
                }
            } catch (e: Exception) {

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



}