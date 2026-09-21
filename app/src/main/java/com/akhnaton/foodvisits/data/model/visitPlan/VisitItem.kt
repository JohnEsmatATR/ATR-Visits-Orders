package com.akhnaton.foodvisits.data.model.visitPlan
import com.google.gson.JsonElement

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

data class DeleteVisitReq(
    val ids: List<Int>
)

data class DeleteVisitData(
    val success: Boolean
)

data class DeleteVisitRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: DeleteVisitData
)

data class CopyPlanReq(
    val source_date: String,
    val target_date: String
)

data class CopyPlanData(
    val inserted_count: Int,
    val skipped_count: Int,
    val message: String,
    val skipped_details: JsonElement? = null,
    val warnings: JsonElement? = null
)

data class CopyPlanRes(
    val status: Int,
    val message: String,
    val type: String,
    val data: CopyPlanData
)