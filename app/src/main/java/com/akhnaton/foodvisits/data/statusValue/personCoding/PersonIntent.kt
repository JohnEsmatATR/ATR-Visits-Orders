package com.akhnaton.foodvisits.data.statusValue.personCoding

import okhttp3.MultipartBody
import okhttp3.RequestBody

sealed class PersonIntent {
    object GetSalesAndCustomerTypes : PersonIntent()
    data class GetLines(val saleType: String, val customerType: String) : PersonIntent()
    data class GetMainCustomersLine(
        val lineId: String,
        val orderType: String,
        val customerType: String
    ) : PersonIntent()
    data class RefreshToken(val userId: String, val token: String) : PersonIntent()
    object GetUserAreas : PersonIntent()
    data class GetAreasByGovernorate(val governorateId: String) : PersonIntent()
    data class AddCustomer(
        val fields: Map<String, RequestBody>,
        val frontImage: MultipartBody.Part?,
        val backImage: MultipartBody.Part?
    ) : PersonIntent()
}