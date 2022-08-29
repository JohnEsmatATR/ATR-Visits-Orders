package com.akhnaton.foodvisits.data.statusValue.visit

import com.akhnaton.foodvisits.data.model.VisitsPlaneData
import com.akhnaton.foodvisits.data.model.VisitsCustomerType
import com.akhnaton.foodvisits.data.model.visits.CustomerLines
import com.akhnaton.foodvisits.data.model.visits.CustomerSite
import com.akhnaton.foodvisits.data.model.visits.Lines
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit


sealed class VisitsStatus {

    object Idle : VisitsStatus()
    object Loading : VisitsStatus()
    data class Plan(val data: VisitsPlaneData) : VisitsStatus()
    data class GetCustomerType(val data: VisitsCustomerType) : VisitsStatus()
    data class GetLines(val data: Lines) : VisitsStatus()
    data class GetCustomerLines(val data: CustomerLines) : VisitsStatus()
    data class GetCustomersSite(val data: CustomerSite) : VisitsStatus()
    data class SaveVisits(val data: SaveVisit) :VisitsStatus()
    data class Error(val error: String?) : VisitsStatus()
}