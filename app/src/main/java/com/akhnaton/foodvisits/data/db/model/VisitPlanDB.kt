package com.akhnaton.foodvisits.data.db.model

//@Entity(tableName = "visits_plan")
data class VisitPlanDB(
    val partySiteId: String,
    val customerName: String,
    val customerAddress: String,
    val customerType: String,
    val customerCode: String,
    val orderType: String,
    val lineId: String,
    val lat: String,
    val lng: String,
    val isVisitedToday: String,
)