package com.akhnaton.foodvisits.data.model.saveVisitPhone

data class Data(
    val duration_minutes: Int,
    val is_suspended: Boolean,
    val message: String,
    val success: Boolean,
    val visit_id: Int
)