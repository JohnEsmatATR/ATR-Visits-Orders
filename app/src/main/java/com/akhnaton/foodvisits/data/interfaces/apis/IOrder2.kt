package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.AppSetting
import com.akhnaton.foodvisits.data.model.getCustomerData.GetCustomerDataRes
import com.akhnaton.foodvisits.data.model.getStartOrderData.GetStartOrderDataRes
import com.akhnaton.foodvisits.data.model.order.Categories
import com.akhnaton.foodvisits.data.model.order.GenerateOrderNumber
import com.akhnaton.foodvisits.data.model.order.Product
import com.akhnaton.foodvisits.data.model.order.SaveOrderResponse
import com.akhnaton.foodvisits.data.model.order.SavedOrderResponse
import com.akhnaton.foodvisits.shared.ConstantLinks.APP_SETTING
import com.akhnaton.foodvisits.shared.ConstantLinks.GENERATE_ORDER_NUMBER
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CATEGORIES
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_CUSTOMER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_PRODUCT
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_START_ORDER_DATA_ENDPOINT
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVED_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVE_ORDER_PENDING
import com.akhnaton.foodvisits.shared.ConstantLinks.SEND_ORDER
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IOrder2 {

    @GET(GET_START_ORDER_DATA_ENDPOINT)
    suspend fun getStartOrderData(
        @Query("party_site_id") partySiteId: String,
        @Query("order_type") orderType: String,
        @Query("customer_code") customerCode: String,
    ): GetStartOrderDataRes

}