package com.akhnaton.foodvisits.data.statusValue.visits2

import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.google.gson.JsonElement

sealed class Visits2Intent {

    object GetVisitPlan : Visits2Intent()

    data class VisitsSelect(val orderType: String, val customerCode: String) :
        Visits2Intent()

    data class SaveVisitGps(val saveVisitGpsReq: SaveVisitGpsReq) :
        Visits2Intent()

    data class DeleteOrder(val orderId: String) :
        Visits2Intent()

    data class GetItems(val orderId: String) :
        Visits2Intent()

    data class GetList(
        val page: String,
        val perPage: String,
        val status: String,
        val dateFrom: String,
        val dateTo: String,
        val search: String,
        val orderType: String,
    ) : Visits2Intent()

    object GetSalesAndCustomerTypes : Visits2Intent()

    data class RefreshToken(val userId: String, val token: String) : Visits2Intent()

}