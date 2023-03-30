package com.akhnaton.foodvisits.shared

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class RetrofitClient {

    companion object {
        var gson: Gson = GsonBuilder()
            .setLenient()
            .create()


        fun <T> getInstance(service: Class<T>): T {
            val retrofit = Retrofit.Builder()
                .baseUrl(ConstantLinks.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(SetupHttpClient().setupOkHttpClient())
                .build()
            return retrofit.create(service)
        }

    }

}