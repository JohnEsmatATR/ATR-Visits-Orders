package com.akhnaton.foodvisits.data.model.saveVisitGps

import com.google.gson.JsonElement

data class SaveVisitGpsRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)