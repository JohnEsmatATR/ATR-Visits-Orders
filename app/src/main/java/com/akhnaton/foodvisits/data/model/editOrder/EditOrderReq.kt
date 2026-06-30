package com.akhnaton.foodvisits.data.model.editOrder

import com.google.gson.annotations.SerializedName

data class EditOrderReq(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("items")
    val items: Map<String, EditOrderItemReq>
)

data class EditOrderItemReq(
    @SerializedName("INVENTORY_ITEM_ID")
    val inventoryItemId: Int,

    @SerializedName("QUANTITY")
    val quantity: Int
)