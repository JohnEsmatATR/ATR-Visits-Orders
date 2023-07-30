package com.akhnaton.foodvisits.data.statusValue.supervisor.orderDetails

sealed class OrderDetailsIntent {

    data class GetOrderDetails(
        val app_version: String,
        val api_token: String,
        val orderNumber: String,
        val superId: String,
        val order_total_price: String,
        val customer_id:String,
    ) : OrderDetailsIntent()

}