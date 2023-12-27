package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.promoter.BaseResponse
import com.akhnaton.foodvisits.data.model.promoter.CompetitorListModel
import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.data.model.promoter.PromoterTargetNotes
import com.akhnaton.foodvisits.data.model.promoter.SubmitStock
import com.akhnaton.foodvisits.shared.ConstantLinks
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface IPromoter {


    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_GET_ITEMS)
    suspend fun getCurrentStockItems(
        @Field("app_version") appVersion: Double?,
        @Field("api_token") apiToken: String?,
        @Field("created_by") createdBy: Int?,
        @Field("party_site_id") partySiteId: Int?,
        @Field("customer_code") customerCode: Int?,
        @Field("creation_date") creationDate: String?,
        @Field("Items") items: Int?,
    ): BaseResponse<PromoterItem>


    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_SUBMIT_ITEMS)
    suspend fun submitStock(
        @Field("app_version") appVersion: Double?,
        @Field("api_token") apiToken: String?,
        @Field("created_by") createdBy: Int?,
        @Field("party_site_id") partySite: Int?,
        @Field("creation_date") date: String?,
        @Field("item_id") itemId: Int?,
        @Field("return_quantity") returnQuantity: Int?,
        @Field("quantity") quantity: Int?,
        @Field("price") price: Double?,
        @Field("customer_code") customerCode: Int?,
        @Field("user_type") userType: String?
    ): BaseResponse<SubmitStock>



//    @FormUrlEncoded
//    @POST(ConstantLinks.PROMOTER_ITEMS)
//    suspend fun getPromotersTargetNotes(
//        @Field("employee_id") employeeId: String?,
//        @Field("user_type") userType: String?,
//        @Field("PromoterTarget") funNum: String?
//    ): List<PromoterTargetNotes?>


    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_INSERT_DETAILS)
    suspend fun sendDetails(
        @Field("app_version") appVersion: Double?,
        @Field("api_token") apiToken: String?,
        @Field("created_by") employeeId: Int?,
        @Field("creation_date") date: String?,
        @Field("party_site_id") partySite: Int?,
        @Field("customer_code") code: Int?,
        @Field("customer_avg") customerAvg: Int?,
        @Field("customer_calls") customerCall: Int?,
        @Field("customer_positive_calls") customerPositiveCall: Int?,
        @Field("customer_purchased") customerPurchase: Int?,
        @Field("user_type") userType: String?,
        @Field("StockDayDetails") funNum: Int?
    ): BaseResponse<SubmitStock>

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
        @Part image: Array<MultipartBody.Part?>,
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
        @Part("competitor_name") competitor_name: RequestBody?,
        @Part("type_name") type_name: RequestBody?,
    ): BaseResponse<String>


}