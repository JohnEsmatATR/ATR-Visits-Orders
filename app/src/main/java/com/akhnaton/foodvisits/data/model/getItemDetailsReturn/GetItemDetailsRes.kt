package com.akhnaton.foodvisits.data.model.getItemDetailsReturn

import com.google.gson.JsonElement

data class GetItemDetailsRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)