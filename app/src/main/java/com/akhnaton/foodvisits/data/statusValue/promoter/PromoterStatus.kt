package com.akhnaton.foodvisits.data.statusValue.promoter

import com.akhnaton.foodvisits.data.model.promoter.BaseResponse
import com.akhnaton.foodvisits.data.model.promoter.CompetitorListModel
import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.data.model.promoter.PromoterTargetNotes
import com.akhnaton.foodvisits.data.model.promoter.SubmitStock

sealed class PromoterStatus {

    object Idle : PromoterStatus()
    object Loading : PromoterStatus()
    data class SubmitItems(val response: BaseResponse<SubmitStock>) : PromoterStatus()
    data class GetCurrentStockItems(val response: BaseResponse<PromoterItem>) : PromoterStatus()
    data class GetPromotersTargetNotes(val data: List<PromoterTargetNotes>) : PromoterStatus()
    data class SendCompetitors(val response: BaseResponse<String>) : PromoterStatus()
    data class GetCompetitorList(val response: CompetitorListModel) : PromoterStatus()
    data class SendDetails(val response: BaseResponse<SubmitStock>) : PromoterStatus()
    data class UploadImages(val response: BaseResponse<SubmitStock>) : PromoterStatus()
    data class Error(val error: String?) : PromoterStatus()
}