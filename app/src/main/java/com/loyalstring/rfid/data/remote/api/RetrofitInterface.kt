package com.loyalstring.rfid.data.remote.api

import com.loyalstring.rfid.data.model.login.LoginRequest
import com.loyalstring.rfid.data.model.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitInterface {
    @POST("api/ClientOnboarding/ClientOnboardingLogin")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}