package com.akhnaton.foodvisits.data.statusValue.food

import com.akhnaton.foodvisits.data.model.food.order.Food
import com.akhnaton.foodvisits.data.model.food.details.FoodOrderDetails

sealed class FoodStatus {
    object Idle : FoodStatus()
    object Loading : FoodStatus()
    data class FoodOrders(val data: Food) : FoodStatus()
    data class OrderDetails(val data: FoodOrderDetails) : FoodStatus()
    data class DeliveryPrint(val data: FoodOrderDetails) : FoodStatus()
    data class Error(val error: String?) : FoodStatus()
}