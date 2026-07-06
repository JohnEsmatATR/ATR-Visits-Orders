package com.akhnaton.foodvisits.data.statusValue.order2

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderRes
import com.akhnaton.foodvisits.data.model.getItemDetails.GetItemDetailsRes
import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.getStartOrderData.GetStartOrderDataRes
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import com.akhnaton.foodvisits.data.model.order.SavedOrderResponse
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderRes
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import org.json.JSONObject

sealed class Order2Status {
    object Idle : Order2Status()
    object Loading : Order2Status()
    data class GetStartOrderData(val data: GetStartOrderDataRes) : Order2Status()
    data class SaveOrder(val saveOrderRes: SaveOrderRes) : Order2Status()
    data class GetItems(val data: GetItemsRes) : Order2Status()
    data class EditOrder(val data: EditOrderRes) : Order2Status()
    data class GetItemDetails(val data: GetItemDetailsRes) : Order2Status()
    data class RefreshToken(val data: RefreshTokenRes) : Order2Status()
    data class Error(val error: String?) : Order2Status()
}