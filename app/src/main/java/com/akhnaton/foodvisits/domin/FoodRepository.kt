package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IFood
import com.akhnaton.foodvisits.shared.RetrofitClient

class FoodRepository {

    private val retrofit = RetrofitClient.getInstance(IFood::class.java)

    suspend fun getFoodOrders(version: String, token: String) =
        retrofit.getFood(version, token)

    suspend fun getOrderDetails(version: String, token: String, orderNumber: String) =
        retrofit.orderDetails(version, token, orderNumber)
}