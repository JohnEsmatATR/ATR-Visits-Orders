package com.akhnaton.foodvisits.data.statusValue.phoneVisits


sealed class PhoneVisitsIntent {

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
        val phoneVisit: Boolean
    ) : PhoneVisitsIntent()


    data class GetAppSetting(
        val app_version: String
    ) : PhoneVisitsIntent()

}