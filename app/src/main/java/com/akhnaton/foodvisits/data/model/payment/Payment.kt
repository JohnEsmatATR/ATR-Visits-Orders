package com.akhnaton.foodvisits.data.model.payment

data class Payment(
    val status: Int,
    val data: PaymentData
)

data class PaymentData(
    var customer_payments_term: List<PaymentTermCustomer>,
    var ordersource_id: List<OrderSourceId>,
    var price_list: List<PriceList>,
)

data class PaymentTermCustomer(
    var payment_term_id: Int,
    var payment_term_description: String
)

data class OrderSourceId(
    var id: Int,
    var name: String,
    var flag: Int
)

data class PriceList(
    var price_list_id: Int,
    var price_list_description: String,
)
