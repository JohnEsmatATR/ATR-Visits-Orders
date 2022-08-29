package com.akhnaton.foodvisits.data.model.visits

data class Lines(
    val status: Int,
    val data:LinesData
)

data class LinesData(
    val user_lines: List<LinesUsers>
)

data class LinesUsers(
    val line_id: String,
    val line_name: String,
    val customer_type: String,
    val order_type: String
)
