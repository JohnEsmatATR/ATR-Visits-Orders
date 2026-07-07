package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderReq
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderRes
import com.akhnaton.foodvisits.data.model.getCustomerData.GetCustomerDataRes
import com.akhnaton.foodvisits.data.model.getItemDetails.GetItemDetailsRes
import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
//import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.getStartOrderData.GetStartOrderDataRes
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import com.akhnaton.foodvisits.data.model.order.SavedOrderResponse
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderRes
import com.akhnaton.foodvisits.shared.ConstantLinks.APP_SETTING
import com.akhnaton.foodvisits.shared.ConstantLinks.EDIT_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.GENERATE_ORDER_NUMBER
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CATEGORIES
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CUSTOMER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS_DETAILS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_PRODUCT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_START_ORDER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVED_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVED_ORDER_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_ORDER_PENDING
import com.akhnaton.foodvisits.shared.ConstantLinks.SEND_ORDER
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IOrder2 {

    @GET(GET_START_ORDER_DATA_ENDPOINT)
    suspend fun getStartOrderData(
        @Query("party_site_id") partySiteId: String,
        @Query("order_type") orderType: String,
        @Query("customer_code") customerCode: String,
    ): GetStartOrderDataRes

    @POST(SAVED_ORDER_ENDPOINT)
    suspend fun saveOrder(
        @Body saveOrderReq: SaveOrderReq,
    ): SaveOrderRes

    @GET("$GET_ITEMS/{order_id}")
    suspend fun getItems(
        @Path("order_id") orderId: String
    ): GetItemsRes

    @POST(EDIT_ORDER)
    suspend fun editOrder(
        @Body editOrderReq: EditOrderReq,
    ): EditOrderRes

    @GET(GET_ITEMS_DETAILS)
    suspend fun getItemDetails(
        @Query("item_id") itemId: String,
        @Query("price_list") priceList: String,
        @Query("store_id") storeId: String,
    ): GetItemDetailsRes

}