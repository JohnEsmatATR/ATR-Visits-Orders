package com.akhnaton.foodvisits.data.statusValue.returns

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.deleteOrder.DeleteOrderRes
import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
//import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.getList.GetListRes
import com.akhnaton.foodvisits.data.model.getPriceLists.GetPriceListsRes
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.GetSalesAndCustomerTypesRes
//import com.akhnaton.foodvisits.data.model.getList.GetListRes
import com.akhnaton.foodvisits.data.model.getStartOrderData.GetStartOrderDataRes
import com.akhnaton.foodvisits.data.model.getVisitPlan.GetVisitPlanRes
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import com.akhnaton.foodvisits.data.model.order.SavedOrderResponse
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsRes
import com.akhnaton.foodvisits.data.model.startReturnData.StartReturnDataRes
import com.akhnaton.foodvisits.data.model.visitesSelect.VisitsSelectRes
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import org.json.JSONObject

sealed class ReturnsStatus {
    object Idle : ReturnsStatus()
    object Loading : ReturnsStatus()
    data class GetPriceLists(val data: GetPriceListsRes) : ReturnsStatus()
    data class StartReturnData(val data: StartReturnDataRes) : ReturnsStatus()
    data class RefreshToken(val data: RefreshTokenRes) : ReturnsStatus()
    data class Error(val error: String?) : ReturnsStatus()
}