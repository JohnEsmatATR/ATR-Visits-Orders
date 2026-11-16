package com.akhnaton.foodvisits.data.statusValue.chartInfo

import com.akhnaton.foodvisits.data.model.chartInfo.ChartInfoRes
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes

sealed class ChartInfoStatus {
    object Loading : ChartInfoStatus()
    data class GetChartInfo(val response: ChartInfoRes) : ChartInfoStatus()
    data class RefreshToken(val data: RefreshTokenRes) : ChartInfoStatus()
    data class Error(val message: String?) : ChartInfoStatus()
}