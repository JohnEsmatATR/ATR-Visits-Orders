package com.akhnaton.foodvisits.data.interfaces

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
    @POST(ConstantLinks.PROMOTER_ITEMS)
    suspend fun getItems(
        @Field("Items") items: String?,
        @Field("created_by") createdBy: String?,
        @Field("customer_code") customerCode: String?,
        @Field("party_site_id") partySiteId: String?,
    ): List<PromoterItem>

    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_ITEMS)
    suspend fun submitStock(
        @Field("created_by") createdBy: String?,
        @Field("party_site_id") partySite: String?,
        @Field("customer_code") customerCode: String?,
        @Field("creation_date") date: String?,
        @Field("item_id") itemId: String?,
        @Field("quantity") quantity: String?,
        @Field("return_quantity") returnQuantity: String?,
        @Field("price") price: String?,
        @Field("StockItem") funNum: String?
    ): List<SubmitStock>


    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_ITEMS)
    suspend fun getCurrentStockItems(
        @Field("created_by") employeeId: String?,
        @Field("party_site_id") partySite: String?,
        @Field("customer_code") code: String?,
        @Field("creation_date") creationDate: String?,
        @Field("user_type") userType: String?,
        @Field("CurrentDayStock") funNum: String?,
    ): List<PromoterItem>


    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_ITEMS)
    suspend fun getPromotersTargetNotes(
        @Field("employee_id") employeeId: String?,
        @Field("user_type") userType: String?,
        @Field("PromoterTarget") funNum: String?
    ): List<PromoterTargetNotes?>


    @FormUrlEncoded
    @POST(ConstantLinks.PROMOTER_ITEMS)
    suspend fun sendDetails(
        @Field("created_by") employeeId: String?,
        @Field("creation_date") date: String?,
        @Field("party_site_id") partySite: String?,
        @Field("customer_code") code: String?,
        @Field("customer_avg") customerAvg: String?,
        @Field("customer_calls") customerCall: String?,
        @Field("customer_positive_calls") customerPositiveCall: String?,
        @Field("customer_purchased") customerPurchase: String?,
        @Field("user_type") userType: String?,
        @Field("StockDayDetails") funNum: String?
    ): List<SubmitStock>

    @Multipart
    @POST(ConstantLinks.PROMOTER_ITEMS)
    suspend fun uploadImages(
        @Part image: Array<MultipartBody.Part?>?,
        @Part("created_by") created_by: RequestBody?,
        @Part("creation_date") creation_date: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("party_site_id") party_site_id: RequestBody?,
        @Part("user_type") user_type: RequestBody?,
        @Part("PromoterImage") funNum: RequestBody?
    ): List<SubmitStock>


}