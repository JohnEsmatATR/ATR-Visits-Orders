package com.akhnaton.foodvisits.data.statusValue.orderHistory

sealed class OrderHistoryIntent {

    data class OrderHistory(
        val token: String,
        val version: String,
        val to: String,
        val from: String,
    ) : OrderHistoryIntent()


    data class OrderHistoryDetails(
        val token: String,
        val version: String,
        val orderNumber: String,
    ) : OrderHistoryIntent()
}