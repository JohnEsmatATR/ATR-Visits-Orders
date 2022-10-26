package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.VisitsPlaneDataDumy
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.CustomerSite
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit
import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMERS_SITE
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMER_LINE
import com.akhnaton.foodvisits.shared.ConstantLinks.CUSTOMER_TYPE
import com.akhnaton.foodvisits.shared.ConstantLinks.LINES
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_VISIT
import com.akhnaton.foodvisits.shared.ConstantLinks.VISITS_PATH
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IPhoneVisits {

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
        @Field("phone_visit") phoneVisit: Boolean,
    ): SaveVisit

    @FormUrlEncoded
    @POST(ConstantLinks.APP_SETTING)
    suspend fun getAppSetting(
        @Field("app_version") appVersion: String
    ): AppSetting

}