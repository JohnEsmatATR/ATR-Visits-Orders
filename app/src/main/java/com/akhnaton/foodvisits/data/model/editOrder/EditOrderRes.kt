package com.akhnaton.foodvisits.data.model.editOrder

import com.google.gson.JsonElement

data class EditOrderRes(
    val `data`: JsonElement?,
    val message: List<String>,
    val status: Int,
    val type: String
)