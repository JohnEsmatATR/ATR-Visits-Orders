package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.shared.ConstantLinks.VISIT_PLAN
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IVisits {

    @FormUrlEncoded
    @POST(VISIT_PLAN)
    suspend fun getPlan(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
    ): VisitsPlan
}