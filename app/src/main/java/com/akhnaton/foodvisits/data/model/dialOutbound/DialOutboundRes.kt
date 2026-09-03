package com.akhnaton.foodvisits.data.model.dialOutbound

data class DialOutboundRes(
    val `data`: Data,
    val message: String,
    val status: Int,
    val type: String
)