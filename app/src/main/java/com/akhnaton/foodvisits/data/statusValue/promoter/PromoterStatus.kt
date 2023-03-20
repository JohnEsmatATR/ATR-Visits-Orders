package com.akhnaton.foodvisits.data.statusValue.promoter

import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.data.model.promoter.PromoterTargetNotes
import com.akhnaton.foodvisits.data.model.promoter.SubmitStock


sealed class PromoterStatus {

    object Idle : PromoterStatus()
    object Loading : PromoterStatus()
    data class GetItems(val data: List<PromoterItem?>) : PromoterStatus()
    data class SubmitItems(val data: List<SubmitStock?>) : PromoterStatus()
    data class GetCurrentStockItems(val data: List<PromoterItem?>) : PromoterStatus()
    data class GetPromotersTargetNotes(val data: List<PromoterTargetNotes?>) : PromoterStatus()
    data class SendDetails(val data: List<SubmitStock?>) : PromoterStatus()
    data class UploadImages(val data: List<SubmitStock?>) : PromoterStatus()
    data class Error(val error: String?) : PromoterStatus()
}