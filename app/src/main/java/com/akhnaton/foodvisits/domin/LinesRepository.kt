package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IAddVisit
import com.akhnaton.foodvisits.shared.RetrofitClient

class LinesRepository {

    private val retrofit = RetrofitClient.getInstance(IAddVisit::class.java)

    suspend fun getLines(saleType: String) = retrofit.getLines(saleType)

}