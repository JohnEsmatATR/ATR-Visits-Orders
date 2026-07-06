package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IOrder
import com.akhnaton.foodvisits.data.interfaces.apis.IOrder2
import com.akhnaton.foodvisits.data.interfaces.apis.IVisits2
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.JsonElement

class Visits2Repository {

    private val retrofit = RetrofitClient.getInstance(IVisits2::class.java)

    suspend fun getVisitPlan() = retrofit.getVisitPlan()

    suspend fun visitsSelect(orderType: String, customerCode: String) =
        retrofit.visitsSelect(orderType, customerCode)

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

    suspend fun getSalesAndCustomerTypes() =
        retrofit.getSalesAndCustomerTypes()
}
