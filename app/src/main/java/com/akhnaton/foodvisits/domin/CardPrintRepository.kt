package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ICardPrint
import com.akhnaton.foodvisits.shared.RetrofitClient

class CardPrintRepository {

    private val retrofit = RetrofitClient.getInstance(ICardPrint::class.java)

    suspend fun getPrintInvoicesList() = retrofit.getPrintInvoicesList()

    suspend fun getPrintInvoiceDetails(orderSalesNumber: String) =
        retrofit.getPrintInvoiceDetails(orderSalesNumber)

}
