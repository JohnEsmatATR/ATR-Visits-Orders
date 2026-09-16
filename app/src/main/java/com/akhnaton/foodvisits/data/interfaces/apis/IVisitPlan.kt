package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.visitPlan.VisitListModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.GET

interface IVisitPlan {
    @GET(ConstantLinks.GET_MONTHLY_VISITS)
    suspend fun getMonthlyVisits(): VisitListModel
}