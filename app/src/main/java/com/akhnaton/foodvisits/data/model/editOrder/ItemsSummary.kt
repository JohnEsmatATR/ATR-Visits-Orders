package com.akhnaton.foodvisits.data.model.editOrder

data class ItemsSummary(
    val AVAILABLE_QUANTITY: Int,
    val BACK_ORDER_QUANTITY: Int,
    val DIRECT_QUANTITY: Int,
    val IS_BACK_ORDER: Boolean,
    val MESSAGE: String,
    val PRODUCT_ID: String,
    val QUANTITY: Int,
    val REMAINING_AVAILABLE: Int,
    val REQUESTED_QUANTITY: Int
)