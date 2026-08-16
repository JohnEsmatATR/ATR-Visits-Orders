package com.akhnaton.foodvisits.data.model.checkInPhone

import com.google.gson.JsonElement

data class CheckInPhoneRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)