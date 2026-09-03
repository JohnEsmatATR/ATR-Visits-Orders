package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.CompetitorListResponse
import com.akhnaton.foodvisits.data.model.SendCompetitorResponse
import com.akhnaton.foodvisits.shared.ConstantLinks
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ICompetitor {

    @Multipart
    @POST(ConstantLinks.PROMOTER_SEND_COMPETITORS)
    suspend fun sendCompetitor(
        @Part("customer_code") customerCode: RequestBody,
        @Part("party_site_id") partySiteId: RequestBody,
        @Part("product_name") productName: RequestBody,
        @Part("price_after_disc") priceAfterDisc: RequestBody,
        @Part("price") price: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("discount_rate") discountRate: RequestBody,
        @Part("competitor_id") competitorId: RequestBody,
        @Part("type_id") typeId: RequestBody,
        @Part("prom_type") promType: RequestBody,
        @Part("prom_date") promDate: RequestBody,
        @Part image: MultipartBody.Part,
    ): SendCompetitorResponse

    @FormUrlEncoded
    @POST(value = ConstantLinks.PROMOTER_COMPETITOR_LIST)
    suspend fun getCompetitorList(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
    ): CompetitorListResponse
}