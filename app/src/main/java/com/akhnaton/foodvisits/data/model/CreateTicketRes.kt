package com.akhnaton.foodvisits.data.model

import com.google.gson.JsonElement

data class CreateTicketRes(
    val `data`: JsonElement?,
    val message: JsonElement?,
    val status: Int,
    val type: String
)