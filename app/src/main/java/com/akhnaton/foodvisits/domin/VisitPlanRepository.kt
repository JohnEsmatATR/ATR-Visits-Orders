package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IVisitPlan
import com.akhnaton.foodvisits.shared.RetrofitClient

class VisitPlanRepository {

    private val retrofit = RetrofitClient.getInstance(IVisitPlan::class.java)

    suspend fun getMonthlyVisits() = retrofit.getMonthlyVisits()

    suspend fun updateVisitDate(id: String, newDate: String) = retrofit.updateVisitDate(id, newDate)

    suspend fun deleteVisitPlan(ids: List<Int>) =
        retrofit.deleteVisitPlan(com.akhnaton.foodvisits.data.model.visitPlan.DeleteVisitReq(ids))

    suspend fun copyPlan(sourceDate: String, targetDate: String) =
        retrofit.copyPlan(com.akhnaton.foodvisits.data.model.visitPlan.CopyPlanReq(sourceDate, targetDate))
}