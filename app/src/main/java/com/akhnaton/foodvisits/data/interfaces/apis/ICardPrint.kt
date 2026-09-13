package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintDetailsModel
import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintListModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.GET
import retrofit2.http.Query

interface ICardPrint {

    @GET(ConstantLinks.GET_CARD)
    suspend fun getPrintInvoicesList(): CardPrintListModel

    @GET(ConstantLinks.GET_CARDDETAILS)
    suspend fun getPrintInvoiceDetails(
        @Query("order_sales_number") orderSalesNumber: String
    ): CardPrintDetailsModel

}