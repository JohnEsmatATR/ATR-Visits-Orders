package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IChart
import com.akhnaton.foodvisits.shared.RetrofitClient

class MainFragmentRepository {
    private val retrofit = RetrofitClient.getInstance(IChart::class.java)

    suspend fun getChart(appVersion: String, apiToken: String) =
        retrofit.getChartData(appVersion, apiToken)
}