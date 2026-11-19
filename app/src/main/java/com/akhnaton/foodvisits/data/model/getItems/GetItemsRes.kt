package com.akhnaton.foodvisits.data.model.getItems

data class GetItemsRes(
    val `data`: List<Data>,
    val message: String,
    val pagination: Pagination,
    val params: Params,
    val order_summary: OrderSummary?,
    val status: Int,
    val type: String
)