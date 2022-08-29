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
        val orderType: String
    ) : OrderIntent()

    data class GetProducts(
        val app_version: String,
        val api_token: String,
        val orderType: String,
        val sub_category: String,
        val customer_type: Int,
        val customer_party_site_id: Int,
    ) : OrderIntent()

    data class GetOrderLimit(
        val app_version: String
    ) : OrderIntent()

    data class SendOrder(
        val request: JsonElement
    ) : OrderIntent()
}