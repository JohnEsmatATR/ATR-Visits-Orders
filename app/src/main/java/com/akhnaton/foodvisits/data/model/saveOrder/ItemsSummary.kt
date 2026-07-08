package com.akhnaton.foodvisits.data.model.saveOrder

data class ItemsSummary(
    val AVAILABLE_QUANTITY: Int,
    val BACK_ORDER_QUANTITY: Int,
    val DIRECT_QUANTITY: Int,
    val PRODUCT_ID: String,
    val REMAINING_AVAILABLE: Int,
    val REQUESTED_QUANTITY: Int
)