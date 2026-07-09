package com.akhnaton.foodvisits.data.model.saveOrder

import com.google.gson.JsonElement

data class SaveOrderRes(
    val `data`: JsonElement?,
    val message: List<String>,
    val status: Int,
    val type: String
)