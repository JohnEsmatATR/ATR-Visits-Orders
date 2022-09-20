package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistory
import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistoryDetails
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IOrderHistory {

    @FormUrlEncoded
    @POST(ConstantLinks.ORDER_HISTORY)
    suspend fun orderHistory(
        @Field("api_token") token: String,
        @Field("app_version") version: String,
        @Field("date_to") to: String,
        @Field("date_from") from: String
    ): OrderHistory

    @FormUrlEncoded
    @POST(ConstantLinks.ORDER_HISTORY_DETAILS)
    suspend fun orderHistoryDetails(
        @Field("api_token") token: String,
        @Field("app_version") version: String,
        @Field("orig_sys_document_ref") to: String,
    ): OrderHistoryDetails

}