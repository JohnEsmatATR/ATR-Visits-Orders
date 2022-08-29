package com.akhnaton.foodvisits.data.statusValue.chart

import com.akhnaton.foodvisits.data.model.chart.Chart


sealed class ChartStatus {

    object Idle : ChartStatus()
    object Loading : ChartStatus()
    data class ChartData(val data: Chart) : ChartStatus()
    data class Error(val error: String?) : ChartStatus()
}