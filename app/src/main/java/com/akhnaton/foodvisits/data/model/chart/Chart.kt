package com.akhnaton.foodvisits.data.model.chart

data class Chart(
    var message: String,
    var status: Int,
    val data: ChartData
)