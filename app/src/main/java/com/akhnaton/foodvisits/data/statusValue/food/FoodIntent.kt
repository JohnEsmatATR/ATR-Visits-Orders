package com.akhnaton.foodvisits.data.statusValue.food

sealed class FoodIntent {

    data class Food(val version: String, val token: String) : FoodIntent()

    data class OrderDetails(val version: String,val token: String,val orderNumber: String) : FoodIntent()

}