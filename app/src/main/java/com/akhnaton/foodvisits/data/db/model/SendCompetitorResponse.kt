package com.akhnaton.foodvisits.data.model

data class SendCompetitorResponse(
    val status: Int,
    val message: String,
    val type: String,
    val data: List<Any>?
)
