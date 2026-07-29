package com.akhnaton.foodvisits.data.model.saveReturn

import com.google.gson.annotations.SerializedName

data class SaveReturnReq(
    @SerializedName("return_id")
    val returnId: String,

    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("order_type")
    val orderType: String,

    @SerializedName("comment")
    val comment: String,

    @SerializedName("party_site_id")
    val partySiteId: String,

    @SerializedName("send")
    val send: String,

    @SerializedName("PRICE_LIST_ID")
    val priceListId: Int,

    @SerializedName("items")
    val items: Map<String, SaveReturnItemReq>
)

data class SaveReturnItemReq(
    @SerializedName("inventory_item_id")
    val inventoryItemId: Int,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: Double,

    @SerializedName("customer_price")
    val customerPrice: Double
)