package com.akhnaton.foodvisits.data.statusValue.visits2

import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq

sealed class Visits2Intent {

    object GetVisitPlan : Visits2Intent()

    object GetSalesMan : Visits2Intent()

    data class CopyDayPlan(val copyDayPlanReq: CopyDayPlanReq) : Visits2Intent()

    data class VisitsSelect(val orderType: String, val customerCode: String) :
        Visits2Intent()

    data class CheckIn(val checkInGPSReq: CheckInGPSReq) :
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

    data class PromoterGetItemData(
        val customerCode: String,
        val partySiteId: String
    ) : Visits2Intent()

    data class PromoterSaveStock(
        val promoterSaveStockReq: PromoterSaveStockReq
    ) : Visits2Intent()

    data class RefreshToken(val userId: String, val token: String) : Visits2Intent()

}