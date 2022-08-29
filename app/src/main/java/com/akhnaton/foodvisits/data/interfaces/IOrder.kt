package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import com.akhnaton.foodvisits.shared.ConstantLinks.APP_SETTING
import com.akhnaton.foodvisits.shared.ConstantLinks.GENERATE_ORDER_NUMBER
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CATEGORIES
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_PRODUCT
import com.akhnaton.foodvisits.shared.ConstantLinks.SEND_ORDER
import com.google.gson.JsonElement
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IOrder {

    @FormUrlEncoded
    @POST(GENERATE_ORDER_NUMBER)
    suspend fun generateOrderNumber(
        @Field("app_version") appVersion: String,
        @Field("api_token") apiToken: String,
        @Field("customer_party_site_id") customerPartySiteId: String,
        @Field("order_type") orderType: String,
        @Field("customer_type") customerType: String,
        @Field("payment_term_id") paymentTermId: String,
        @Field("visit_id") visitId: String,
    ): GenerateOrderNumber

    @FormUrlEncoded
    @POST(GET_CATEGORIES)
    suspend fun getCategories(
        @Field("app_version") appVersion: String,
        @Field("api_token") apiToken: String,
        @Field("order_type") orderType: String,
    ): Categories


    @FormUrlEncoded
    @POST(GET_PRODUCT)
    suspend fun getProduct(
        @Field("app_version") appVersion: String,
        @Field("api_token") apiToken: String,
        @Field("order_type") orderType: String,
        @Field("sub_category") sub_category: String,
        @Field("customer_type") customerType: Int,
        @Field("customer_party_site_id") customerPartySiteId: Int,
    ): Product

    @FormUrlEncoded
    @POST(APP_SETTING)
    suspend fun getOrderLimit(
        @Field("app_version") appVersion: String
    ): AppSetting

    @POST(SEND_ORDER)
    suspend fun sendOrder(
        @Body request: JsonElement,
    ): SaveOrderResponse
}