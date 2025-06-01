package com.akhnaton.foodvisits.data.statusValue.customerCoding

import okhttp3.MultipartBody
import okhttp3.RequestBody

sealed class CustomerCodingIntent {

    data class GetTypes(
        val app_version: String,
        val api_token: String,
        val userId: String,
    ) : CustomerCodingIntent()

    data class GetLines(
        val app_version: String,
        val api_token: String,
        val userId: String,
        val custType: String,
    ) : CustomerCodingIntent()

    data class GetCategories(
        val app_version: String,
        val api_token: String,
        val userId: String,
        val custType: String,
        val lineId: String,
    ) : CustomerCodingIntent()

    data class GetAreas(
        val app_version: String,
        val api_token: String,
        val userId: String,
    ) : CustomerCodingIntent()

    data class SendCustomer(
        val app_version: RequestBody,
        val api_token: RequestBody,
        val user_id: RequestBody,
        val cust_type: RequestBody,
        val line_id: RequestBody,
        val cust_code_id: RequestBody,
        val area: RequestBody,
        val customer_name: RequestBody,
        val customer_address: RequestBody,
        val phoneNumber: RequestBody,
        val mobileNumber: RequestBody,
        val customer_national_id: RequestBody,
        val name_in_national_id: RequestBody,
        val address_in_national_id: RequestBody,
        val id_1: MultipartBody.Part,
        val id_2: MultipartBody.Part,
        val long: RequestBody,
        val lat: RequestBody,

    ) : CustomerCodingIntent()

}