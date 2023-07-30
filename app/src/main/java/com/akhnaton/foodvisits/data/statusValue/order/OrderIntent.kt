package com.akhnaton.foodvisits.data.statusValue.order

import com.google.gson.JsonElement

sealed class OrderIntent {

    data class GenerateOrderNumber(
        val app_version: String,
        val api_token: String,
        val customerPartySiteId: String,
        val orderType: String,
        val customerType: String,
        val paymentTermId: String,
        val visitId: String
    ) : OrderIntent()

    data class GetCategories(
        val app_version: String,
        val api_token: String,
        val orderType: String,
        val customer_type: String,
    ) : OrderIntent()

    data class GetProducts(
        val app_version: String,
        val api_token: String,
        val orderType: String,
        val sub_category: String,
        val customer_type: Int,
        val customer_code: Int,
        val customer_party_site_id: Int,
        val item_price_list: String,
    ) : OrderIntent()

    data class GetOrderLimit(
        val app_version: String
    ) : OrderIntent()

    data class SendOrder(
        val request: JsonElement
    ) : OrderIntent()

    data class SaveOrderPending(
        val request: JsonElement
    ) : OrderIntent()

    data class SavedOrder(
        val appVersion: String,
        val apiToken: String,
        val orderNumber: String,
    ) : OrderIntent()
}