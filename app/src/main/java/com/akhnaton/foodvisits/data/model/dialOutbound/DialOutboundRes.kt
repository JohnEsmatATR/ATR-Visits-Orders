package com.akhnaton.foodvisits.data.model.dialOutbound

import com.google.gson.JsonElement

data class DialOutboundRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)