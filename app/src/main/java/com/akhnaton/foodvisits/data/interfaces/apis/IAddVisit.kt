package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.visitPlan.AddVisitPlan
import com.akhnaton.foodvisits.data.model.visitPlan.GetLinesRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.GET
import retrofit2.http.Query

interface IAddVisit {

    @GET(ConstantLinks.ADD_VISIT_PLAN)
    suspend fun getSalesAndCustomerTypes(): AddVisitPlan

    @GET(ConstantLinks.GET_LINE_VISIT)
    suspend fun getLines(@Query("sale_type") saleType: String): GetLinesRes
}