package com.akhnaton.foodvisits.data.model.order

data class GenerateOrderNumber(
    var status: Int,
    var data: GenerateOrderNumberData
)

data class GenerateOrderNumberData(
    var order_number: String
)
