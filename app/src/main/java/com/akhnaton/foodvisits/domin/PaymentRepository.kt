package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IChart
import com.akhnaton.foodvisits.data.interfaces.IPayment
import com.akhnaton.foodvisits.data.interfaces.ITickets
import com.akhnaton.foodvisits.shared.RetrofitClient

class PaymentRepository {

    private val retrofit = RetrofitClient.getInstance(IPayment::class.java)

    suspend fun getPayment(
        appVersion: String,
        apiToken: String,
        customerPartySiteId: String,
        orderType: String,
        customerType: String
    ) =
        retrofit.getCustomerPayment(appVersion, apiToken, customerPartySiteId,orderType,customerType)
}