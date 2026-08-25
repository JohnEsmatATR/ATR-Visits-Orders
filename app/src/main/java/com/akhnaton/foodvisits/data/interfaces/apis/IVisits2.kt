package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSRes
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanRes
import com.akhnaton.foodvisits.data.model.deleteOrder.DeleteOrderRes
import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
//import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.getList.GetListRes
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.GetSalesAndCustomerTypesRes
import com.akhnaton.foodvisits.data.model.getSalesMan.GetSalesManRes
//import com.akhnaton.foodvisits.data.model.getList.GetListRes
import com.akhnaton.foodvisits.data.model.getVisitPlan.GetVisitPlanRes
import com.akhnaton.foodvisits.data.model.promoterGetItemData.PromoterGetItemDataRes
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockRes
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsRes
import com.akhnaton.foodvisits.data.model.visitesSelect.VisitsSelectRes
import com.akhnaton.foodvisits.shared.ConstantLinks.CHECK_IN_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.COPY_DAY_PLAN
import com.akhnaton.foodvisits.shared.ConstantLinks.DELETE_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_LIST
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_SALES_AND_CUSTOMER_TYPES_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_SALES_MAN
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_VISIT_PLAN
import com.akhnaton.foodvisits.shared.ConstantLinks.PROMOTER_GET_ITEM_DATA
import com.akhnaton.foodvisits.shared.ConstantLinks.PROMOTER_SAVE_STOCK
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_VISIT_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.VISITS_SELECT_ENDPOINT
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

    @GET(GET_SALES_MAN)
    suspend fun getSalesMan(): GetSalesManRes

    @POST(COPY_DAY_PLAN)
    suspend fun copyDayPlan(
        @Body copyDayPlanReq: CopyDayPlanReq
    ): CopyDayPlanRes

    @POST(CHECK_IN_ENDPOINT)
    suspend fun checkInGPS(
        @Body checkInGPSReq: CheckInGPSReq
    ): CheckInGPSRes

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

    @GET(PROMOTER_GET_ITEM_DATA)
    suspend fun promoterGetItemData(
        @Query("customer_code") customerCode: String,
        @Query("party_site_id") partySiteId: String
    ): PromoterGetItemDataRes

    @POST(PROMOTER_SAVE_STOCK)
    suspend fun promoterSaveStock(
        @Body promoterSaveStockReq: PromoterSaveStockReq
    ): PromoterSaveStockRes

}