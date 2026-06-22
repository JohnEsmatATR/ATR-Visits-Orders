package com.akhnaton.foodvisits.data.statusValue.order2

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.getStartOrderData.GetStartOrderDataRes
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import com.akhnaton.foodvisits.data.model.order.SavedOrderResponse
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import org.json.JSONObject

sealed class Order2Status {
    object Idle : Order2Status()
    object Loading : Order2Status()
    data class GetStartOrderData(val data: GetStartOrderDataRes) : Order2Status()
    data class RefreshToken(val data: RefreshTokenRes) : Order2Status()
    data class Error(val error: String?) : Order2Status()
}