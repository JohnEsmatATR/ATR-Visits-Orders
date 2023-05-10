package com.akhnaton.foodvisits.data.model.visits.saveVisit


data class SaveVisit(
    var status: Int,
    var data :SaveVisitData,
) {
    constructor(): this(0, SaveVisitData(0))
}

data class SaveVisitData(
    val visit_id:Int
) {
    constructor(): this(0)
}