package com.loyalstring.rfid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalstring.rfid.data.model.login.LoginRequest
import com.loyalstring.rfid.data.model.login.LoginResponse
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository // or whatever your dependency is
) : ViewModel() {

    private val _loginResponse = MutableLiveData<Resource<LoginResponse>>()
    val loginResponse: LiveData<Resource<LoginResponse>> = _loginResponse

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginResponse.value = Resource.Loading()
            try {
                val response = repository.login(request)
                if (response.isSuccessful) {
                    _loginResponse.value = Resource.Success(response.body())
                } else {
                    _loginResponse.value = Resource.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }
}