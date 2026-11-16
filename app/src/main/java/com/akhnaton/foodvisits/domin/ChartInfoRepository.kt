package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IChartInfo
import com.akhnaton.foodvisits.shared.RetrofitClient

class ChartInfoRepository {

    private val retrofit = RetrofitClient.getInstance(IChartInfo::class.java)

    suspend fun getChartInfo() = retrofit.getChartInfo()

}