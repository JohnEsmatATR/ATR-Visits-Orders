package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.deleteOrder.DeleteOrderRes
import com.akhnaton.foodvisits.data.model.getCustomerData.GetCustomerDataRes
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
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderRes
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsRes
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneRes
import com.akhnaton.foodvisits.data.model.visitesSelect.VisitsSelectRes
import com.akhnaton.foodvisits.shared.ConstantLinks.APP_SETTING
import com.akhnaton.foodvisits.shared.ConstantLinks.DELETE_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.GENERATE_ORDER_NUMBER
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CATEGORIES
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CUSTOMER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_LIST
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_PRODUCT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_SALES_AND_CUSTOMER_TYPES_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_START_ORDER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_VISIT_PLAN
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVED_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_ORDER_PENDING
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_VISIT_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.SEND_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.VISITS_SELECT_ENDPOINT
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IVisits2 {

    @GET(GET_VISIT_PLAN)
    suspend fun getVisitPlan(): GetVisitPlanRes

    @POST(SAVE_VISIT_ENDPOINT)
    suspend fun saveVisitGps(
        @Body saveVisitGpsReq: SaveVisitGpsReq
    ): SaveVisitGpsRes

    @GET(VISITS_SELECT_ENDPOINT)
    suspend fun visitsSelect(
        @Query("order_type") orderType: String,
        @Query("customer_code") customerCode: String
    ): VisitsSelectRes

    @GET(GET_LIST)
    suspend fun getList(
        @Query("page") page: String,
        @Query("per_page") perPage: String,
        @Query("status") status: String,
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String,
        @Query("search") search: String,
        @Query("order_type") orderType: String,
    ): GetListRes

    @DELETE("$DELETE_ORDER/{order_id}")
    suspend fun deleteOrder(
        @Path("order_id") orderId: String
    ): DeleteOrderRes

    @GET("$GET_ITEMS/{order_id}")
    suspend fun getItems(
        @Path("order_id") orderId: String
    ): GetItemsRes

    @GET(GET_SALES_AND_CUSTOMER_TYPES_ENDPOINT)
    suspend fun getSalesAndCustomerTypes(): GetSalesAndCustomerTypesRes

}