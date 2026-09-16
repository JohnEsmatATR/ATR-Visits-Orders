package com.akhnaton.foodvisits.data.statusValue.visitPlan

sealed class VisitIntent {
    object GetMonthlyVisits : VisitIntent()
    data class RefreshToken(val userId: String, val token: String) : VisitIntent()
}