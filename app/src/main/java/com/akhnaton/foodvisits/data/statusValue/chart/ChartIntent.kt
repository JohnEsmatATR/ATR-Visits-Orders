package com.akhnaton.foodvisits.data.statusValue.chart

sealed class ChartIntent {

    data class Chart(
        val app_version: String, val api_token: String
    ) : ChartIntent()
}