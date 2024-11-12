package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ICustomerCoding
import com.akhnaton.foodvisits.data.interfaces.apis.IOrder
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CustomerCodingRepository {

    private val retrofit = RetrofitClient.getInstance(ICustomerCoding::class.java)

    suspend fun getTypes(
        appVersion: String,
        apiToken: String,
        userId: String,
    ) = retrofit.getTypes(
        app_version = appVersion,
        api_token = apiToken,
        userId = userId,
    )

    suspend fun getLines(
        appVersion: String,
        apiToken: String,
        userId: String,
        custType: String,
    ) = retrofit.getLines(
        app_version = appVersion,
        api_token = apiToken,
        userId = userId,
        custType = custType
    )

    suspend fun getCategories(
        appVersion: String,
        apiToken: String,
        userId: String,
        custType: String,
        lineId: String,
    ) = retrofit.getCategories(
        app_version = appVersion,
        api_token = apiToken,
        userId = userId,
        custType = custType,
        lineId = lineId,
    )

    suspend fun getAreas(
        appVersion: String,
        apiToken: String,
        userId: String,
    ) = retrofit.getAreas(
        app_version = appVersion,
        api_token = apiToken,
        userId = userId,
    )

    suspend fun sendCustomer(
        appVersion: RequestBody,
        apiToken: RequestBody,
        userId: RequestBody,
        custType: RequestBody,
        lineId: RequestBody,
        categoryId: RequestBody,
        area: RequestBody,
        customerName: RequestBody,
        customerAddress: RequestBody,
        phoneNumber: RequestBody,
        mobileNumber: RequestBody,
        customerNationalId: RequestBody,
        nameInNationalId: RequestBody,
        addressInNationalId: RequestBody,
//        id_1: MultipartBody.Part,
//        id_2: MultipartBody.Part,
        long: RequestBody,
        lat: RequestBody,
    ) = retrofit.getSendData(
        app_version = appVersion,
        api_token = apiToken,
        userId = userId,
        custType = custType,
        lineId = lineId,
        categoryId = categoryId,
        area = area,
        customerName = customerName,
        customerAddress = customerAddress,
        phoneNumber = phoneNumber,
        mobileNumber = mobileNumber,
        customerNationalId = customerNationalId,
        nameInNationalId = nameInNationalId,
        addressInNationalId = addressInNationalId,
//        id_1 = id_1,
//        id_2 = id_2,
        long = long,
        lat = lat,
    )
}
