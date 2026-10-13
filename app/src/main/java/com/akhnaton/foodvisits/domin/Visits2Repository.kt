package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IVisits2
import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.getVisitPlan.GetVisitPlanRes
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.visitesSelect.VisitsSelectRes
import com.akhnaton.foodvisits.data.offline.OfflineDatabase
import com.akhnaton.foodvisits.data.offline.dao.ApiCacheDao
import com.akhnaton.foodvisits.data.offline.entity.ApiCacheEntity
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.JsonParser
import java.io.IOException

class Visits2Repository(
    private val apiCacheDao: ApiCacheDao? = null
) {

    private val retrofit = RetrofitClient.getInstance(IVisits2::class.java)

    private fun key(suffix: String) = "${OfflineDatabase.currentUserId()}|$suffix"

    suspend fun getVisitPlan(): GetVisitPlanRes {
        val cacheKey = key("today")
        try {
            val response = retrofit.getVisitPlan()
            val responseData = response.data
            if (response.status == 200 && responseData != null) {
                apiCacheDao?.insert(
                    ApiCacheEntity(
                        cacheKey = cacheKey,
                        data = responseData.toString(),
                        message = response.message,
                        type = response.type
                    )
                )
            }
            return response
        } catch (e: IOException) {
            val cached = apiCacheDao?.get(cacheKey) ?: throw e
            return GetVisitPlanRes(
                data = JsonParser.parseString(cached.data),
                message = cached.message,
                status = 200,
                type = cached.type
            )
        }
    }

    suspend fun getSalesMan() = retrofit.getSalesMan()

    suspend fun copyDayPlan(copyDayPlanReq: CopyDayPlanReq) = retrofit.copyDayPlan(copyDayPlanReq)

    suspend fun visitsSelect(orderType: String, customerCode: String): VisitsSelectRes {
        val cacheKey = key("$orderType-$customerCode")
        try {
            val response = retrofit.visitsSelect(orderType, customerCode)
            val responseData = response.data
            if (response.status == 200 && responseData != null) {
                apiCacheDao?.insert(
                    ApiCacheEntity(
                        cacheKey = cacheKey,
                        data = responseData.toString(),
                        message = response.message,
                        type = response.type
                    )
                )
            }
            return response
        } catch (e: IOException) {
            val cached = apiCacheDao?.get(cacheKey) ?: throw e
            return VisitsSelectRes(
                data = JsonParser.parseString(cached.data),
                message = cached.message,
                status = 200,
                type = cached.type
            )
        }
    }

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