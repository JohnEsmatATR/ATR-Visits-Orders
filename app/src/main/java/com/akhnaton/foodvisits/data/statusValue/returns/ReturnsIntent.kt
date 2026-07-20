package com.akhnaton.foodvisits.data.statusValue.returns

import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.google.gson.JsonElement

sealed class ReturnsIntent {

    data class GetPriceLists(
        val partySiteId: String,
        val orderType: String
    ) : ReturnsIntent()

    data class StartReturnData(
        val orderId: String,
        val priceListId: String
    ) : ReturnsIntent()

    data class GetItemDetails(
        val itemId: String,
        val priceList: String,
        val storeId: String,
    ) : ReturnsIntent()

    data class RefreshToken(val userId: String, val token: String) : ReturnsIntent()

}