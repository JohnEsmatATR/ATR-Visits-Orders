package com.akhnaton.foodvisits.data.model.copyDayPlan

data class CopyDayPlanReq(
    val date: String,
    val target_date: String,
    val sales_id: Int
)