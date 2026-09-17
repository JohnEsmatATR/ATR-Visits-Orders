package com.akhnaton.foodvisits.data.model.visitPlan

data class AddVisitPlan(
    val status: Int,
    val message: String,
    val type: String,
    val data: AddVisitPlanData
)

data class AddVisitPlanData(
    val sales_types: List<String>
)