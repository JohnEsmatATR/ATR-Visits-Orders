package com.akhnaton.foodvisits.data.statusValue.visitPlan

sealed class VisitIntent {
    object GetMonthlyVisits : VisitIntent()
    data class RefreshToken(val userId: String, val token: String) : VisitIntent()
    data class UpdateVisitDate(val id: String, val newDate: String) : VisitIntent()
    data class DeleteVisitPlan(val ids: List<Int>) : VisitIntent()
    data class CopyPlan(val sourceDate: String, val targetDate: String) : VisitIntent()
}