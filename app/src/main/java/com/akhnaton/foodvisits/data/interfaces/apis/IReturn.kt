package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.getPriceLists.GetPriceListsRes
//import com.akhnaton.foodvisits.data.model.getItems.GetItemsRes
import com.akhnaton.foodvisits.data.model.startReturnData.StartReturnDataRes
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_ITEMS_DETAILS
import com.akhnaton.foodvisits.shared.ConstantLinks.GET_PRICE_LISTS
import com.akhnaton.foodvisits.shared.ConstantLinks.START_RETURN_DATA
import retrofit2.http.GET
import retrofit2.http.Query

interface IReturn {

    @GET(GET_PRICE_LISTS)
    suspend fun getPriceLists(
        @Query("party_site_id") partySiteId: String,
        @Query("order_type") orderType: String
    ): GetPriceListsRes

    @GET(START_RETURN_DATA)
    suspend fun startReturnData(
        @Query("order_id") orderId: String,
        @Query("price_list_id") priceListId: String
    ): StartReturnDataRes

    @GET(GET_ITEMS_DETAILS)
    suspend fun getItemDetails(
        @Query("item_id") itemId: String,
        @Query("price_list") priceList: String,
        @Query("store_id") storeId: String,
    ): com.akhnaton.foodvisits.data.model.getItemDetailsReturn.GetItemDetailsRes

}