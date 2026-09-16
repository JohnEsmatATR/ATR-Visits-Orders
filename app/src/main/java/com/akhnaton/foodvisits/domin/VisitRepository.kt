package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IVisitPlan
import com.akhnaton.foodvisits.shared.RetrofitClient

class VisitRepository {

    private val retrofit = RetrofitClient.getInstance(IVisitPlan::class.java)

    suspend fun getMonthlyVisits() = retrofit.getMonthlyVisits()

    suspend fun updateVisitDate(id: String, newDate: String) = retrofit.updateVisitDate(id, newDate)

}