package com.akhnaton.foodvisits.data.model.saveOrder

data class SaveOrderRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)