package com.akhnaton.foodvisits.data.model.promoterSaveStock

data class Item(
    val item_id: Int,
    val price: Double = 0.0,
    val quantity: Int = 0,
    val return_quantity: Int = 0
)