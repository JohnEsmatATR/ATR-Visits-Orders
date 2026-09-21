package com.akhnaton.foodvisits.data.statusValue.visitPlan

import com.akhnaton.foodvisits.data.model.visitPlan.AddVisitPlan
import com.akhnaton.foodvisits.data.model.visitPlan.GetLinesRes
import com.akhnaton.foodvisits.data.model.visitPlan.GetVisitCustomersRes
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRes

sealed class AddVisitStatus {
    object Loading : AddVisitStatus()
    data class GetSalesTypes(val response: AddVisitPlan) : AddVisitStatus()
    data class Error(val message: String?) : AddVisitStatus()
    data class GetLines(val response: GetLinesRes) : AddVisitStatus()
    data class GetCustomers(val response: GetVisitCustomersRes) : AddVisitStatus()
    data class SaveSetupPlan(val response: SaveSetupPlanRes) : AddVisitStatus()
}