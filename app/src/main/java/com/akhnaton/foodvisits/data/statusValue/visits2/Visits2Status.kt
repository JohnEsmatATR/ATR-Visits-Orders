package com.akhnaton.foodvisits.data.statusValue.visits2

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.deleteOrder.DeleteOrderRes
import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
//import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.getList.GetListRes
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
import com.akhnaton.foodvisits.data.model.visitesSelect.VisitsSelectRes
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import org.json.JSONObject

sealed class Visits2Status {
    object Idle : Visits2Status()
    object Loading : Visits2Status()
    data class GetVisitPlan(val data: GetVisitPlanRes) : Visits2Status()

    data class GetList(val data: GetListRes) : Visits2Status()
    data class DeleteOrder(val data: DeleteOrderRes) : Visits2Status()
    data class GetItems(val data: GetItemsRes) : Visits2Status()
    data class SaveVisitGps(val data: SaveVisitGpsRes) : Visits2Status()
    data class VisitsSelect(val data: VisitsSelectRes) : Visits2Status()
    data class GetSalesAndCustomerTypes(val data: GetSalesAndCustomerTypesRes) : Visits2Status()
    data class RefreshToken(val data: RefreshTokenRes) : Visits2Status()
    data class Error(val error: String?) : Visits2Status()
}