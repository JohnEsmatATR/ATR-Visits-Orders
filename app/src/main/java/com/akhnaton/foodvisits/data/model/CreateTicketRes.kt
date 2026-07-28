package com.akhnaton.foodvisits.data.model

data class CreateTicketRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)