package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.editOrder.EditOrderReq
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderRes
import com.akhnaton.foodvisits.data.model.getItemDetails.GetItemDetailsRes
import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.getPriceLists.GetPriceListsRes
//import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderRes
import com.akhnaton.foodvisits.shared.ConstantLinks.EDIT_ORDER
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS_DETAILS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_PRICE_LISTS
import com.akhnaton.foodvisits.shared.ConstantLinks.SAVED_ORDER_ENDPOINT
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IReturn {

    @GET(GET_PRICE_LISTS)
    suspend fun getPriceLists(
        @Query("party_site_id") partySiteId: String,
        @Query("order_type") orderType: String
    ): GetPriceListsRes

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