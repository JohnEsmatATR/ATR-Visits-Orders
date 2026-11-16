package com.akhnaton.foodvisits.data.model.chartInfo

import com.google.gson.JsonElement

data class ChartInfoRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: JsonElement?
)

data class ChartInfoData(
    val user_chart_info: List<ChartInfoItem> = emptyList()
)

data class ChartInfoItem(
    val order_type: String,
    val sell_value: Double,
    val plan_value: Double,
    val percentage: Double,
    val month_name: String
)