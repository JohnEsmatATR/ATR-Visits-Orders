package com.akhnaton.foodvisits.data.statusValue.db.visitPlan

import com.akhnaton.foodvisits.data.db.model.VisitPlanDB

sealed class VisitPlanState {
    object Idle : VisitPlanState()
    object Loading : VisitPlanState()
    data class VisitPlanList(val visitPlanList: List<VisitPlanDB>) : VisitPlanState()
    data class Error(val message: String) : VisitPlanState()
}