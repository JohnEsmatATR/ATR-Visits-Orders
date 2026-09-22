package com.akhnaton.foodvisits.data.model.personCoding

import com.google.gson.JsonElement

data class MainCustomersLineModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)

data class MainCustomersLineData(
    val main_customer_line: List<MainCustomer>?
)

data class MainCustomer(
    val customer_name: String,
    val customer_code: String
) {
    override fun toString(): String = customer_name
}