package com.akhnaton.foodvisits.data.model.getItemDetails

data class Data(
    val DESCRIPTION: String,
    val INVENTORY_ITEM_ID: String,
    val ITEM_CODE: String,
    val PRICE_LIST_ID: String,
    val QUANTITY: String,
    val RATE_TYPE: String,
    val PRICES: List<Prices>,
)