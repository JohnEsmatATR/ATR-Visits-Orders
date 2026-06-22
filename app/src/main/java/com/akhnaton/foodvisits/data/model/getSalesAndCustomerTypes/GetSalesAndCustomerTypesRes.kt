package com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes

import com.google.gson.JsonElement

data class GetSalesAndCustomerTypesRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)