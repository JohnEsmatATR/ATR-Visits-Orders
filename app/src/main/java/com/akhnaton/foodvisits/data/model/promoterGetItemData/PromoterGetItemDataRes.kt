package com.akhnaton.foodvisits.data.model.promoterGetItemData

data class PromoterGetItemDataRes(
    val `data`: List<Data>,
    val message: String,
    val status: Int,
    val type: String
)