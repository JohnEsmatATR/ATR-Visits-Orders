package com.akhnaton.foodvisits.data.model.saveOrder

import com.google.gson.annotations.SerializedName

data class SaveOrderReq(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("party_site_id")
    val partySiteId: String,

    @SerializedName("order_type")
    val orderType: String,

    @SerializedName("device_type")
    val deviceType: String,

    @SerializedName("send")
    val send: String,

    @SerializedName("warehouse_type")
    val warehouseType: String,

//    @SerializedName("login")
//    val login: Int,

    @SerializedName("[payment_id]")
    val paymentId: Int,

    @SerializedName("items")
    val items: Map<String, SaveOrderItemReq>
)

data class SaveOrderItemReq(
    @SerializedName("INVENTORY_ITEM_ID")
    val inventoryItemId: Int,

    @SerializedName("QUANTITY")
    val quantity: Int
)