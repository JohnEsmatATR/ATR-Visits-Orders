package com.akhnaton.foodvisits.data.statusValue.visitPlan

import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRequest
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent

sealed class AddVisitIntent {
    object GetSalesTypes : AddVisitIntent()
    data class GetLines(val saleType: String) : AddVisitIntent()
    data class GetCustomers(val lineId: String) : AddVisitIntent()
    data class SaveSetupPlan(val request: SaveSetupPlanRequest) : AddVisitIntent()
    object GetSalesMan : AddVisitIntent()
    data class CopyDayPlan(val copyDayPlanReq: CopyDayPlanReq) : AddVisitIntent()
    data class RefreshToken(val userId: String, val token: String) : AddVisitIntent()
}