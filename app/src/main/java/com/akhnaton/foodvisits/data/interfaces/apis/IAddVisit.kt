package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.visitPlan.AddVisitPlan
import com.akhnaton.foodvisits.data.model.visitPlan.GetLinesRes
import com.akhnaton.foodvisits.data.model.visitPlan.GetVisitCustomersRes
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRequest
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IAddVisit {

    @GET(ConstantLinks.ADD_VISIT_PLAN)
    suspend fun getSalesAndCustomerTypes(): AddVisitPlan

    @GET(ConstantLinks.GET_LINE_VISIT)
    suspend fun getLines(@Query("sale_type") saleType: String): GetLinesRes
    @GET(ConstantLinks.GET_VISIT_CUSTOMERS)
    suspend fun getVisitCustomers(@Query("line_id") lineId: String): GetVisitCustomersRes
    @POST(ConstantLinks.SAVE_VISIT_PLAN)
    suspend fun saveSetupPlan(@Body request: SaveSetupPlanRequest): SaveSetupPlanRes
}