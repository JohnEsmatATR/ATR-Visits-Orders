package com.akhnaton.foodvisits.data.statusValue.phoneVisits

import com.akhnaton.foodvisits.data.model.checkInPhone.CheckInPhoneReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq


sealed class PhoneVisitsIntent {

    object GetSalesAndCustomerTypes : PhoneVisitsIntent()
    data class GetCustomers(val saleType: String) : PhoneVisitsIntent()
    data class GetCustomerData(val saleType: String, val customerCode: String, val line: String) :
        PhoneVisitsIntent()
    data class VisitsSelect(val orderType: String, val customerCode: String) :
        PhoneVisitsIntent()

    data class CheckIn(val checkInPhoneReq: CheckInPhoneReq) :
        PhoneVisitsIntent()
    data class SaveVisitPhone(val saveVisitPhoneReq: SaveVisitPhoneReq) :
        PhoneVisitsIntent()

    data class RefreshToken(val userId: String, val token: String) : PhoneVisitsIntent()

    //----------------------------------------------------------------------------------------------

    data class GetPlan(val version: String, val token: String) : PhoneVisitsIntent()

    data class GetCustomerType(val version: String, val token: String) : PhoneVisitsIntent()

    data class GetLines(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String
    ) : PhoneVisitsIntent()

    data class GetCustomerLines(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String,
        val lineId: String
    ) : PhoneVisitsIntent()

    data class GetCustomersSite(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String,
        val lineId: String,
        val customerCode: String
    ) : PhoneVisitsIntent()

    data class SaveVisit(
        val version: String,
        val token: String,
        val customerPartySiteId: String,
        val visitType: String,
        val visitTarget: String,
        val visitActualTarget: String,
        val latitude: String,
        val longtitude: String,
        val deviceType: String,
        val zoneFlag: String,
        val checkInDate: String,
        val dateVisit: String,
        val customerType: String,
        val orderType: String,
        val phoneVisit: Boolean
    ) : PhoneVisitsIntent()


    data class GetAppSetting(
        val app_version: String
    ) : PhoneVisitsIntent()

}