package com.akhnaton.foodvisits.shared

import com.akhnaton.foodvisits.shared.gson.StringOrListAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class RetrofitWaveClient {

    companion object {
        var gson: Gson = GsonBuilder()
            .registerTypeAdapter(
                object : TypeToken<List<String>>() {}.type,
                StringOrListAdapter()
            )
            .setLenient()
            .create()

        fun <T> getInstance(service: Class<T>): T {
            val retrofit = Retrofit.Builder()
                .baseUrl(ConstantLinks.PROD_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(SetupHttpClient().setupOkHttpClient())
                .build()
            return retrofit.create(service)
        }
    }
}