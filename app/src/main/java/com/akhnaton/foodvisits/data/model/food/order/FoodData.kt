package com.akhnaton.foodvisits.data.model.food.order

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FoodData : Serializable {

    @SerializedName("customer_name")
    var customerName: String = ""

    @SerializedName("order_sales_number")
    var orderSalesNumber: String = ""

    @SerializedName("order_type")
    var orderType: String = ""
}