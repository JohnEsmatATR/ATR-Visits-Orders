package com.akhnaton.foodvisits.data.model.getCustomerData

data class GetCustomerDataRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)