package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IAddCustomer
import com.akhnaton.foodvisits.shared.RetrofitClient

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

    suspend fun createNewCustomer(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        lineId: String,
        customerCode: String,
        customerName: String,
        customerAddress: String,
        nationalId: String,
        latitude: String,
        longitude: String,
    ) =
        retrofit.createNewCustomer(version, token, customerType,orderType,lineId,customerCode, customerName, customerAddress, nationalId, latitude, longitude)
}