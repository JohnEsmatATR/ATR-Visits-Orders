package com.akhnaton.foodvisits.data.model.checkInGPS

import com.google.gson.JsonElement

data class CheckInGPSRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)