package com.akhnaton.foodvisits.data.model.customers

data class GetCustomersRes(
    val `data`: List<Data>,
    val message: String,
    val status: Int,
    val type: String
)