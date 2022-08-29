package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.food.order.Food
import com.akhnaton.foodvisits.data.model.food.details.FoodOrderDetails
import com.akhnaton.foodvisits.shared.ConstantLinks.FOOD_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.FOOD_ORDER_Details
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IFood {

    @FormUrlEncoded
    @POST(FOOD_ORDER)
    suspend fun getFood(
        @Field("app_version") version: String,
        @Field("api_token") token: String
    ): Food

    @FormUrlEncoded
    @POST(FOOD_ORDER_Details)
    suspend fun orderDetails(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("order_sales_number") orderNumber: String
    ): FoodOrderDetails
}