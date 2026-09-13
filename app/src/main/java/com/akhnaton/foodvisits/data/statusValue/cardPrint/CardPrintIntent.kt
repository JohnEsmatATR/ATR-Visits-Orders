package com.akhnaton.foodvisits.data.statusValue.cardPrint

sealed class CardPrintIntent {
    object GetPrintInvoicesList : CardPrintIntent()
    data class GetPrintInvoiceDetails(val orderSalesNumber: String) : CardPrintIntent()
    data class RefreshToken(val userId: String, val token: String) : CardPrintIntent()
}