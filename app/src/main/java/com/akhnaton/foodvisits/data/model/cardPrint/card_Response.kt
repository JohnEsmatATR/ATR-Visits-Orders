package com.akhnaton.foodvisits.data.model.cardPrint

data class CardPrintListModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: List<CardPrintItem>
)

data class CardPrintItem(
    val customer_name: String,
    val order_sales_number: String,
    val order_type: String
)