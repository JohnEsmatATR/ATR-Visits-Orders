package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IVisits2
import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.shared.RetrofitClient

class Visits2Repository {

    private val retrofit = RetrofitClient.getInstance(IVisits2::class.java)

    suspend fun getVisitPlan() = retrofit.getVisitPlan()

    suspend fun getSalesMan() = retrofit.getSalesMan()

    suspend fun copyDayPlan(copyDayPlanReq: CopyDayPlanReq) = retrofit.copyDayPlan(copyDayPlanReq)

    suspend fun visitsSelect(orderType: String, customerCode: String) =
        retrofit.visitsSelect(orderType, customerCode)

    suspend fun checkInGPS(checkInGPSReq: CheckInGPSReq) =
        retrofit.checkInGPS(checkInGPSReq)

    suspend fun saveVisitGps(saveVisitGpsReq: SaveVisitGpsReq) =
        retrofit.saveVisitGps(saveVisitGpsReq)

    suspend fun getList(
        page: String,
        perPage: String,
        status: String,
        dateFrom: String,
        dateTo: String,
        search: String,
        orderType: String,
    ) = retrofit.getList(
        page,
        perPage,
        status,
        dateFrom,
        dateTo,
        search,
        orderType,
    )

    suspend fun deleteOrder(
        orderId: String
    ) = retrofit.deleteOrder(
        orderId
    )

    suspend fun getItems(
        orderId: String
    ) = retrofit.getItems(
        orderId
    )

    suspend fun getSalesAndCustomerTypes(
    ) =
        retrofit.getSalesAndCustomerTypes(
        )

    suspend fun promoterGetItemData(
        customerCode: String,
        partySiteId: String
    ) =
        retrofit.promoterGetItemData(
            customerCode,
            partySiteId
        )

    suspend fun promoterSaveStock(
        promoterSaveStockReq: PromoterSaveStockReq
    ) =
        retrofit.promoterSaveStock(
            promoterSaveStockReq
        )
}
