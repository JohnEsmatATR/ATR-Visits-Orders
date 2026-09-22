package com.akhnaton.foodvisits.data.model.visitPlan

import com.google.gson.JsonElement

data class SaveCustomerRequest(
    val customer_code: String,
    val party_site_id: String,
    val customer_type: String,
    val customer_branch: String
)

data class SaveSetupPlanRequest(
    val order_type: String,
    val line_id: String,
    val customers: List<SaveCustomerRequest>,
    val dates: List<String>
)

data class SaveSetupPlanData(
    val success: Boolean,
    val rows_inserted: Int
)

data class SaveSetupPlanRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)