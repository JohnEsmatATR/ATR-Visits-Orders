package com.akhnaton.foodvisits.data.statusValue.order2

import com.akhnaton.foodvisits.data.model.editOrder.EditOrderReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.google.gson.JsonElement

sealed class Order2Intent {

    data class GetStartOrderData(
        val partySiteId: String,
        val orderType: String,
        val customerCode: String,
    ) : Order2Intent()

    data class SaveOrder(
        val saveOrderReq: SaveOrderReq
    ) : Order2Intent()

    data class GetItems(val orderId: String) :
        Order2Intent()

    data class EditOrder(
        val editOrderReq: EditOrderReq
    ) : Order2Intent()

    data class RefreshToken(val userId: String, val token: String) : Order2Intent()

}