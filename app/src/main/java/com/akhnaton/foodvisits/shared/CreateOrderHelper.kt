package com.akhnaton.foodvisits.shared

import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.data.model.order.OrderItem
import com.akhnaton.foodvisits.data.model.order.ItemsList
import com.akhnaton.foodvisits.data.model.order.ReturnItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement

class CreateOrderHelper {
    private val versionName = BuildConfig.VERSION_NAME

    fun addOrder(
        orderType: String,
        orderNumber: String,
        customerType: String,
        customerPartySiteId: String,
        payTermId: String,
        turnOver: Boolean,
        mOrderItemCardAdded: MutableList<OrderItem>,
        mReturnItemCardAdded: MutableList<ReturnItem>,
        orderSource: Int,
        priceListIdPosition: Int,
        priceListDescriptionPosition: String,
    ): JsonElement {

        val itemsList = ItemsList(
            versionName, SharedPreferencesHelper.getInstance().getUserToken(),
            orderType, orderNumber, customerType, customerPartySiteId, payTermId, turnOver,
            mOrderItemCardAdded,mReturnItemCardAdded,orderSource, priceListIdPosition, priceListDescriptionPosition)

        val gson: Gson = GsonBuilder().create()
        return gson.toJsonTree(itemsList)
    }
}