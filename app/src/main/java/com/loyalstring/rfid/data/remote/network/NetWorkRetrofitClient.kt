package com.loyalstring.rfid.data.remote.network

import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetWorkRetrofitClient {
    @Provides
    fun provideBaseUrl() = "https://rrgold.loyalstring.co.in/"

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideLoginApiService(retrofit: Retrofit): RetrofitInterface =
        retrofit.create(RetrofitInterface::class.java)

}