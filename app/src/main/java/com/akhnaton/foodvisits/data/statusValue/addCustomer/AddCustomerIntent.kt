package com.akhnaton.foodvisits.data.statusValue.addCustomer

import com.akhnaton.foodvisits.data.model.createNewCustomer.Governorate
import okhttp3.MultipartBody
import okhttp3.RequestBody

sealed class AddCustomerIntent {


    data class GetCustomerType(val version: String, val token: String) : AddCustomerIntent()

    data class GetLines(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String
    ) : AddCustomerIntent()

    data class GetMainLines(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String,
        val lineId: String
    ) : AddCustomerIntent()

    data class CreateCustomer(
        val version: RequestBody,
        val token: RequestBody,
        val customerType: RequestBody,
        val orderType: RequestBody,
        val lineId: RequestBody,
        val governorate: RequestBody,
        val city: RequestBody,
        val customerName: RequestBody,
        val phone: RequestBody,
        val secondPhone: RequestBody,
        val customerAddress: RequestBody,
        val nationalId: RequestBody,
        val latitude: RequestBody,
        val longitude: RequestBody,
        val suggetsAddress : RequestBody,
        val id_1: MultipartBody.Part,
        val id_2: MultipartBody.Part,
    ) : AddCustomerIntent()
}