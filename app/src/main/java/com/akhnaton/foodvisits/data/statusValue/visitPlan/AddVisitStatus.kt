package com.akhnaton.foodvisits.data.statusValue.visitPlan

import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanRes
import com.akhnaton.foodvisits.data.model.getSalesMan.GetSalesManRes
import com.akhnaton.foodvisits.data.model.refreshToken.RefreshTokenRes
import com.akhnaton.foodvisits.data.model.visitPlan.AddVisitPlan
import com.akhnaton.foodvisits.data.model.visitPlan.GetLinesRes
import com.akhnaton.foodvisits.data.model.visitPlan.GetVisitCustomersRes
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRes
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status

sealed class AddVisitStatus {
    object Loading : AddVisitStatus()
    data class GetSalesTypes(val response: AddVisitPlan) : AddVisitStatus()
    data class Error(val message: String?) : AddVisitStatus()
    data class GetLines(val response: GetLinesRes) : AddVisitStatus()
    data class GetCustomers(val response: GetVisitCustomersRes) : AddVisitStatus()
    data class SaveSetupPlan(val response: SaveSetupPlanRes) : AddVisitStatus()
    data class GetSalesMan(val response: GetSalesManRes) : AddVisitStatus()
    data class CopyDayPlan(val response: CopyDayPlanRes) : AddVisitStatus()
    data class RefreshToken(val response: RefreshTokenRes) : AddVisitStatus()
}