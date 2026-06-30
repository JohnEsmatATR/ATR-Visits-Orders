package com.akhnaton.foodvisits.data.model.getVisitPlan

import com.google.gson.JsonElement


data class GetVisitPlanRes(
    val `data`: JsonElement?,
    val message: String,
    val status: Int,
    val type: String
)