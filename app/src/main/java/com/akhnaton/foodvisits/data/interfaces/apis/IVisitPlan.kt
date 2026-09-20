package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.deleteVisitPlan.DeleteVisitPlanRes
import com.akhnaton.foodvisits.data.model.deleteVisitPlan.DeleteVisitPlanReq
import com.akhnaton.foodvisits.data.model.visitPlan.UpdateVisitDateRes
import com.akhnaton.foodvisits.data.model.visitPlan.VisitListModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface IVisitPlan {
    @GET(ConstantLinks.GET_MONTHLY_VISITS)
    suspend fun getMonthlyVisits(): VisitListModel

    @FormUrlEncoded
    @POST(ConstantLinks.UPDATE_VISIT)
    suspend fun updateVisitDate(
        @Field("id") id: String,
        @Field("new_date") newDate: String
    ): UpdateVisitDateRes

    @DELETE(ConstantLinks.DELETE_VISIT)
    suspend fun deleteVisitDate(
        @Body deleteVisitPlanReq: DeleteVisitPlanReq
    ): DeleteVisitPlanRes
}