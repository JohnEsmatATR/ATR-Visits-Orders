package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ISupervisor
import com.akhnaton.foodvisits.shared.RetrofitClient

class OrderDetailsSuperRepository {
    private val retrofit = RetrofitClient.getInstance(ISupervisor::class.java)

    suspend fun orderDetailsSuper(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
        superId: String,
        order_total_price: String,
        customer_id:String,
    ) = retrofit.getOrderDetailsSuper(app_version, api_token, orderNumber, superId, order_total_price, customer_id)

}