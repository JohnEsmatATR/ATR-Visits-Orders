package com.akhnaton.foodvisits.data.statusValue.addCustomer


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
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String,
        val lineId: String,
        val customerCode: String,
        val customerName: String,
        val customerAddress: String,
        val nationalId: String,
        val latitude: String,
        val longitude: String,
    ) : AddCustomerIntent()
}