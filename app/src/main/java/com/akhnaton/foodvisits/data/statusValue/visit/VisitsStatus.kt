package com.akhnaton.foodvisits.data.statusValue.visit

import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit

sealed class VisitsStatus {
    object Idle : VisitsStatus()
    object Loading : VisitsStatus()
    data class Plan(val data: VisitsPlan) : VisitsStatus()
    data class SaveVisits(val data: SaveVisit) : VisitsStatus()
    data class Error(val error: String?) : VisitsStatus()
}