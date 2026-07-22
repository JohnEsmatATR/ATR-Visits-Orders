package com.akhnaton.foodvisits.data.model.getSalesMan

import com.google.gson.JsonElement

data class GetSalesManRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)