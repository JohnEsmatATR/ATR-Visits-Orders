package com.akhnaton.foodvisits.data.model.deleteOrder

data class DeleteOrderRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)