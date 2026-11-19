package com.akhnaton.foodvisits.data.model.deleteOrder

import com.google.gson.JsonElement

data class DeleteOrderRes(
    val `data`: JsonElement,
    val message: String,
    val status: Int,
    val type: String
)