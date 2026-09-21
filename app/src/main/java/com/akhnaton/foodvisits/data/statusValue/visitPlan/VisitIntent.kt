package com.akhnaton.foodvisits.data.statusValue.visitPlan

import com.akhnaton.foodvisits.data.model.deleteVisitPlan.DeleteVisitPlanReq

sealed class VisitIntent {
    object GetMonthlyVisits : VisitIntent()
    data class RefreshToken(val userId: String, val token: String) : VisitIntent()
    data class UpdateVisitDate(val id: String, val newDate: String) : VisitIntent()
    data class DeleteVisitDate(val deleteVisitPlanReq: DeleteVisitPlanReq) : VisitIntent()
}