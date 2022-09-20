package com.akhnaton.foodvisits.data.model.orderHistory


data class OrderHistory(
    var status: Int,
    var data: List<OrderHistoryData>
)

data class OrderHistoryData(
    val orig_sys_document_ref: String,
    val order_created_at: Int,
    val customer_name: String,
    val customer_site_address: String,
    val order_total_price: Float,
    val order_return_price: Float,
)