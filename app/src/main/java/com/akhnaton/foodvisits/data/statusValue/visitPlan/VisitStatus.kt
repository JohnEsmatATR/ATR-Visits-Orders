package com.akhnaton.foodvisits.data.statusValue.visitPlan


import com.akhnaton.foodvisits.data.model.visitPlan.VisitListModel
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes

sealed class VisitStatus {
    object Loading : VisitStatus()
    data class GetMonthlyVisits(val response: VisitListModel) : VisitStatus()
    data class RefreshToken(val data: RefreshTokenRes) : VisitStatus()
    data class Error(val message: String?) : VisitStatus()
}