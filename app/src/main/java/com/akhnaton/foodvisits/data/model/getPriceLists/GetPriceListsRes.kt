package com.akhnaton.foodvisits.data.model.getPriceLists

data class GetPriceListsRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)