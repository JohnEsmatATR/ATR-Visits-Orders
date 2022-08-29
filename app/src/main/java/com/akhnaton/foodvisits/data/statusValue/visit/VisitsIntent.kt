package com.akhnaton.foodvisits.data.statusValue.visit

sealed class VisitsIntent {

    data class GetPlan(val version: String, val token: String) : VisitsIntent()

    data class GetCustomerType(val version: String, val token: String) : VisitsIntent()

    data class GetLines(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String
    ) : VisitsIntent()

    data class GetCustomerLines(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String,
        val lineId: String
    ) : VisitsIntent()

    data class GetCustomersSite(
        val version: String,
        val token: String,
        val customerType: String,
        val orderType: String,
        val lineId: String,
        val customerCode: String
    ) : VisitsIntent()

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
    ) : VisitsIntent()

}