package com.akhnaton.foodvisits.data.model.getItemDetailsReturn

data class Data(
    val DESCRIPTION: String,
    val INVENTORY_ITEM_ID: String,
    val ITEM_CODE: String,
    val PRICES: List<PRICES>,
    val PRICE_LIST_ID: String,
    val QUANTITY: Int,
    val RATE_TYPE: String
)