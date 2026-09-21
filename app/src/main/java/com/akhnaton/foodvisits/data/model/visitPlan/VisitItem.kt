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
    val customer_name: String,
    val customer_code: String,
    val site_address: String,
    val party_site: String,
    val sales_man: String,
    val approve: String?
)

data class UpdateVisitDateRes(
    val status: Int,
    val message: String?
)