package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.payment.Payment
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMER_PAYMENT
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IPayment {

    @FormUrlEncoded
    @POST(CUSTOMER_PAYMENT)
    suspend fun getCustomerPayment(
        @Field("app_version") appVersion: String,
        @Field("api_token") apiToken: String,
        @Field("customer_party_site_id") customerPartySiteId: String,
        @Field("order_type") orderType: String,
        @Field("customer_type") customerType: String
    ): Payment
}