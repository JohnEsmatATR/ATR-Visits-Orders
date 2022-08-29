package com.akhnaton.foodvisits.data.statusValue.payment

sealed class PaymentIntent {

    data class Payments(
        val app_version: String,
        val api_token: String,
        val customerPartySiteId: String,
        val orderType: String,
        val customerType: String
    ) : PaymentIntent()
}