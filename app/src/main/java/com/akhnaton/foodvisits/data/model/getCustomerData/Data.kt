package com.akhnaton.foodvisits.data.model.getCustomerData

data class Data(
    val customer_address: List<CustomerAddres>,
    val payment_terms: List<PaymentTerm>
)