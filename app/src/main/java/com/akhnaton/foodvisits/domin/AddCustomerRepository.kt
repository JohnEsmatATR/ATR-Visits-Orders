package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IAddCustomer
import com.akhnaton.foodvisits.data.model.createNewCustomer.Governorate
import com.akhnaton.foodvisits.data.statusValue.addCustomer.GetGovernoratesIntent
import com.akhnaton.foodvisits.shared.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddCustomerRepository {
    private val retrofit = RetrofitClient.getInstance(IAddCustomer::class.java)

    suspend fun getCustomerType(version: String, token: String) =
        retrofit.getCustomerType(version, token)

    suspend fun getLines(version: String, token: String, customerType: String, orderType: String) =
        retrofit.getLines(version, token, customerType, orderType)


    suspend fun getMainLineCustomer(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        lineId: String
    ) =
        retrofit.getMainLineCustomer(version, token, customerType, orderType, lineId)

    suspend fun getGovernorates(
        version: String,
        token: String,
    ) = retrofit.getGovernment(version, token)

    suspend fun getAreas(
        version: String,
        token: String,
        governorate_id: String,
    ) = retrofit.getCity(version, token, governorate_id)

    suspend fun createNewCustomer(
        version: RequestBody,
        token: RequestBody,
        customerType: RequestBody,
        orderType: RequestBody,
        lineId: RequestBody,
        governorate: RequestBody,
        city: RequestBody,
        customerName: RequestBody,
        phone : RequestBody,
        secondPhone : RequestBody,
        customerAddress: RequestBody,
        nationalId: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        suggetsAddress : RequestBody,
        id_1: MultipartBody.Part,
        id_2: MultipartBody.Part,
    ) =
        retrofit.createNewCustomer(
            version,
            token,
            customerType,
            orderType,
            lineId,
            governorate,
            city,
            customerName,
            phone,
            secondPhone,
            customerAddress,
            nationalId,
            latitude,
            longitude,
            suggetsAddress,
            id_1,
            id_2
        )
}