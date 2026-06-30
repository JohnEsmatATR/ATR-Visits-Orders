package com.akhnaton.foodvisits.data.model.editOrder

data class EditOrderRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)