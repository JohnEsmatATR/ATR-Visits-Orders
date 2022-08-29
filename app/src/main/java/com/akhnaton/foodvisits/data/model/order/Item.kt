package com.akhnaton.foodvisits.data.model.order


data class ItemsList(
    var app_version: String,
    var api_token: String,
    var order_type: String,
    var order_number: String,
    var customer_type: String,
    var customer_party_site_id: String,
    var pay_term_id:String,
    var turn_over: Boolean,
    var items: MutableList<Item>,

    )

data class Item(
    var bonus: String,
    var item_id: Int,
    var item_quantity: String
)

data class CardItem(
    var item_id: Int,
    var item_code: Int,
    var item_description: String,
    var item_price: Double,
    var item_tax: Float,
    var quantity : String,
    var total :Float,
    var bonus: String
)