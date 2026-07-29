package com.akhnaton.foodvisits.data.model.saveReturn

import com.google.gson.JsonElement

data class SaveReturnRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)