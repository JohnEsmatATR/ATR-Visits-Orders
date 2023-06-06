package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IOrderHistory
import com.akhnaton.foodvisits.shared.RetrofitClient

class OrderHistoryRepository {

    private val retrofit = RetrofitClient.getInstance(IOrderHistory::class.java)

    suspend fun getOrdersHistory(token: String, version: String, to: String, from: String) =
        retrofit.orderHistory(token, version, to, from)


    suspend fun getOrderHistoryDetails(token: String, version: String, orderNumber: String) =
        retrofit.orderHistoryDetails(token,version,orderNumber)
}