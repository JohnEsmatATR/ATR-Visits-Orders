package com.akhnaton.foodvisits.data.model.getItemDetailsReturn

data class PRICES(
    val CUST_PRICE: String,
    val OPERAND: String,
    var isSelected: Boolean = false
)