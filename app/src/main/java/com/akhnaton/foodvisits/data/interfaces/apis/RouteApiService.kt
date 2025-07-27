package com.akhnaton.foodvisits.data.interfaces.apis

import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RouteApiService {

    @POST("v2/directions/driving-car/geojson")
    suspend fun getRoute(@Body body: RequestBody): Response<ResponseBody>
}