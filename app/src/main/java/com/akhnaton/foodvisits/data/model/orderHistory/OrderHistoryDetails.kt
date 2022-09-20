package com.akhnaton.foodvisits.data.model.orderHistory

data class OrderHistoryDetails(
    var status: Int,
    var data: OrderHistoryDetailsData
)

data class OrderHistoryDetailsData(
    var order_details: List<OrderDetails>,
    var return_details: List<OrderDetails>
)


data class OrderDetails(
    var total: Double,
    var unit_price: Double,
    var ordered_quantity: Int,
    var product_name: String,
    var tax: Double,
)