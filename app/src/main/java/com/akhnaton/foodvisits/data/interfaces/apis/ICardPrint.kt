package com.akhnaton.foodvisits.data.interfaces.apis

import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintListModel
import com.akhnaton.foodvisits.shared.ConstantLinks
import retrofit2.http.GET
interface ICardPrint {

    @GET(ConstantLinks.GET_CARD)
    suspend fun getPrintInvoicesList(): CardPrintListModel

}