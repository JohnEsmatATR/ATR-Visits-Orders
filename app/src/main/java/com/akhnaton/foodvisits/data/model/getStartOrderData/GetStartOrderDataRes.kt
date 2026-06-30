package com.akhnaton.foodvisits.data.model.getStartOrderData

import com.google.gson.JsonElement

data class GetStartOrderDataRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)