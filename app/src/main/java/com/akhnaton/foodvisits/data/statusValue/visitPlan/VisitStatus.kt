package com.akhnaton.foodvisits.data.statusValue.visitPlan


import com.akhnaton.foodvisits.data.model.visitPlan.VisitListModel
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.model.visitPlan.DeleteVisitRes
import com.akhnaton.foodvisits.data.model.visitPlan.UpdateVisitDateRes

sealed class VisitStatus {
    object Loading : VisitStatus()
    data class GetMonthlyVisits(val response: VisitListModel) : VisitStatus()
    data class RefreshToken(val data: RefreshTokenRes) : VisitStatus()
    data class UpdateVisitDate(val response: UpdateVisitDateRes) : VisitStatus()
    data class Error(val message: String?) : VisitStatus()
    data class DeleteVisitPlan(val response: DeleteVisitRes) : VisitStatus()
    data class CopyPlan(val response: com.akhnaton.foodvisits.data.model.visitPlan.CopyPlanRes) : VisitStatus()
}