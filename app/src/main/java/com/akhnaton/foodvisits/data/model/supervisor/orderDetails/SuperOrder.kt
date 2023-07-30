package com.akhnaton.foodvisits.data.model.supervisor.orderDetails



data class SuperOrder(
    var message: String,
    var status: String,
    var data: List<SuoerOrderModel>

)

data class SuoerOrderModel(
    var message: String,
    var status: Int,
    val order_nmber: String,
    val customer_name: String,
    val customer_code: String,
    val last_updated_date: String,
    val order_return_number: String,
    val total_order: String,
    var orderDetails: List<SuperOrderDetails>,
    var orderDetailsReturn: List<SuperReturnDetails>

)