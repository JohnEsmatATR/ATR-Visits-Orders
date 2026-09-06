package com.akhnaton.foodvisits.data.statusValue.cardPrint

sealed class CardPrintIntent {
    object GetPrintInvoicesList : CardPrintIntent()
}