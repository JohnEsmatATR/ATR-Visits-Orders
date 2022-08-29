package com.akhnaton.foodvisits.data.model.food.details

import com.google.gson.annotations.SerializedName


data class FoodDetailsData(
    var invoice_info: FoodInvoiceInfo,
    var invoice_details: List<FoodInvoiceDetails>
)

data class FoodInvoiceInfo(
    val customer_name: String,
    val customer_address: String,
    val invoice_total_value: Float
)

class FoodInvoiceDetails {

    @SerializedName("tax_value")
    var taxValue: String? = ""

    @SerializedName("unit_selling_price")
    var listPrice: String? = ""

    @SerializedName("order_quantity")
    var orderQuantity: String? = ""

    @SerializedName("customer_name")
    var customerName: String? = ""

    @SerializedName("item_desc")
    var itemDesc: String? = ""

}
