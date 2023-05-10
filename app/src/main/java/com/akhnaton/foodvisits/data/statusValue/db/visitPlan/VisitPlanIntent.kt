package com.akhnaton.foodvisits.data.statusValue.db.visitPlan

import com.akhnaton.foodvisits.data.db.model.VisitPlanDB

sealed class VisitPlanIntent {
    object GetVisitPlanList : VisitPlanIntent()
    data class AddVisitPlan(val visitPlan: VisitPlanDB) : VisitPlanIntent()
    data class DeleteVisitPlan(val visitPlan: VisitPlanDB) : VisitPlanIntent()
}