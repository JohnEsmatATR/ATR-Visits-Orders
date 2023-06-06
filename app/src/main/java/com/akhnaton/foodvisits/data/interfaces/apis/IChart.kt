package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.chart.Chart
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IChart {

    @FormUrlEncoded
    @POST(ConstantLinks.CHART_PATH)
    suspend fun getChartData(
        @Field("app_version") appVersion: String?,
        @Field("api_token") apiToken: String?,
    ): Chart
}