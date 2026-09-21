package com.akhnaton.foodvisits.data.statusValue.visitPlan

import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRequest

sealed class AddVisitIntent {
    data class RefreshToken(val userId: String, val token: String) : AddVisitIntent()
    object GetSalesTypes : AddVisitIntent()
    data class GetLines(val saleType: String) : AddVisitIntent()
    data class GetCustomers(val lineId: String) : AddVisitIntent()
    data class SaveSetupPlan(val request: SaveSetupPlanRequest) : AddVisitIntent()
}