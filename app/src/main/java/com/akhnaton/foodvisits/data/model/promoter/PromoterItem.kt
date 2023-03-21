package com.akhnaton.foodvisits.data.model.promoter

import com.google.gson.annotations.SerializedName

data class PromoterItem(
    @SerializedName("item_code")
    var itemCode: String? = null,
    @SerializedName("inventory_item_id")
    var inventoryItemId: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("segment4")
    var segment4: String? = null,
    @SerializedName("segment3")
    var segment3: String? = null,
    @SerializedName("cust_price")
    var custPrice: String? = null,
    @SerializedName("percentage_rate")
    var percentageRate: String? = null,
    @SerializedName("quantity")
    var quantity: String? = "0",
    @SerializedName("price")
    var price: String? = null,
    @SerializedName("return_quantity")
    var returnQuantity: String? = null

)