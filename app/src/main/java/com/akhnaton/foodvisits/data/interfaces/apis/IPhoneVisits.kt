package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.VisitsPlaneDataDumy
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.checkInPhone.CheckInPhoneReq
import com.akhnaton.foodvisits.data.model.checkInPhone.CheckInPhoneRes
import com.akhnaton.foodvisits.data.model.customers.GetCustomersRes
import com.akhnaton.foodvisits.data.model.getCustomerData.GetCustomerDataRes
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.GetSalesAndCustomerTypesRes
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneRes
import com.akhnaton.foodvisits.data.model.visitesSelect.VisitsSelectRes
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.CustomerSite
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit
import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.shared.ConstantLinks.CHECK_IN_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMERS_SITE
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMER_LINE
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMER_TYPE
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CUSTOMERS_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CUSTOMER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_SALES_AND_CUSTOMER_TYPES_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.LINES
import com.akhnaton.foodvisits.shared.ConstantLinks.REFRESH_TOKEN
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_VISIT
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_VISIT_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.VISITS_PATH
import com.akhnaton.foodvisits.shared.ConstantLinks.VISITS_SELECT_ENDPOINT
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IPhoneVisits {

    @GET(GET_SALES_AND_CUSTOMER_TYPES_ENDPOINT)
    suspend fun getSalesAndCustomerTypes(): GetSalesAndCustomerTypesRes

    @GET(GET_CUSTOMERS_ENDPOINT)
    suspend fun getCustomers(
        @Query("sale_type") saleType: String
    ): GetCustomersRes

    @GET(GET_CUSTOMER_DATA_ENDPOINT)
    suspend fun getCustomerData(
        @Query("sale_type") saleType: String,
        @Query("customer_code") customerCode: String,
        @Query("line") line: String
    ): GetCustomerDataRes

    @GET(VISITS_SELECT_ENDPOINT)
    suspend fun visitsSelect(
        @Query("order_type") orderType: String,
        @Query("customer_code") customerCode: String
    ): VisitsSelectRes

    @POST(CHECK_IN_ENDPOINT)
    suspend fun checkInPhone(
        @Body checkInReq: CheckInPhoneReq
    ): CheckInPhoneRes

    @POST(SAVE_VISIT_ENDPOINT)
    suspend fun saveVisitPhone(
        @Body saveVisitPhoneReq: SaveVisitPhoneReq
    ): SaveVisitPhoneRes

    @FormUrlEncoded
    @POST(REFRESH_TOKEN)
    suspend fun refreshToken(
        @Field("USER_ID") userId: String,
        @Field("TOKEN") token: String,
    ): RefreshTokenRes

    //----------------------------------------------------------------------------------------------

    @FormUrlEncoded
    @POST(VISITS_PATH)
    suspend fun getPlan(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
    ): VisitsPlaneDataDumy


    @FormUrlEncoded
    @POST(CUSTOMER_TYPE)
    suspend fun getCustomerType(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
    ): VisitsCustomerType


    @FormUrlEncoded
    @POST(LINES)
    suspend fun getLines(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
    ): Lines

    @FormUrlEncoded
    @POST(CUSTOMER_LINE)
    suspend fun getMainLineCustomer(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
        @Field("line_id") lineId: String,
    ): CustomerLines


    @FormUrlEncoded
    @POST(CUSTOMERS_SITE)
    suspend fun getCustomersSite(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
        @Field("line_id") lineId: String,
        @Field("customer_code") customerCode: String
    ): CustomerSite

    @FormUrlEncoded
    @POST(SAVE_VISIT)
    suspend fun saveVisits(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
        @Field("customer_party_site_id") customerPartySiteId: String,
        @Field("visit_type") visitType: String,
        @Field("visit_target") visitarget: String,
        @Field("visit_actual_target") visitActualTarget: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("device_type") deviceType: String,
        @Field("zone_flag") zoneFlag: String,
        @Field("check_in_date") checkInDate: String,
        @Field("date_visit") dateVisit: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
        @Field("phone_visit") phoneVisit: Boolean,
    ): SaveVisit

    @FormUrlEncoded
    @POST(ConstantLinks.APP_SETTING)
    suspend fun getAppSetting(
        @Field("app_version") appVersion: String
    ): AppSetting

}