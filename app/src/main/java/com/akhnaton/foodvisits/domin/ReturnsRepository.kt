package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IOrder
import com.akhnaton.foodvisits.data.interfaces.apis.IOrder2
import com.akhnaton.foodvisits.data.interfaces.apis.IReturn
import com.akhnaton.foodvisits.data.interfaces.apis.IVisits2
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveReturn.SaveReturnReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.shared.RetrofitClient
import com.google.gson.JsonElement

class ReturnsRepository {

    private val retrofit = RetrofitClient.getInstance(IReturn::class.java)

    suspend fun getPriceLists(
        partySiteId: String,
        orderType: String
    ) = retrofit.getPriceLists(
        partySiteId,
        orderType,
    )

    suspend fun startReturnData(
        orderId: String,
        priceListId: String
    ) = retrofit.startReturnData(
        orderId,
        priceListId,
    )

    suspend fun saveReturn(
        saveReturnReq: SaveReturnReq
    ) = retrofit.saveReturn(
        saveReturnReq
    )

    suspend fun getItemDetails(
        itemId: String,
        priceList: String,
        storeId: String,
    ) = retrofit.getItemDetails(
        itemId,
        priceList,
        storeId,
    )

}
