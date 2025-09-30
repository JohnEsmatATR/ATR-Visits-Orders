package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit
import com.akhnaton.foodvisits.shared.ConstantLinks
import com.akhnaton.foodvisits.shared.ConstantLinks.VISIT_PLAN
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IVisits {

    @FormUrlEncoded
    @POST(VISIT_PLAN)
    suspend fun getPlan(
        @Field("app_version") version: String,
        @Field("api_token") token: String,
    ): VisitsPlan


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
    @POST(ConstantLinks.SAVE_VISIT)
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
        @Field("start_lat") startLat : String,
        @Field("start_long")startLong : String,
        @Field("zone_flag") zoneFlag: String,
        @Field("check_in_date") checkInDate: String,
        @Field("date_visit") dateVisit: String,
        @Field("customer_type") customerType: String,
        @Field("order_type") orderType: String,
        ): SaveVisit

    @FormUrlEncoded
    @POST(ConstantLinks.APP_SETTING)
    suspend fun getAppSetting(
        @Field("app_version") appVersion: String
    ): AppSetting

}