package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSRes
import com.akhnaton.foodvisits.data.model.promoter.BaseResponse
import com.akhnaton.foodvisits.data.model.promoter.CompetitorListModel
import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.data.model.promoter.SubmitStock
import com.akhnaton.foodvisits.data.model.promoterGetItemData.PromoterGetItemDataRes
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockRes
import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.shared.ConstantLinks.CHECK_IN_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.PROMOTER_GET_ITEM_DATA
import com.akhnaton.foodvisits.shared.ConstantLinks.PROMOTER_SAVE_STOCK
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IPromoter2 {

    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_COMPETITOR_LIST)
    suspend fun getCompetitorList(
        @Field("app_version") appVersion: Double?,
    ): CompetitorListModel

    @Multipart
    @POST(ConstantLinks.PROMOTER_UPLOAD_IMAGE)
    suspend fun uploadImages(
        @Part("app_version") appVersion: RequestBody?,
        @Part("api_token") apiToken: RequestBody?,
        @Part image: Array<MultipartBody.Part?>?,
        @Part("created_by") created_by: RequestBody?,
        @Part("creation_date") creation_date: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("party_site_id") party_site_id: RequestBody?,
        @Part("user_type") user_type: RequestBody?,
        @Part("PromoterImage1") funNum: RequestBody?,
    ): BaseResponse<SubmitStock>


    @Multipart
    @POST(ConstantLinks.PROMOTER_SEND_COMPETITORS)
    suspend fun sendCompetitors(
        @Part("app_version") appVersion: RequestBody?,
        @Part("api_token") apiToken: RequestBody?,
        @Part image: MultipartBody.Part,
        @Part("created_by") created_by: RequestBody?,
        @Part("creation_date") creation_date: RequestBody?,
        @Part("party_site_id") party_site_id: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("product_id") product_id: RequestBody?,
        @Part("price") price: RequestBody?,
        @Part("price_after_disc") price_after_disc: RequestBody?,
        @Part("product_name") product_name: RequestBody?,
        @Part("weight") weight: RequestBody?,
        @Part("discount_rate") discount_rate: RequestBody?,
        @Part("prom_type") prom_type: RequestBody?,
        @Part("prom_date") prom_date: RequestBody?,
        @Part("user_type") user_type: RequestBody?,
        @Part("PromoterCompetitorCompress") PromoterCompetitorCompress: RequestBody?,
        @Part("competitor_id") competitor_id: RequestBody?,
        @Part("type_id") type_id: RequestBody?,
    ): BaseResponse<String>

    @POST(CHECK_IN_ENDPOINT)
    suspend fun checkInGPS(
        @Body checkInGPSReq: CheckInGPSReq
    ): CheckInGPSRes

    @GET(PROMOTER_GET_ITEM_DATA)
    suspend fun promoterGetItemData(
        @Query("customer_code") customerCode: String,
        @Query("party_site_id") partySiteId: String
    ): PromoterGetItemDataRes

    @POST(PROMOTER_SAVE_STOCK)
    suspend fun promoterSaveStock(
        @Body promoterSaveStockReq: PromoterSaveStockReq
    ): PromoterSaveStockRes
}