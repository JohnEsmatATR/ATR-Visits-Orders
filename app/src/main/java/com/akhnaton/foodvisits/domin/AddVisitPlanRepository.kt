package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IAddVisit
import com.akhnaton.foodvisits.shared.RetrofitClient

class AddVisitPlanRepository {
    private val retrofit = RetrofitClient.getInstance(IAddVisit::class.java)

    suspend fun getSalesAndCustomerTypes() = retrofit.getSalesAndCustomerTypes()
}