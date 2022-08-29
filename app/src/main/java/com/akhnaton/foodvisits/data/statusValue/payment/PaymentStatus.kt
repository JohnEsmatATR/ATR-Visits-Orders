package com.akhnaton.foodvisits.data.statusValue.payment

import com.akhnaton.foodvisits.data.model.payment.Payment


sealed class PaymentStatus {

    object Idle : PaymentStatus()
    object Loading : PaymentStatus()
    data class GetPayments(val data: Payment) : PaymentStatus()
    data class Error(val error: String?) : PaymentStatus()
}