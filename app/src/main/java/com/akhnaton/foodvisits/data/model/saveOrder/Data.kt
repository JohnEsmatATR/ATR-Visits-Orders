package com.akhnaton.foodvisits.data.model.saveOrder

data class Data(
    val items_summary: List<ItemsSummary>,
    val order_number: String,
    val order_summary: OrderSummary,
    val order_total: Double
)