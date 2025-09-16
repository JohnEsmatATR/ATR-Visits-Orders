package com.akhnaton.foodvisits.shared

import android.util.Log
import com.akhnaton.foodvisits.data.interfaces.apis.RouteApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

abstract class RouteRetrofitClient {
    companion object {
        private fun getClient(apiKey: String): OkHttpClient {
            val authInterceptor = Interceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", apiKey)
                    .addHeader("Content-Type","application/json")
                    .build()
                val response = chain.proceed(request)
                response
            }
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            return OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        }

        fun create(apiKey: String): RouteApiService {
            return Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .client(getClient(apiKey))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(RouteApiService::class.java)
        }
    }
}

