package com.akhnaton.foodvisits.data.model.getCustomerData

import com.google.gson.JsonElement

data class GetCustomerDataRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)