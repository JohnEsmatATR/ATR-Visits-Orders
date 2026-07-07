package com.akhnaton.foodvisits.data.model.getStartOrderData

data class Data(
    val invoice_number: String,
    val products: List<Product>,
    val select_lists: List<SelectLists>,
    val price_list_id: String,
    val store_id: String,
)