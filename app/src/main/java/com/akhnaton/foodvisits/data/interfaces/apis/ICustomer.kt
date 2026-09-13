package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.personCoding.AddCustomerModel
import com.akhnaton.foodvisits.data.model.personCoding.AreasModel
import com.akhnaton.foodvisits.data.model.personCoding.GovernoratesModel
import com.akhnaton.foodvisits.data.model.personCoding.LinesModel
import com.akhnaton.foodvisits.data.model.personCoding.MainCustomersLineModel
import com.akhnaton.foodvisits.data.model.personCoding.SalesAndCustomerModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

interface ICustomer {

    @GET(ConstantLinks.GET_SALES_AND_CUSTOMER_TYPES)
    suspend fun getSalesAndCustomerTypes(): SalesAndCustomerModel

    @GET(ConstantLinks.GET_LINES)
    suspend fun getLines(
        @Query("sale_type") saleType: String,
        @Query("customer_type") customerType: String
    ): LinesModel

    @GET(ConstantLinks.GET_MAIN_CUSTOMERS_LINE)
    suspend fun getMainCustomersLine(
        @Query("line_id") lineId: String,
        @Query("order_type") orderType: String,
        @Query("customer_type") customerType: String
    ): MainCustomersLineModel

    @GET(ConstantLinks.GET_USER_AREAS)
    suspend fun getUserAreas(): GovernoratesModel

    @GET(ConstantLinks.GET_USER_AREAS)
    suspend fun getAreasByGovernorate(
        @Query("governorate_id") governorateId: String
    ): AreasModel

    @Multipart
    @POST(ConstantLinks.POST_PERSON_CODING)
    suspend fun addCustomer(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part frontImage: MultipartBody.Part?,
        @Part backImage: MultipartBody.Part?
    ): AddCustomerModel
}