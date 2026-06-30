package com.akhnaton.foodvisits.data.model.getVisitPlan

data class Data(
    val customer_visit_plan: List<CustomerVisitPlan>,
    val date: String,
    val day: String
)