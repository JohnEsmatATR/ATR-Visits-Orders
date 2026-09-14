package com.akhnaton.foodvisits.data.statusValue.promoter2

import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSRes
import com.akhnaton.foodvisits.data.model.promoter.BaseResponse
import com.akhnaton.foodvisits.data.model.promoter.CompetitorListModel
import com.akhnaton.foodvisits.data.model.promoter.SubmitStock
import com.akhnaton.foodvisits.data.model.promoterGetItemData.PromoterGetItemDataRes
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockRes
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes


sealed class PromoterStatus {

    object Idle : PromoterStatus()
    object Loading : PromoterStatus()

    data class GetCompetitorList(val data: CompetitorListModel) : PromoterStatus()
    data class CheckIn(val data: CheckInGPSRes) : PromoterStatus()
    data class PromoterGetItemData(val data: PromoterGetItemDataRes) : PromoterStatus()
    data class PromoterSaveStock(val data: PromoterSaveStockRes) : PromoterStatus()
    data class SendCompetitors(val response: BaseResponse<String>) : PromoterStatus()
    data class UploadImages(val response: BaseResponse<SubmitStock>) : PromoterStatus()

    data class RefreshToken(val data: RefreshTokenRes) : PromoterStatus()
    data class Error(val error: String?) : PromoterStatus()
}