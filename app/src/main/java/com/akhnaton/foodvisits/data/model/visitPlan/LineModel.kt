package com.akhnaton.foodvisits.data.model.visitPlan

import com.google.gson.JsonElement

data class LineItem(
    val STORE_CODE: String,
    val LINE_CODE: String,
    val LINE_NAME: String,
    val JOB_FLAG: String
)

data class GetLinesData(
    val lines: List<LineItem>
)

data class GetLinesRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)