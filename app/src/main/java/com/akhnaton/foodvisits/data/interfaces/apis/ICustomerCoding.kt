package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.BaseModel
import com.akhnaton.foodvisits.data.model.coding.CodingAreaModel
import com.akhnaton.foodvisits.data.model.coding.CodingCategoryModel
import com.akhnaton.foodvisits.data.model.coding.CodingLineModel
import com.akhnaton.foodvisits.data.model.coding.CodingTypeModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ICustomerCoding {

    @FormUrlEncoded
    @POST(ConstantLinks.ADD_FOOD_CUST_API)
    suspend fun getTypes(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("action") action: String? = "get_customer_type",
        @Field("user_id") userId: String?,
    ): BaseModel<List<CodingTypeModel>>


    @FormUrlEncoded
    @POST(ConstantLinks.ADD_FOOD_CUST_API)
    suspend fun getLines(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("action") action: String? = "get_lines_names",
        @Field("user_id") userId: String?,
        @Field("cust_type") custType: String?,
    ): BaseModel<List<CodingLineModel>>


    @FormUrlEncoded
    @POST(ConstantLinks.ADD_FOOD_CUST_API)
    suspend fun getCategories(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("action") action: String? = "get_customers",
        @Field("user_id") userId: String?,
        @Field("cust_type") custType: String?,
        @Field("line_id") lineId: String?,
    ): BaseModel<List<CodingCategoryModel>>


    @FormUrlEncoded
    @POST(ConstantLinks.ADD_FOOD_CUST_API)
    suspend fun getAreas(
        @Field("app_version") app_version: String?,
        @Field("api_token") api_token: String?,
        @Field("action") action: String? = "get_areas",
        @Field("user_id") userId: String?,
    ): BaseModel<List<CodingAreaModel>>

    @Multipart
    @POST(ConstantLinks.ADD_FOOD_CUST_API)
    suspend fun getSendData(
        @Part("app_version") app_version: RequestBody,
        @Part("api_token") api_token: RequestBody,
        @Part("action") action: RequestBody = "save_customer".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("user_id") userId: RequestBody,
        @Part("cust_type") custType: RequestBody,
        @Part("line_id") lineId: RequestBody,
        @Part("cust_code_id") categoryId: RequestBody,
        @Part("area") area: RequestBody,
        @Part("customer_name") customerName: RequestBody,
        @Part("customer_address") customerAddress: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part("mobile_number") mobileNumber: RequestBody,
        @Part("customer_national_id") customerNationalId: RequestBody,
        @Part("name_in_national_id") nameInNationalId: RequestBody,
        @Part("address_in_national_id") addressInNationalId: RequestBody,
//        @Part id_1: MultipartBody.Part,
//        @Part id_2: MultipartBody.Part,
        @Part("long") long: RequestBody,
        @Part("lat") lat: RequestBody,
    ): BaseModel<List<String>>

}