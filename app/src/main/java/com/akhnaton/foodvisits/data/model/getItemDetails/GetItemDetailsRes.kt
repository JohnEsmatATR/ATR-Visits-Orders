package com.akhnaton.foodvisits.data.model.getItemDetails

data class GetItemDetailsRes(
    val `data`: List<Data>,
    val message: String,
    val status: Int,
    val type: String
)