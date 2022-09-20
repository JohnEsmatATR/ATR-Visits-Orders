package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.CreateNewCustomer
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IAddCustomer {

    @FormUrlEncoded
    @POST(ConstantLinks.CUSTOMER_TYPE)
    suspend fun getCustomerType(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
    ): VisitsCustomerType

    @FormUrlEncoded
    @POST(ConstantLinks.LINES)
    suspend fun getLines(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
    ): Lines


    @FormUrlEncoded
    @POST(ConstantLinks.CUSTOMER_LINE)
    suspend fun getMainLineCustomer(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
        @Field("line_id") lineId: String,
    ): CustomerLines


    @FormUrlEncoded
    @POST(ConstantLinks.ADD_CUSTOMER)
    suspend fun createNewCustomer(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_sale_type") customerType: String,
        @Field("customer_order_type") orderType: String,
        @Field("customer_line_id") lineId: String,
        @Field("customer_code") customerCode: String,
        @Field("customer_name") customerName: String,
        @Field("customer_address") customerAddress: String,
        @Field("customer_national_id") nationalId: String,
        @Field("customer_latitude") latitude: String,
        @Field("customer_longitude") longitude: String,
    ): CreateNewCustomer
}