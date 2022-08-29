package com.akhnaton.foodvisits.data.model.visits.saveVisit

data class SaveVisit(
    var status: Int,
    var data :SaveVisitData
)

data class SaveVisitData(
    val visit_id:Int
)