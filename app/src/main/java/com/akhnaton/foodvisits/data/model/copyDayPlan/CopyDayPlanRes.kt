package com.akhnaton.foodvisits.data.model.copyDayPlan

import com.google.gson.JsonElement

data class CopyDayPlanRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)