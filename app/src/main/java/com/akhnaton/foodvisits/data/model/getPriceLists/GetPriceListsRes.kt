package com.akhnaton.foodvisits.data.model.getPriceLists

import com.google.gson.JsonElement

data class GetPriceListsRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)