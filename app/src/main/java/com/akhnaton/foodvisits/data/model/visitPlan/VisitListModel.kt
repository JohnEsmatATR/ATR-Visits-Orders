package com.akhnaton.foodvisits.data.model.visitPlan

data class VisitListModel(
    val status: Int,
    val message: String,
    val type: String,
    val data: VisitData
)

data class VisitData(
    val visits: List<VisitItem>
)

data class VisitItem(
    val id: String,
    val start: String,
    val title: String,
    val party_site: String,
    val approve: String?
)