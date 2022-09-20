package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IVisits
import com.akhnaton.foodvisits.shared.RetrofitClient

class VisitsRepository {
    private val retrofit = RetrofitClient.getInstance(IVisits::class.java)

    suspend fun getPlan(version: String, token: String) =
        retrofit.getPlan(version, token)
}