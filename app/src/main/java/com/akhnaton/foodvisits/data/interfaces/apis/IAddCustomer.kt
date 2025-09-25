package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AreasResponse
import com.akhnaton.foodvisits.data.model.CreateNewCustomer
import com.akhnaton.foodvisits.data.model.GovernoratesResponse
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.createNewCustomer.AreasResponse
import com.akhnaton.foodvisits.data.model.createNewCustomer.Governorate
import com.akhnaton.foodvisits.data.model.createNewCustomer.GovernoratesResponse
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.shared.ConstantLinks
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
    @POST(ConstantLinks.GET_GOVERNMENT)
    suspend fun getGovernment(
        @Field("app_version")version: String,
        @Field("api_token") token: String,
    ): GovernoratesResponse

    @FormUrlEncoded
    @POST(ConstantLinks.GET_GOVERNMENT)
    suspend fun getCity(
        @Field("app_version")version: String,
        @Field("api_token") token: String,
        @Field("governorate_id") governorate_id: String,
    ): AreasResponse

    @Multipart
    @POST(ConstantLinks.ADD_CUSTOMER)
    suspend fun createNewCustomer(
        @Part("app_version") version: RequestBody,
        @Part("api_token") token: RequestBody,
        @Part("customer_sale_type") customerType: RequestBody,
        @Part("customer_order_type") orderType: RequestBody,
        @Part("customer_line_id") lineId: RequestBody,
        @Part("governorate_id") governorate: RequestBody,
        @Part("city_id") city: RequestBody,
        @Part("customer_name") customerName: RequestBody,
        @Part("phone")    phone : RequestBody,
        @Part("phone_2")secondPhone : RequestBody,
        @Part("customer_address") customerAddress: RequestBody,
        @Part("customer_national_id") nationalId: RequestBody,
        @Part("customer_latitude") latitude: RequestBody,
        @Part("customer_longitude") longitude: RequestBody,
        @Part("suggest_address") suggetsAddress : RequestBody,
        @Part id_1: MultipartBody.Part,
        @Part id_2: MultipartBody.Part,
    ): CreateNewCustomer
}