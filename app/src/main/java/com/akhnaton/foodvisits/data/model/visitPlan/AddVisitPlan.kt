package com.akhnaton.foodvisits.data.model.visitPlan

import com.google.gson.JsonElement

data class AddVisitPlan(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)

data class AddVisitPlanData(
    val sales_types: List<String>
)