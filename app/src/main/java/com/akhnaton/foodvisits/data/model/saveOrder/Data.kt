package com.akhnaton.foodvisits.data.model.saveOrder

data class Data(
    val items_summary: List<ItemsSummary>,
    val msg: String,
    val order_numbers: List<String>
)