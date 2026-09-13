package com.akhnaton.foodvisits.data.model.personCoding

data class AddCustomerModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: Any? = null
)