package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.chartInfo.ChartInfoRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.GET

interface IChartInfo {

    @GET(ConstantLinks.GET_CHART_INFO)
    suspend fun getChartInfo(): ChartInfoRes

}