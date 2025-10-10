package com.loyalstring.rfid.data.remote.network

import android.content.Context
import com.loyalstring.rfid.data.remote.api.RetrofitInterface
import com.loyalstring.rfid.ui.utils.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetWorkRetrofitClient {
    @Provides
    fun provideBaseUrl() = "https://rrgold.loyalstring.co.in/"


    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY

        }
    }

    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // ⏱️ connection timeout
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // ⏱️ server response read timeout
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // ⏱️ client request write timeout
            .addInterceptor(loggingInterceptor)
            .build()
    }


  /*  @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        baseUrl: String
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl) // replace with your URL
            .addConverterFactory(GsonConverterFactory.create()) // or MoshiConverterFactory.create()
            .client(okHttpClient)
            .build()
    }*/
  @Provides
  @Singleton
  fun provideRetrofit(
      @ApplicationContext context: Context,
      okHttpClient: OkHttpClient,
      defaultBaseUrl: String
  ): Retrofit {

      val userPrefs = UserPreferences.getInstance(context)
      val customBaseUrl = userPrefs.getCustomApi()   // ✅ Fetch custom base URL if set

      // ✅ Use custom URL if exists, else fallback to default
      val baseUrl = if (!customBaseUrl.isNullOrEmpty()) {
          if (!customBaseUrl.endsWith("/")) "$customBaseUrl/" else customBaseUrl
      } else {
          defaultBaseUrl
      }

      return Retrofit.Builder()
          .baseUrl(baseUrl)
          .addConverterFactory(GsonConverterFactory.create())
          .client(okHttpClient)
          .build()
  }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): RetrofitInterface {
        return retrofit.create(RetrofitInterface::class.java)
    }

}