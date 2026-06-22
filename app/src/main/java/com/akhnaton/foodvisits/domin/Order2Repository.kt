package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IOrder
import com.akhnaton.foodvisits.data.interfaces.apis.IOrder2
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.JsonElement

class Order2Repository {

    private val retrofit = RetrofitClient.getInstance(IOrder2::class.java)

    suspend fun getStartOrderData(
        partySiteId: String,
        orderType: String,
        customerCode: String
    ) =
        retrofit.getStartOrderData(
            partySiteId,
            orderType,
            customerCode
        )
}
