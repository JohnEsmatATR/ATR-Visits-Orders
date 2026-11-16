package com.akhnaton.foodvisits.data.statusValue.chartInfo

sealed class ChartInfoIntent {
    object GetChartInfo : ChartInfoIntent()
    data class RefreshToken(val userId: String, val token: String) : ChartInfoIntent()
}