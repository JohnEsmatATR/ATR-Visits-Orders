package com.akhnaton.foodvisits.data.statusValue.order

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import org.json.JSONObject

sealed class OrderStatus {
    object Idle : OrderStatus()
    object Loading : OrderStatus()
    data class GetOrderNumber(val data: GenerateOrderNumber) : OrderStatus()
    data class GetCategories(val data: Categories) : OrderStatus()
    data class GetProducts(val data: Product) : OrderStatus()
    data class GetOrderLimit(val data: AppSetting) : OrderStatus()
    data class SendOrder(val data: SaveOrderResponse) : OrderStatus()
    data class Error(val error: String?) : OrderStatus()
}