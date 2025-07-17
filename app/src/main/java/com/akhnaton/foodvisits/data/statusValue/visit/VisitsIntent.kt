package com.akhnaton.foodvisits.data.statusValue.visit

sealed class VisitsIntent {

    data class GetPlan(val version: String, val token: String) : VisitsIntent()

    data class SaveVisit(
        val version: String,
        val token: String,
        val customerPartySiteId: String,
        val visitType: String,
        val visitTarget: String,
        val visitActualTarget: String,
        val latitude: String,
        val longtitude: String,
        val startLat: String,
        val statLong : String,
        val deviceType: String,
        val zoneFlag: String,
        val checkInDate: String,
        val dateVisit: String,
        val customerType: String,
        val orderType: String,
        val phoneVisit:Boolean
    ) : VisitsIntent()

    object SaveVisitOnline : VisitsIntent()

    data class GetAppSetting(
        val app_version: String
    ) : VisitsIntent()

}