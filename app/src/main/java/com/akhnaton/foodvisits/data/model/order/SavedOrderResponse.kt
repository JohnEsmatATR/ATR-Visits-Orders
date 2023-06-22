package com.akhnaton.foodvisits.data.model.order

data class SavedOrderResponse(
    val status: Int,
    val message: String,
    val data: SavedOrder
)

data class SavedOrder(
    val order_items_general_data: ItemsGeneralData,
    val order_return_items_general_data: ItemsGeneralData,
    val order_items: List<OrderItems>,
    val return_items: List<OrderItems>,
)

data class OrderItems(
    val item_id: Int,
    val item_name: String,
    val item_code: Int,
    val item_price: Double,
    val tax: Double,
    val quantity: Int,
    val items_price: Double,
    val price_list_id: Int,
)

data class ItemsGeneralData(
    val order_item_count: Int,
    val total_items_price: Double,
)