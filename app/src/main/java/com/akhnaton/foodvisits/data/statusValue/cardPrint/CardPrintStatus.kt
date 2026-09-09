package com.akhnaton.foodvisits.data.statusValue.cardPrint

import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintDetailsModel
import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintListModel
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes

sealed class CardPrintStatus {
    object Loading : CardPrintStatus()
    data class GetPrintInvoicesList(val response: CardPrintListModel) : CardPrintStatus()
    data class Error(val message: String?) : CardPrintStatus()
    data class GetPrintInvoiceDetails(val response: CardPrintDetailsModel) : CardPrintStatus()
    data class RefreshToken(val data: RefreshTokenRes) : CardPrintStatus()
}