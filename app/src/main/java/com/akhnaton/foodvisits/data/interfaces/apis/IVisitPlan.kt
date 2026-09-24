package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.pendingVisits.ApproveVisitsReq
import com.akhnaton.foodvisits.data.model.pendingVisits.ApproveVisitsRes
import com.akhnaton.foodvisits.data.model.pendingVisits.PendingVisitsRes
import com.akhnaton.foodvisits.data.model.visitPlan.CopyPlanReq
import com.akhnaton.foodvisits.data.model.visitPlan.CopyPlanRes
import com.akhnaton.foodvisits.data.model.visitPlan.DeleteVisitReq
import com.akhnaton.foodvisits.data.model.visitPlan.DeleteVisitRes
import com.akhnaton.foodvisits.data.model.visitPlan.UpdateVisitDateRes
import com.akhnaton.foodvisits.data.model.visitPlan.VisitListModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface IVisitPlan {
    @GET(ConstantLinks.GET_MONTHLY_VISITS)
    suspend fun getMonthlyVisits(): VisitListModel

    @GET(ConstantLinks.GET_PENDING_VISITS_ENDPOINT)
    suspend fun getPendingVisitsForApproval(
        @retrofit2.http.Query("page") page: Int,
        @retrofit2.http.Query("page_size") pageSize: Int
    ): PendingVisitsRes

    @POST(ConstantLinks.APPROVE_VISITS_ENDPOINT)
    suspend fun approveVisits(@Body request: ApproveVisitsReq): ApproveVisitsRes

    @FormUrlEncoded
    @POST(ConstantLinks.UPDATE_VISIT)
    suspend fun updateVisitDate(
        @Field("id") id: String,
        @Field("new_date") newDate: String
    ): UpdateVisitDateRes

    @HTTP(method = "DELETE", path = ConstantLinks.DELETE_VISIT, hasBody = true)
    suspend fun deleteVisitPlan(@Body request: DeleteVisitReq): DeleteVisitRes

    @POST(ConstantLinks.COPY_PLAN)
    suspend fun copyPlan(@Body request: CopyPlanReq): CopyPlanRes
}