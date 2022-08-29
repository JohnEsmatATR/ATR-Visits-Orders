package com.akhnaton.foodvisits.data.model.payment

data class Payment(
    val status: Int,
    val data: PaymentData
)

data class PaymentData(
    var customer_payments_term: List<PaymentTermCustomer>
)

data class PaymentTermCustomer(
    var payment_term_id: Int,
    var payment_term_description: String
)
