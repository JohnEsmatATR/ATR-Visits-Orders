package com.akhnaton.foodvisits.data.statusValue.cardPrint

import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintListModel

sealed class CardPrintStatus {
    object Loading : CardPrintStatus()
    data class GetPrintInvoicesList(val response: CardPrintListModel) : CardPrintStatus()
    data class Error(val message: String?) : CardPrintStatus()
}