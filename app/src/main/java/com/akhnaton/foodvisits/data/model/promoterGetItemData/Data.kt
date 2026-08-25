package com.akhnaton.foodvisits.data.model.promoterGetItemData

data class Data(
    var cust_price: String,
    val description: String,
    val inventory_item_id: String,
    val item_code: String,
    var percentage_rate: String,
    var quantity: Int,
    val segment3: String,
    val segment4: String,
    var writtenQuantity: String = "0",
    var writtenPrice: String = "0.0",
    var writtenReturned: String = "0",
    var hasChanges: Boolean = false

)