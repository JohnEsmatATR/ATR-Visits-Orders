package com.akhnaton.foodvisits.data.model.getList

data class OrdersFilter(
    val status: String,
    val fromDate: String,
    val toDate: String,
    val orderType: String
)